package BlockPower.Skills.SkillLock;

import BlockPower.ModException.SkillLockException;
import BlockPower.Util.Timer.TickListener;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 技能锁管理器，用于管理玩家的技能锁
 * 可以禁止玩家使用技能
 */
public class SkillLockManager {
    public static final int LOCK_MAX_TICK = 400;// 如果超出此最大时长未解锁，则会抛出异常

    /**
     * 技能锁数据类，用于存储玩家的技能锁优先级信息
     */
    public static class LockData {
        public final int priority;
        public final int duration;
        public final long startTick;

        public LockData(int priority, int duration) {
            this.priority = priority;
            this.duration = duration;
            this.startTick = TickListener.getServerTicks();
        }

        // 无持续时间的锁
        public LockData(int priority) {
            this.priority = priority;
            this.duration = -1;
            this.startTick = -1;
        }

        public boolean isFinished() {
            if (duration == -1) return false;
            return TickListener.getServerTicks() - startTick >= duration;
        }
    }

    /**
     * 玩家技能锁容器类，用于存储玩家的技能锁信息
     */
    private static class PlayerLockContainer {
        private final Map<String, LockData> skillLocks = new HashMap<>();
        private int highestPriorityCache = 0;

        /**
         * 添加或更新玩家的技能锁
         *
         * @param lockId   技能锁ID
         * @param priority 技能锁优先级
         * @param duration 技能锁持续时间
         *                 注意，如果lockId已存在，则会更新其优先级和持续时间
         */
        public void addOrUpdateLock(@NotNull String lockId, int priority, int duration) {
            if (duration == -1) {
                skillLocks.put(lockId, new LockData(priority));
            } else {
                skillLocks.put(lockId, new LockData(priority, duration));
            }
            // 更新缓存
            if (priority > highestPriorityCache) {
                highestPriorityCache = priority;
            }
        }

        /**
         * 移除玩家的技能锁
         *
         * @param lockId 技能锁ID
         */
        public void removeLock(@NotNull String lockId) {
            LockData removedLock = skillLocks.remove(lockId);

            // 如果被移除的锁是当前最高的锁，则重新计算当前最高优先级
            if (removedLock != null && removedLock.priority == highestPriorityCache) {
                // 遍历所有剩余的锁，找到新的最大值
                this.highestPriorityCache = skillLocks.values().stream()
                        .mapToInt(lock -> lock.priority)
                        .max()
                        .orElse(0); // 如果没有锁，重置为0
            }
        }

        public int getHighestPriority() {
            return this.highestPriorityCache;
        }

        public boolean isEmpty() {
            return skillLocks.isEmpty();
        }

        public LockData getData(@NotNull String lockId) {
            return skillLocks.get(lockId);
        }

        public void tick() {
            if (skillLocks.isEmpty()) return;
            long currentTick = TickListener.getServerTicks();

            // 收集所有已过期的锁ID
            ArrayList<String> expiredLockIds = new ArrayList<>();
            for (Map.Entry<String, LockData> entry : skillLocks.entrySet()) {
                LockData data = entry.getValue();

                if (data.isFinished()) {
                    expiredLockIds.add(entry.getKey());
                }
            }

            // 移除所有过期的锁
            for (String lockId : expiredLockIds) {
                this.removeLock(lockId);
            }
        }
    }

    private static final Map<Player, PlayerLockContainer> playerLocks = new WeakHashMap<>();

    /**
     * 为玩家添加一个技能锁
     *
     * @param player   玩家实体
     * @param lockId   技能锁ID
     * @param priority 技能锁优先级
     * @param duration 技能锁持续时间
     */
    public static synchronized void lock(@NotNull Player player, @NotNull String lockId, LockPriority priority, int duration) {
        PlayerLockContainer container = playerLocks.computeIfAbsent(player, k -> new PlayerLockContainer());
        container.addOrUpdateLock(lockId, priority.getPriority(), duration);
    }

