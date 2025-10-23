package BlockPower.Skills.ComboSkills;

public enum ComboSkillType {
    MAGMA_BLOCK(new MagmaBlockComboSkill());

    private final ComboSkill skillInstance;

    ComboSkillType(ComboSkill skillInstance) {
        this.skillInstance = skillInstance;
    }

    public ComboSkill getSkill() {
        return this.skillInstance;
    }
}
