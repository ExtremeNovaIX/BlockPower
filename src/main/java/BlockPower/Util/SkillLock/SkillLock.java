package BlockPower.Util.SkillLock;

/**
 * 技能锁
 * 用于玩家释放技能时锁定其他部分技能，使其他部分技能无法使用
 */
public class SkillLock {
    private boolean locked;
    private int lockTime;
    private boolean overrideableLocked;
    private int overrideableLockTime;
    public static final int MAX_LOCK_TICK = 400;//最大锁定时间20s

    /**
     * @param locked  是否锁定
     * @param lockTime 锁定时间，单位为tick
     */
    public SkillLock(boolean locked, int lockTime) {
        this.locked = locked;
        this.lockTime = lockTime;
    }

    /**
     * 添加一个可被覆盖锁定的技能锁，不可覆盖lock方法设定的lock
     * @param overrideableLockTime 锁定时间，单位为tick
     * @param overrideableLocked 是否可被覆盖锁定
     */
    public SkillLock(int overrideableLockTime, boolean overrideableLocked) {
        this.overrideableLockTime = overrideableLockTime;
        this.overrideableLocked = overrideableLocked;
    }

    public SkillLock() {
        this.locked = false;
        this.lockTime = 0;
        this.overrideableLocked = false;
        this.overrideableLockTime = 0;
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

    public boolean isOverrideableLocked() {
        return overrideableLocked;
    }

    public void setOverrideableLocked(boolean overrideableLocked) {
        this.overrideableLocked = overrideableLocked;
    }

    public int getOverrideableLockTime() {
        return overrideableLockTime;
    }

    public void setOverrideableLockTime(int overrideableLockTime) {
        this.overrideableLockTime = overrideableLockTime;
    }
}
