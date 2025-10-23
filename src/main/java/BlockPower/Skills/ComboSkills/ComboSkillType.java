package BlockPower.Skills.ComboSkills;

import net.minecraft.world.level.block.MagmaBlock;

public enum ComboSkillType {
    MAGMA_BLOCK(new MagmaBlockSkill());

    private final ComboSkill skillInstance;

    ComboSkillType(ComboSkill skillInstance) {
        this.skillInstance = skillInstance;
    }

    public ComboSkill getSkill() {
        return this.skillInstance;
    }
}
