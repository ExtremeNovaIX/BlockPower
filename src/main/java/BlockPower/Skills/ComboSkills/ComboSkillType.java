package BlockPower.Skills.ComboSkills;

public enum ComboSkillType {
    PICKAXE_HIT(new PickaxeHitComboSkill());

    private final ComboSkill skillInstance;

    ComboSkillType(ComboSkill skillInstance) {
        this.skillInstance = skillInstance;
    }

    public ComboSkill getSkill() {
        return this.skillInstance;
    }
}
