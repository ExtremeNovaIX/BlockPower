package BlockPower.Util.Timer;

import BlockPower.ModException.TimerException;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 一个全局的、单例的计时器管理器。
 * 此管理器提供了多种创建和管理计时器的方式，包括基于唯一字符串键的全局计时器，
 * 以及与特定实体关联的、可包含多个命名子计时器的计时器集合。
 * 管理器能自动处理与实体生命周期相关的内存回收，并提供严格的API使用检查。
 */
public final class TimerManager {

    private static final TimerManager SERVER_INSTANCE = new TimerManager(false);
    private static final TimerManager CLIENT_INSTANCE = new TimerManager(true);

    public final Map<String, TickTimer> globalTimers = new HashMap<>();
    public final Map<Entity, Map<String, TickTimer>> entityToStringTimers = new WeakHashMap<>();
    public final Map<Entity, Map<Entity, TickTimer>> entityToEntityTimers = new WeakHashMap<>();

    private final boolean isClient;

    private TimerManager(boolean isClient) {
        this.isClient = isClient;
    }

    /**
     * 获取 TimerManager 的实例
     * @param isClient 代码是否运行在客户端？
     * @return 对应端的 TimerManager 实例
     */
    public static TimerManager getInstance(boolean isClient) {
        return isClient ? CLIENT_INSTANCE : SERVER_INSTANCE;
    }

    /**
     * 检查全局计时器是否已完成。
     * 如果timerName计时器不存在，则创建名字为timerName的计时器。
     * 此方法在计时器完成后会执行一次if内语句，并且在循环中会自动无限循环重置计时器
     * @param timerName 计时器的唯一字符串标识符。
     * @param tickDuration 计时器需要持续的 tick 数量。
     * @return 如果计时器已完成返回 true，否则返回 false。
     */
    public boolean isTimerCyclingDue(@NotNull String timerName, @NotNull Integer tickDuration) {
        this.createTimerIfAbsent(timerName, tickDuration);
        return this.isFinished(timerName);
    }

    /**
     * 检查实体挂载字符串式计时器是否已完成。
     * 如果timerName计时器不存在，则创建主键为timerName的计时器。
     * 此方法在计时器完成后会执行一次if内语句，并且在循环中会自动无限循环重置计时器
     * @param entity 计时器的挂载对象。
     * @param timerName 计时器的唯一字符串标识符。
     * @param tickDuration 计时器需要持续的 tick 数量。
     * @return 如果计时器已完成返回 true，否则返回 false。
     */
    public boolean isTimerCyclingDue(@NotNull Entity entity, @NotNull String timerName, @NotNull Integer tickDuration) {
        this.createTimerIfAbsent(entity, timerName, tickDuration);
        return this.isFinished(entity, timerName);
    }

    /**
     * 检查实体挂载实体式计时器是否已完成。
     * 如果effectedEntity计时器不存在，则创建主键为effectedEntity的计时器。
     * 此方法在计时器完成后会执行一次if内语句，并且在循环中会自动无限循环重置计时器
     * @param entity 计时器的挂载对象。
     * @param effectedEntity 受影响的实体。
     * @param tickDuration 计时器需要持续的 tick 数量。
     * @return 如果计时器已完成返回 true，否则返回 false。
     */
    public boolean isTimerCyclingDue(@NotNull Entity entity, @NotNull Entity effectedEntity, @NotNull Integer tickDuration) {
        this.createTimerIfAbsent(entity, effectedEntity, tickDuration);
        return this.isFinished(entity, effectedEntity);
    }


    /**
     * 设置或重置一个由字符串键标识的全局计时器。
     * 如果具有相同名称的计时器已存在，它将被新的计时器覆盖。
     * @param timerName 计时器的唯一字符串标识符。
     * @param tickDuration 计时器需要持续的 tick 数量。
     */
    public void setTimer(@NotNull String timerName, int tickDuration) {
        globalTimers.put(timerName, new TickTimer(tickDuration, this.isClient));
    }

