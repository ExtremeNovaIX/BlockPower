package BlockPower.Skills.ComboSkills;

public enum ComboSkillType {
    MAGMA_BLOCK(new MagmaBlockComboSkill()),
    TEST(new TestComboSkill()),
    FIRECRACKER(new FirecrackerComboSkill());

    private final IComboSkill skillInstance;

    ComboSkillType(IComboSkill skillInstance) {
        this.skillInstance = skillInstance;
    }

    public IComboSkill getSkill() {
        return this.skillInstance;
    }
}