    /**
     * 为玩家添加一个技能锁
     * 默认优先级为NORMAL
     *
     * @param player   玩家实体
     * @param lockId   技能锁ID
     * @param duration 技能锁持续时间
     */
    public static synchronized void lock(@NotNull Player player, @NotNull String lockId, int duration) {
        PlayerLockContainer container = playerLocks.computeIfAbsent(player, k -> new PlayerLockContainer());
        container.addOrUpdateLock(lockId, LockPriority.NORMAL.getPriority(), duration);
    }

    /**
     * 为玩家添加一个无持续时间的技能锁。此技能锁不会自动解除，需要手动解除
     *
     * @param player   玩家实体
     * @param lockId   技能锁ID
     * @param priority 技能锁优先级
     */
    public static synchronized void lock(@NotNull Player player, @NotNull String lockId, LockPriority priority) {
        PlayerLockContainer container = playerLocks.computeIfAbsent(player, k -> new PlayerLockContainer());
        container.addOrUpdateLock(lockId, priority.getPriority(), -1);
    }

    /**
     * 为玩家添加一个无持续时间的技能锁。此技能锁不会自动解除，需要手动解除
     * 默认优先级为NORMAL
     *
     * @param player 玩家实体
     * @param lockId 技能锁ID
     */
    public static synchronized void lock(@NotNull Player player, @NotNull String lockId) {
        PlayerLockContainer container = playerLocks.computeIfAbsent(player, k -> new PlayerLockContainer());
        container.addOrUpdateLock(lockId, LockPriority.NORMAL.getPriority(), -1);
    }

    /**
     * 移除玩家身上的一个技能锁
     *
     * @param player 玩家实体
     * @param lockId 技能锁ID
     */
    public static synchronized void unlock(@NotNull Player player, @NotNull String lockId) {
        PlayerLockContainer container = playerLocks.get(player);

        if (container != null) {
            container.removeLock(lockId);

            // 如果容器空了，就从 WeakHashMap 中移除
            if (container.isEmpty()) {
                playerLocks.remove(player);
            }
        }
    }

    /**
     * 获取玩家身上当前所有锁中的最高优先级
     *
     * @param player 玩家实体
     * @return 最高优先级
     */
    public static synchronized int getHighestLockPriority(@NotNull Player player) {
        PlayerLockContainer container = playerLocks.get(player);
        if (container == null) {
            return 0; // 未锁定
        }
        return container.getHighestPriority();
    }

    /**
     * 检查玩家是否被任意锁锁定
     *
     * @param player 玩家实体
     * @return 如果玩家被锁定则返回true，否则返回false
     */
    public static synchronized boolean isLocked(@NotNull Player player) {
        return getHighestLockPriority(player) > 0;
    }

    /**
     * 检查玩家是否被最低优先级或以上的锁锁定（包括等于最低优先级的锁）
     *
     * @param player   玩家实体
     * @param priority 最低优先级
     * @return 如果玩家被锁定则返回true，否则返回false
     */
    public static synchronized boolean isLocked(@NotNull Player player, LockPriority priority) {
        return getHighestLockPriority(player) >= priority.getPriority();
    }

    /**
     * 检查玩家是否被锁定（检测特定锁ID）
     *
     * @param player 玩家实体
     * @param lockId 技能锁ID
     * @return 如果玩家被锁定则返回true，否则返回false
     */
    public static synchronized boolean isLocked(@NotNull Player player, @NotNull String lockId) {
        PlayerLockContainer container = playerLocks.get(player);
        if (container == null) {
            return false;
        }
        LockData lockData = container.getData(lockId);
        return lockData != null && lockData.priority > 0;
    }

    /**
     * 由服务器调用的每tick更新方法，用于检查并移除过期的技能锁
     */
    public static synchronized void serverTick() {
        Iterator<Map.Entry<Player, PlayerLockContainer>> iterator = playerLocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Player, PlayerLockContainer> entry = iterator.next();
            Player player = entry.getKey();
            PlayerLockContainer container = entry.getValue();

            if (player == null || player.isRemoved()) {
                iterator.remove();
                continue;
            }

            // 调用容器的tick方法刷新其内部的锁
            container.tick();

            // 如果容器在 tick 后变空了，也将其移除
            if (container.isEmpty()) {
                iterator.remove();
            }
        }
    }
}