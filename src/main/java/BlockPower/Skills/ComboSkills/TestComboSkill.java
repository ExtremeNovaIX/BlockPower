package BlockPower.Skills.ComboSkills;

import net.minecraft.world.entity.player.Player;

public class TestComboSkill implements IComboSkill {
    @Override
    public String getSkillName() {
        return "";
    }

    @Override
    public String getSkillDescription() {
        return "";
    }

    @Override
    public int getSkillLevel() {
        return 0;
    }

    @Override
    public void triggerSkill(Player player) {

    }

    @Override
    public boolean canTriggerSkill(int comboCount) {
        return false;
    }

    @Override
    public int getCooldownTick() {
        return 0;
    }

    @Override
    public int getComboWindowTick() {
        return 60;
    }

    @Override
    public String getTextureLocation() {
        return "textures/gui/combo_skills/magma_block_combo_skill.png";
    }

    @Override
    public double getSkillKBPercent() {
        return 0;
    }
}
