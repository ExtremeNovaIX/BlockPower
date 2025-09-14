package BlockPower.Util;

import BlockPower.Util.Timer.TimerManager;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskManager {
    private static final TaskManager SERVER_INSTANCE = new TaskManager(false);
    private static final TaskManager CLIENT_INSTANCE = new TaskManager(true);

    private final Set<scheduledTaskState> scheduledTasks = new HashSet<>();
    private final Map<String, RepeatingTaskState> repeatingTaskMap = new ConcurrentHashMap<>();
    private final Map<Entity, Map<String, Integer>> taskExecutionCounter = new WeakHashMap<>();// 挂载在实体下的任务执行次数Map
    private final Map<Entity, Set<String>> coolingDownTasks = new WeakHashMap<>();// 挂载在实体下的冷却中任务Map
    private final TimerManager timerManager;
    private final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);

    private TaskManager(boolean isClient) {
        this.timerManager = TimerManager.getInstance(isClient);
    }

    /**
     * 获取 TaskManager 的实例
     *
     * @param isClient 代码是否运行在客户端?
     * @return 对应端的 TaskManager 实例
     */
    public static TaskManager getInstance(boolean isClient) {
        return isClient ? CLIENT_INSTANCE : SERVER_INSTANCE;
    }

    /**
     * 添加任务到自更新timer池内，在tickDuration后执行。
     * 没有重复检查，最好不要在循环内执行。
     * 此方法是线程安全的。
     *
     * @param tickDuration 在多少tick后执行。
     * @param runnable     执行的方法或者语句。
     */
    public synchronized void runTaskAfterTicks(@NotNull Integer tickDuration, @NotNull Runnable runnable) {
        scheduledTasks.add(new scheduledTaskState(runnable, tickDuration));
    }

    /**
     * 添加任务到自更新timer池内，在tickDuration后执行
     * 如果任务ID已存在且任务未执行，会覆盖旧任务
     * 此方法是线程安全的
     *
     * @param tickDuration 在多少tick后执行。
     * @param runnable     执行的方法或者语句。
     */
    public synchronized void runOverrideTaskAfterTicks(@NotNull Integer tickDuration, @NotNull Runnable runnable, @NotNull String taskID) {
        //移除相同taskID的现有任务
        scheduledTasks.removeIf(entry -> entry.taskID.equals(taskID));

        //添加新任务
        scheduledTasks.add(new scheduledTaskState(runnable, tickDuration, taskID));
    }

    /**
     * 安排一个任务在接下来的 X tick 内，每 tick 都执行一次。
     *
     * @param taskID      任务的唯一ID (用于覆盖检查)
     * @param runTimes    总共执行的次数 (例如 40 ticks, 将执行40次)
     * @param runnable    每tick要执行的代码
     * @param canOverride 如果为true, 同名的旧任务(如果存在)会被这个新任务覆盖; 如果为false, 则会失败
     */
    public void scheduleRepeatingTaskPerTick(@NotNull String taskID, @NotNull Integer runTimes, @NotNull Runnable runnable, @NotNull Boolean canOverride) {
        // 如果不允许覆盖，并且任务已存在，则直接返回
        if (!canOverride && repeatingTaskMap.containsKey(taskID)) {
            return;
        }
        // 放入（或覆盖）新任务
        repeatingTaskMap.put(taskID, new RepeatingTaskState(runnable, runTimes));
    }

    /**
     * 在指定实体下挂载任务，仅执行一次
     * 相同实体指定次数耗尽后不再执行
     * 此方法现在是线程安全的。
     *
     * @param entity     实体
     * @param methodName 方法名
     * @param runnable   执行的方法或者语句
     */
    public synchronized void runOnce(@NotNull Entity entity, @NotNull String methodName, @NotNull Runnable runnable) {
        runTimes(entity, methodName, 1, runnable);
    }

    /**
     * 在指定实体下挂载任务，会执行指定次数
     * 相同实体指定次数耗尽后不再执行
     * 此方法是线程安全的。
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param runnable      执行的方法或者语句
     * @param maxExecutions 执行次数
     */
    public synchronized void runTimes(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions, @NotNull Runnable runnable) {
        if (entity.isRemoved()) {
            taskExecutionCounter.remove(entity);
            return;
        }
        int remainTimes = getRemainExecutions(entity, methodName, maxExecutions);

        if (remainTimes > 0) {
            runnable.run();
            setRemainExecutions(entity, methodName, remainTimes - 1);
        }
    }

    /**
     * 在指定实体下挂载任务，只执行一次
     * 任务执行后会添加到冷却列表中，冷却时间结束后刷新使用次数并且可以再度执行
     * 此方法是线程安全的。
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param cooldownTicks 冷却时间
     * @param runnable      执行的方法或者语句
     */
    public synchronized void runOnceWithCooldown(@NotNull Entity entity, @NotNull String methodName, Integer cooldownTicks, @NotNull Runnable runnable) {
        runTimesWithCooldown(entity, methodName, 1, cooldownTicks, runnable);
    }

    /**
     * 在指定实体下挂载任务，会执行指定次数
     * 任务执行次数耗尽后会添加到冷却列表中，冷却时间结束后刷新使用次数并且可以再度执行
     * 此方法是线程安全的。
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param maxExecutions 执行次数
     * @param cooldownTicks 冷却时间
     * @param runnable      执行的方法或者语句
     */
    public synchronized void runTimesWithCooldown(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions, @NotNull Integer cooldownTicks, @NotNull Runnable runnable) {
        if (entity.isRemoved()) {
            taskExecutionCounter.remove(entity);
            return;
        }

        // 创建计时器任务时会只执行一次，把entity放到冷却中任务Map中并添加空HashSet集合用于存储任务名称
        coolingDownTasks.computeIfAbsent(entity, k -> new HashSet<>());

        // 如果任务在冷却列表中，并且冷却时间已到，就将其移除并刷新次数。
        if (coolingDownTasks.get(entity).contains(methodName)) {
            if (timerManager.isTimerCyclingDue(entity, methodName, cooldownTicks)) {
                coolingDownTasks.get(entity).remove(methodName);
                // 刷新任务执行次数
                setRemainExecutions(entity, methodName, maxExecutions);
            }
        }

        // 处理完冷却状态后，尝试执行任务。
        if (!coolingDownTasks.get(entity).contains(methodName)) {
            int remainTimes = getRemainExecutions(entity, methodName, maxExecutions);

            if (remainTimes > 0) {
                // 如果当前任务执行次数未耗尽，则执行任务
                runnable.run();
                setRemainExecutions(entity, methodName, remainTimes - 1);

                // 如果减少后次数为0，则将任务添加到冷却列表中
                if (remainTimes - 1 == 0) {
                    coolingDownTasks.get(entity).add(methodName);
                    timerManager.setTimer(entity, methodName, cooldownTicks);
                }
            }
        }

    }

    /**
     * 只查询指定实体下的特定任务的剩余执行次数，不会自动创建Map
     * 此方法是线程安全的。
     *
     * @param entity     实体
     * @param methodName 方法名
     * @return 剩余执行次数，如果实体或任务不存在则返回-1
     */
    public synchronized int queryRemainExecutions(@NotNull Entity entity, @NotNull String methodName) {
        // 只查询，不自动创建Map
        Map<String, Integer> innerMap = taskExecutionCounter.get(entity);
        if (innerMap == null) {
            return -1;
        }

        // 使用getOrDefault获取当前剩余次数，如果不存在则返回-1
        return innerMap.getOrDefault(methodName, -1);
    }

    /**
     * 手动刷新指定实体下的特定任务执行次数
     * 此方法是线程安全的。
     *
     * @param entity     实体
     * @param methodName 方法名
     */
    public synchronized void flushTasks(@NotNull Entity entity, @NotNull String methodName) {
        Map<String, Integer> innerMap = taskExecutionCounter.get(entity);
        if (innerMap != null) {
            innerMap.remove(methodName);
        }
    }

    /**
     * 获取指定实体下的特定任务的剩余执行次数
     * 注意：此方法会自动创建实体内部Map，不对外开放。
     * 想要查询实体下的特定任务的剩余执行次数，请使用queryRemainExecutions方法。
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param maxExecutions 最大执行次数
     * @return 剩余执行次数
     */
    private int getRemainExecutions(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions) {
        // 获取该实体的内部Map，如果不存在则创建一个
        Map<String, Integer> innerMap = taskExecutionCounter.computeIfAbsent(entity, k -> new HashMap<>());

        // 使用getOrDefault 获取当前剩余次数。如果不存在，则使用传入的 maxExecutions 作为初始值
        return innerMap.getOrDefault(methodName, maxExecutions);
    }

    /**
     * 手动设置指定实体下的特定任务的剩余执行次数
     * 注意：此方法会自动创建实体内部Map，不对外开放。
     * 想要手动设置实体下的特定任务的剩余执行次数，请使用setRemainExecutions方法。
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param maxExecutions 最大执行次数
     */
    private void setRemainExecutions(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions) {
        // 获取该实体的内部Map，如果不存在则创建一个
        Map<String, Integer> innerMap = taskExecutionCounter.computeIfAbsent(entity, k -> new HashMap<>());

        // 在innerMap中更新任务执行次数
        innerMap.put(methodName, maxExecutions);
    }

    public void updateTick() {
        this.updateScheduledTasksList();
        this.updateRepeatingTasksList();
    }

    /**
     * 更新“延迟执行”任务列表
     */
    private void updateScheduledTasksList() {
        scheduledTasks.removeIf(entry -> {
            entry.runAfterTicks--;
            if (entry.runAfterTicks <= 0) {
                entry.task.run();
                return true;
            }
            return false;
        });
    }

    /**
     * 更新“每Tick重复执行”任务列表
     */
    private void updateRepeatingTasksList() {
        // 如果Map为空，快速退出
        if (repeatingTaskMap.isEmpty()) {
            return;
        }

        repeatingTaskMap.entrySet().removeIf(entry -> {
            RepeatingTaskState state = entry.getValue();
            state.run(); // 运行任务（内部会自减计数器）
            return state.isFinished(); // 如果任务已完成（次数耗尽），返回true，任务将被移除
        });
    }

    private String getRandomUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     * 内部辅助类：用于封装重复任务的状态
     */
    private static class RepeatingTaskState {
        final Runnable task;
        int remainingTicks; // 剩余执行次数

        public RepeatingTaskState(Runnable r, int ticks) {
            this.task = r;
            this.remainingTicks = ticks;
        }

        public void run() {
            if (remainingTicks > 0) {
                task.run();
                remainingTicks--;
            }
        }

        public boolean isFinished() {
            return remainingTicks <= 0;
        }
    }

    private static class scheduledTaskState {
        final Runnable task;
        int runAfterTicks;
        final String taskID;

        public scheduledTaskState(Runnable task, int runAfterTicks, String taskID) {
            this.task = task;
            this.runAfterTicks = runAfterTicks;
            this.taskID = taskID;
        }

        public scheduledTaskState(Runnable task, int runAfterTicks) {
            this.task = task;
            this.runAfterTicks = runAfterTicks;
            this.taskID = String.valueOf(this.hashCode());
        }

        @Override
        public int hashCode() {
            if (taskID == null) return super.hashCode();
            return taskID.hashCode();
        }
    }
}