package BlockPower.Util.SkillLock;

import BlockPower.ModException.SkillLockException;
import BlockPower.Util.TaskManager;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 玩家技能状态锁的管理类
 * 用于玩家释放技能时锁定其他部分技能，使其他部分技能无法使用
 * 技能锁的最大锁定时间为15s
 */
public class SkillLockManager {
    private static final SkillLockManager INSTANCE = new SkillLockManager();
    private final Map<Player, SkillLock> skillLockMap = new WeakHashMap<>();
    private final TaskManager taskManager = TaskManager.getInstance(false);

    private SkillLockManager() {
    }

    public static SkillLockManager getInstance() {
        return INSTANCE;
    }

    /**
     * 为玩家设置一个技能锁，不可覆盖lock方法设定的lock，也不会被overrideLock覆盖
     * 该方法是线程安全的。
     *
     * @param player   要锁定的玩家
     * @param lockTime 锁定时间，单位为tick
     * @throws SkillLockException 如果玩家已经被技能锁定，则抛出异常，此异常是提醒程序员不应该在玩家已经被技能锁定的情况下再次锁定玩家
     */
    public synchronized void lock(Player player, int lockTime) {
        if (skillLockMap.containsKey(player)) {
            if (skillLockMap.get(player).isLocked()) {
                throw new SkillLockException("Player " + player.getName().getString() + " is already locked , can not lock again.");
            }
        } else {
            skillLockMap.put(player, new SkillLock());
        }
        SkillLock skillLock = this.skillLockMap.get(player);
        skillLock.setLocked(true);
        skillLock.setLockTime(lockTime);
        if (lockTime > 0) {
            taskManager.runTaskAfterTicks(lockTime, () -> unlock(player));
        }
    }

    /**
     * 为玩家设置一个技能锁，不可覆盖lock方法设定的lock，也不会被overrideLock覆盖
     * 此方法不会自动解锁，必须要在技能配置类里手动解锁
     * 该方法是线程安全的。
     *
     * @param player 要锁定的玩家
     */
    public synchronized void lock(Player player) {
        lock(player, -1);
    }

    /**
     * 为玩家设置一个技能锁，能够覆盖之前的技能锁(无法覆盖通过lock方法设置的技能锁)
     * 该方法是线程安全的。
     *
     * @param player               要锁定的玩家
     * @param overrideableLockTime 锁定时间，单位为tick
     */
    public synchronized void overrideableLock(Player player, int overrideableLockTime) {
        if (!skillLockMap.containsKey(player)) {
            skillLockMap.put(player, new SkillLock());
        }
        SkillLock skillLock = this.skillLockMap.get(player);
        skillLock.setOverrideableLockTime(overrideableLockTime);
        skillLock.setOverrideableLocked(true);
        String taskID = player.getName().getString() + "overrideLock";
        if (overrideableLockTime > 0) {
            taskManager.runOverrideTaskAfterTicks(overrideableLockTime, () -> unlock(player), taskID);
        }
    }

    /**
     * 为玩家设置一个技能锁，能够覆盖之前的技能锁(无法覆盖通过lock方法设置的技能锁)
     * 此方法不会自动解锁，必须要在技能配置类里手动解锁
     * 该方法是线程安全的。
     *
     * @param player 要锁定的玩家
     */
    public synchronized void overrideableLock(Player player) {
        overrideableLock(player, -1);
    }

    /**
     * 移除玩家的技能锁。
     * 该方法是线程安全的。
     *
     * @param player 要解锁的玩家
     */
    public synchronized void unlock(Player player) {
        if (this.skillLockMap.containsKey(player)) {
            SkillLock skillLock = this.skillLockMap.get(player);
            skillLock.setLocked(false);
            skillLock.setLockTime(0);
            skillLock.setOverrideableLocked(false);
            skillLock.setOverrideableLockTime(0);
        }
    }

    /**
     * 该方法是线程安全的。
     *
     * @param player 要检查的玩家
     * @return 如果玩家当前技能已被锁定，则返回 true
     */
    public synchronized boolean isLocked(Player player) {
        if (this.skillLockMap.containsKey(player)) {
            SkillLock skillLock = this.skillLockMap.get(player);
            //lock的优先级高于overrideableLocked
            return skillLock.isLocked() || skillLock.isOverrideableLocked();
        }
        return false;
    }
}