    /**
     * 为特定实体设置或重置一个具名计时器。
     * 如果该实体已存在同名计时器，它将被新的计时器覆盖。
     * @param entity 与计时器关联的实体。
     * @param timerName 该实体内计时器的唯一名称。
     * @param tickDuration 计时器需要持续的 tick 数量。
     */
    public void setTimer(@NotNull Entity entity, @NotNull String timerName, int tickDuration) {
        Map<String, TickTimer> innerMap = entityToStringTimers.computeIfAbsent(entity, k -> new HashMap<>());
        innerMap.put(timerName, new TickTimer(tickDuration, this.isClient));
    }

    /**
     * 为特定实体设置或重置一个具名计时器。
     * 如果该实体已存在同名计时器，它将被新的计时器覆盖。
     * @param entity 与计时器关联的实体。
     * @param effectedEntity 受影响的实体。
     * @param tickDuration 计时器需要持续的 tick 数量。
     */
    public void setTimer(@NotNull Entity entity, @NotNull Entity effectedEntity, int tickDuration) {
        Map<Entity, TickTimer> innerMap = entityToEntityTimers.computeIfAbsent(entity, k -> new HashMap<>());
        innerMap.put(effectedEntity, new TickTimer(tickDuration, this.isClient));
    }

    /**
     * 确保一个由字符串键标识的全局计时器存在。
     * 仅当同名计时器不存在时，才会创建新的计时器。如果已存在，则此方法不执行任何操作。
     * 此方法适合在循环中安全调用。
     * @param timerName 计时器的唯一字符串标识符。
     * @param tickDuration 计时器需要持续的 tick 数量。
     */
    private void createTimerIfAbsent(@NotNull String timerName, int tickDuration) {
        globalTimers.putIfAbsent(timerName, new TickTimer(tickDuration, this.isClient));
    }

    /**
     * 确保一个特定实体的具名计时器存在。
     * 仅当该实体不存在同名计时器时，才会创建新的计时器。如果已存在，则此方法不执行任何操作。
     * 此方法适合在循环中安全调用。
     * @param entity 与计时器关联的实体。
     * @param timerName 该实体内计时器的唯一名称。
     * @param tickDuration 计时器需要持续的 tick 数量。
     */
    private void createTimerIfAbsent(@NotNull Entity entity, @NotNull String timerName, int tickDuration) {
        Map<String, TickTimer> innerMap = entityToStringTimers.computeIfAbsent(entity, k -> new HashMap<>());
        innerMap.putIfAbsent(timerName, new TickTimer(tickDuration, this.isClient));
    }

    /**
     * 确保一个特定实体的具名计时器存在。
     * 仅当该实体不存在同名计时器时，才会创建新的计时器。如果已存在，则此方法不执行任何操作。
     * 此方法适合在循环中安全调用。
     * @param entity 与计时器关联的实体。
     * @param timerName 该实体内计时器的唯一名称。
     * @param tickDuration 计时器需要持续的 tick 数量。
     */
    private void createTimerIfAbsent(@NotNull Entity entity, @NotNull Entity timerName, int tickDuration) {
        Map<Entity, TickTimer> innerMap = entityToEntityTimers.computeIfAbsent(entity, k -> new HashMap<>());
        innerMap.putIfAbsent(timerName, new TickTimer(tickDuration, this.isClient));
    }

    /**
     * 检查一个全局计时器当前是否处于活动状态。
     * 这是一个无副作用的纯查询方法。
     * @param timerName 要检查的计时器的名称。
     * @return 如果计时器存在且正在运行，返回 true；否则返回 false。
     */
    public boolean isTimerActive(@NotNull String timerName) {
        return globalTimers.containsKey(timerName);
    }

