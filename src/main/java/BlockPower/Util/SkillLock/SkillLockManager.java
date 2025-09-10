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
     * 为玩家设置一个技能锁
     * 该方法是线程安全的。
     *
     * @param player 要锁定的玩家
     * @param lock   技能锁对象，包含锁定时长等信息。如果不指定锁定时长，则需要手动解锁或等到最大锁定时间(15s)以后自然解锁
     * @throws SkillLockException 如果玩家已经被技能锁定，则抛出异常，此异常是提醒程序员不应该在玩家已经被技能锁定的情况下再次锁定玩家
     *
     */
    public synchronized void lock(Player player, SkillLock lock) {
        if (skillLockMap.containsKey(player)) {
            if (skillLockMap.get(player).isLocked()) {
                throw new SkillLockException("Player " + player.getName().getString() + " is already locked by a skill.");
            }
        }
        this.skillLockMap.put(player, lock);
        if(lock.getLockTime() > 0){
            taskManager.runTaskAfterTicks(lock.getLockTime(), () -> unlock(player));
        }else{
            taskManager.runTaskAfterTicks(SkillLock.MAX_LOCK_TICK, () -> unlock(player));
        }
    }

    /**
     * 移除玩家的技能锁。
     * 该方法是线程安全的。
     *
     * @param player 要解锁的玩家
     */
    public synchronized void unlock(Player player) {
        if (this.skillLockMap.containsKey(player)) {
            this.skillLockMap.get(player).setLocked(false);
        }
    }

    /**
     * 该方法是线程安全的。
     *
     * @param player 要检查的玩家
     * @return 如果玩家当前被技能锁定，则返回 true
     */
    public synchronized boolean isLocked(Player player) {
        if (this.skillLockMap.containsKey(player)) {
            return this.skillLockMap.get(player).isLocked();
        } else {
            this.skillLockMap.put(player, new SkillLock());
            return false;
        }
    }
}