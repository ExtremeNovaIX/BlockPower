package BlockPower.Skills.SkillLock;

public enum LockPriority {
    HIGHEST(Integer.MAX_VALUE),
    NORMAL(100),
    MEDIUM(50),
    LOWEST(1);

    private final int priority;

    LockPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
