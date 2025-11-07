package BlockPower.Skills.ComboSkills;

import BlockPower.ModEntities.MagmaEntity;
import net.minecraft.world.entity.player.Player;

public class MagmaBlockComboSkill implements IComboSkill {
    @Override
    public String getSkillName() {
        return "MagmaBlockCombo";
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
        MagmaEntity.spawnMagmaEntity(player,this);
    }

    @Override
    public boolean canTriggerSkill(int comboCount) {
        return comboCount >= 4;
    }

    @Override
    public int getCooldownTick() {
        return 200;
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
        return 5;
    }

    public double getFireKBPercent() {
        return 0.5;
    }
}
