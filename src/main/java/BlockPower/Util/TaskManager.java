package BlockPower.Util;

import BlockPower.Util.Timer.TimerManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Mod.EventBusSubscriber
public class TaskManager {
    private static final TaskManager INSTANCE = new TaskManager();
    public static final List<Map.Entry<Runnable, Integer>> scheduledTasks = new ArrayList<>();// 自更新timer池
    public static final Map<Entity, Map<String, Integer>> taskExecutionCounter = new WeakHashMap<>();// 挂载在实体下的任务执行次数Map
    private static final Map<Entity, Set<String>> coolingDownTasks = new WeakHashMap<>();// 挂载在实体下的冷却中任务Map
    private static final TimerManager timerManager = TimerManager.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);

    private TaskManager() {
    }

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    /**
     * 添加任务到自更新timer池内，在tickDuration后执行。
     * 没有重复检查，最好不要在循环内执行。
     *
     * @param tickDuration 在多少tick后执行。
     * @param runnable     执行的方法或者语句。
     */
    public void runTaskAfterTicks(@NotNull Integer tickDuration, @NotNull Runnable runnable) {
        scheduledTasks.add(new AbstractMap.SimpleEntry<>(runnable, tickDuration));
    }

    /**
     * 在指定实体下挂载任务，仅执行一次
     * 相同实体指定次数耗尽后不再执行
     *
     * @param entity     实体
     * @param methodName 方法名
     * @param runnable   执行的方法或者语句
     */
    public void runOnce(@NotNull Entity entity, @NotNull String methodName, @NotNull Runnable runnable) {
        runTimes(entity, methodName, 1, runnable);
    }

    /**
     * 在指定实体下挂载任务，会执行指定次数
     * 相同实体指定次数耗尽后不再执行
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param runnable      执行的方法或者语句
     * @param maxExecutions 执行次数
     */
    public void runTimes(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions, @NotNull Runnable runnable) {
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
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param cooldownTicks 冷却时间
     * @param runnable      执行的方法或者语句
     */
    public void runOnceWithCooldown(@NotNull Entity entity, @NotNull String methodName, Integer cooldownTicks, @NotNull Runnable runnable) {
        runTimesWithCooldown(entity, methodName, 1, cooldownTicks, runnable);
    }

    /**
     * 在指定实体下挂载任务，会执行指定次数
     * 任务执行次数耗尽后会添加到冷却列表中，冷却时间结束后刷新使用次数并且可以再度执行
     *
     * @param entity        实体
     * @param methodName    方法名
     * @param maxExecutions 执行次数
     * @param cooldownTicks 冷却时间
     * @param runnable      执行的方法或者语句
     */
    public void runTimesWithCooldown(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions, @NotNull Integer cooldownTicks, @NotNull Runnable runnable) {
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
     *
     * @param entity     实体
     * @param methodName 方法名
     * @return 剩余执行次数，如果实体或任务不存在则返回-1
     */
    public int queryRemainExecutions(@NotNull Entity entity, @NotNull String methodName) {
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
     *
     * @param entity     实体
     * @param methodName 方法名
     */
    public void flushTasks(@NotNull Entity entity, @NotNull String methodName) {
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            updateScheduledTasksList();
        }
    }

    private static void updateScheduledTasksList() {
        scheduledTasks.removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            if (entry.getValue() <= 0) {
                entry.getKey().run();
                return true;
            }
            return false;
        });
    }

    private String getRandomUUID() {
        return UUID.randomUUID().toString();
    }
}
