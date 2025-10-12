package BlockPower.Skills.ComboSkills;

public enum ComboSkillType {
    CHASE(new ChaseComboSkill());

    private final ComboSkill skillInstance;

    ComboSkillType(ComboSkill skillInstance) {
        this.skillInstance = skillInstance;
    }

    public ComboSkill getSkill() {
        return this.skillInstance;
    }
}