    /**
     * 检查一个特定实体的具名计时器当前是否处于活动状态。
     * 这是一个无副作用的纯查询方法。
     * @param mainEntity 挂载timer的主实体。
     * @param timerName 要检查的计时器的名称。
     * @return 如果计时器存在且正在运行，返回 true；否则返回 false。
     */
    public boolean isTimerActive(@NotNull Entity mainEntity, @NotNull String timerName) {
        Map<String, TickTimer> innerMap = entityToStringTimers.get(mainEntity);
        return innerMap != null && innerMap.containsKey(timerName);
    }

    /**
     * 检查一个特定实体的具名计时器是否处于活动状态。
     * 这是一个无副作用的纯查询方法。
     * @param mainEntity 挂载timer的主实体。
     * @param effectedEntity 受影响的实体。
     * @return 如果计时器存在且正在运行，返回 true；否则返回 false。
     */
    public boolean isTimerActive(@NotNull Entity mainEntity, @NotNull Entity effectedEntity) {
        Map<Entity, TickTimer> innerMap = entityToEntityTimers.get(mainEntity);
        return innerMap != null && innerMap.containsKey(effectedEntity);
    }

    /**
     * 检查一个全局计时器是否已完成。
     * 此方法具有副作用：如果计时器已完成，它将被从管理器中移除。
     * @param timerName 要检查的计时器的名称。
     * @return 如果计时器已完成，返回 true；否则返回 false。
     */
    public boolean isFinished(@NotNull String timerName) {
        TickTimer timer = globalTimers.get(timerName);
        if (timer == null)
            throw new TimerException("Attempted to check a non-existent global timer: '" + timerName + "'. Use isTimerActive() first.");

        if (timer.isFinished()) {
            globalTimers.remove(timerName);
            return true;
        }
        return false;
    }

    /**
     * 检查一个特定实体的具名计时器是否已完成。
     * 此方法具有副作用：如果计时器已完成，它将被从管理器中移除。
     * @param entity 要检查的实体。
     * @param timerName 要检查的计时器的名称。
     * @return 如果计时器已完成，返回 true；否则返回 false。
     */
    public boolean isFinished(@NotNull Entity entity, @NotNull String timerName) {

        Map<String, TickTimer> innerMap = entityToStringTimers.get(entity);
        if (innerMap == null)
            throw new TimerException("Attempted to check a timer for an entity that has no active timers.");

        TickTimer timer = innerMap.get(timerName);
        if (timer == null)
            throw new TimerException("Attempted to check a non-existent timer '" + timerName + "' for entity: " + entity.getUUID());

        if (timer.isFinished()) {
            innerMap.remove(timerName);
            // 如果一个实体的所有计时器都已完成，则可以移除该实体的条目以节省内存
            if (innerMap.isEmpty()) {
                entityToStringTimers.remove(entity);
            }
            return true;
        }
        return false;
    }

    /**
     * 检查一个特定实体的具名计时器是否已完成。
     * 此方法具有副作用：如果计时器已完成，它将被从管理器中移除。
     * @param mainEntity 挂载timer的主实体。
     * @param effectedEntity 受影响的实体。
     * @return 如果计时器已完成，返回 true；否则返回 false。
     */
    public boolean isFinished(@NotNull Entity mainEntity, @NotNull Entity effectedEntity) {
        Map<Entity, TickTimer> innerMap = entityToEntityTimers.get(mainEntity);
        if (innerMap == null)
            throw new TimerException("Attempted to check a timer for an entity that has no active timers.");

        TickTimer timer = innerMap.get(effectedEntity);
        if (timer == null)
            throw new TimerException("Attempted to check a non-existent timer '" + effectedEntity.getUUID() + "' for entity: " + mainEntity.getUUID());

        if (timer.isFinished()) {
            innerMap.remove(effectedEntity);
            // 如果一个实体的所有计时器都已完成，则可以移除该实体的条目以节省内存
            if (innerMap.isEmpty()) {
                entityToEntityTimers.remove(mainEntity);
            }
            return true;
        }
        return false;
    }


}
