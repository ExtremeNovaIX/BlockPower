package BlockPower.Util;

import BlockPower.Util.Timer.TimerManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.Entity;

import java.util.*;

@Mod.EventBusSubscriber
public class TaskManager {
    private static final TaskManager INSTANCE = new TaskManager();
    public static final List<Map.Entry<Runnable, Integer>> scheduledTasks = new ArrayList<>();// 自更新timer池
    public static final Map<Entity, Map<String, Integer>> taskExecutionCounter = new WeakHashMap<>();// 挂载在实体下的任务执行次数Map
    private static final Map<Entity, Set<String>> coolingDownTasks = new WeakHashMap<>();// 挂载在实体下的冷却中任务Map
    private static final TimerManager timerManager = TimerManager.getInstance();


    private TaskManager() {
    }

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    /**
     * 添加任务到自更新timer池内，在tickDuration后执行。
     * 没有重复检查，最好不要在循环内执行。
     * @param tickDuration 在多少tick后执行。
     * @param runnable 执行的方法或者语句。
     */
    public void runTaskAfterTicks(@NotNull Integer tickDuration, @NotNull Runnable runnable) {
        scheduledTasks.add(new AbstractMap.SimpleEntry<>(runnable, tickDuration));
    }

    /**
     * 在指定实体下挂载任务，仅执行一次
     * 相同实体指定次数耗尽后不再执行
     * @param entity 实体
     * @param methodName 方法名
     * @param runnable 执行的方法或者语句
     */
    public void runOnce(@NotNull Entity entity, @NotNull String methodName, @NotNull Runnable runnable) {
        runTimes(entity, methodName, 1, runnable);
    }

    /**
     * 在指定实体下挂载任务，会执行指定次数
     * 相同实体指定次数耗尽后不再执行
     * @param entity 实体
     * @param methodName 方法名
     * @param runnable 执行的方法或者语句
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
     * @param entity 实体
     * @param methodName 方法名
     * @param cooldownTicks 冷却时间
     * @param runnable 执行的方法或者语句
     */
    public void runOnceWithCooldown(@NotNull Entity entity, @NotNull String methodName, Integer cooldownTicks, @NotNull Runnable runnable) {
        runTimesWithCooldown(entity, methodName, 1, cooldownTicks, runnable);
    }

    /**
     * 在指定实体下挂载任务，会执行指定次数
     * 任务执行次数耗尽后会添加到冷却列表中，冷却时间结束后刷新使用次数并且可以再度执行
     * @param entity 实体
     * @param methodName 方法名
     * @param maxExecutions 执行次数
     * @param cooldownTicks 冷却时间
     * @param runnable 执行的方法或者语句
     */
    public void runTimesWithCooldown(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions, @NotNull Integer cooldownTicks, @NotNull Runnable runnable) {
        if (entity.isRemoved()) {
            taskExecutionCounter.remove(entity);
            return;
        }

        // 创建计时器任务时会只执行一次，把entity放到冷却中任务Map中并添加空HashSet集合用于存储任务名称
        coolingDownTasks.computeIfAbsent(entity, k -> new HashSet<>());

        // 如果当前任务不在冷却列表中
        if (!coolingDownTasks.get(entity).contains(methodName)) {
            int remainTimes = getRemainExecutions(entity, methodName, maxExecutions);

            if (remainTimes > 0) {
                // 如果当前任务执行次数未耗尽，则执行任务
                runnable.run();
                setRemainExecutions(entity, methodName, remainTimes - 1);

                if (remainTimes - 1 == 0) {
                    // 如果减少后次数为0，则立即将任务添加到冷却列表中
                    coolingDownTasks.get(entity).add(methodName);
                    timerManager.setTimer(entity, methodName, cooldownTicks);
                }

            }

        } else {
            // 如果当前任务在冷却列表中
            if (timerManager.isTimerCyclingDue(entity, methodName, cooldownTicks)) {
                // 如果冷却时间到了，则从冷却列表中移除任务
                coolingDownTasks.get(entity).remove(methodName);
                // 刷新任务执行次数
                setRemainExecutions(entity, methodName, maxExecutions);
            }
        }

    }

    public int getRemainExecutions(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions) {
        // 获取该实体的内部Map，如果不存在则创建一个
        Map<String, Integer> innerMap = taskExecutionCounter.computeIfAbsent(entity, k -> new HashMap<>());

        // 使用getOrDefault 获取当前剩余次数。如果不存在，则使用传入的 maxExecutions 作为初始值
        return innerMap.getOrDefault(methodName, maxExecutions);
    }

    public void setRemainExecutions(@NotNull Entity entity, @NotNull String methodName, @NotNull Integer maxExecutions) {
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
