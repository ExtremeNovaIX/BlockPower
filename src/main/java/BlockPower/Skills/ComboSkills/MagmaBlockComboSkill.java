package BlockPower.Skills.ComboSkills;

import BlockPower.ModEntities.MagmaBlock.MagmaEntity;
import net.minecraft.world.entity.player.Player;

public class MagmaBlockComboSkill implements ComboSkill {
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
        MagmaEntity.spawnMagmaEntity(player);
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
        return 20;
    }

    @Override
    public String getTextureLocation() {
        return null;
    }
}
