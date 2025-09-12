package BlockPower.Util.SkillLock;

/**
 * 技能锁
 * 用于玩家释放技能时锁定其他部分技能，使其他部分技能无法使用
 */
public class SkillLock {
    private boolean locked;
    private int lockTime;
    public static final int MAX_LOCK_TICK = 300;//最大锁定时间15s

    /**
     * @param locked  是否锁定
     * @param lockTime 锁定时间，单位为tick
     */
    public SkillLock(boolean locked, int lockTime) {
        this.locked = locked;
        this.lockTime = lockTime;
    }

    public SkillLock() {
        this.locked = false;
        this.lockTime = 0;
    }

    /**
     * @param locked  是否锁定
     * 不指定锁定时间，需要手动解锁或等到最大锁定时间以后自然解锁
     */
    public SkillLock(boolean locked) {
        this.locked = locked;
        this.lockTime = 0;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getLockTime() {
        return lockTime;
    }

    public void setLockTime(int lockTime) {
        this.lockTime = lockTime;
    }

}
