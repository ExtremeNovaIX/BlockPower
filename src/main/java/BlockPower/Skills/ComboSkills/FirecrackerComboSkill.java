package BlockPower.Skills.ComboSkills;

import BlockPower.ModEntities.FirecrackerEntity;
import net.minecraft.world.entity.player.Player;

public class FirecrackerComboSkill implements IComboSkill {
    @Override
    public String getSkillName() {
        return "Firecracker";
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
        FirecrackerEntity.spawnFirecrackerEntity(player);
    }

    @Override
    public boolean canTriggerSkill(int comboCount) {
        return comboCount == 1;
    }

    @Override
    public int getCooldownTick() {
        return 400;
    }

    @Override
    public int getComboWindowTick() {
        return 60;
    }

    @Override
    public String getTextureLocation() {
        return "textures/gui/combo_skills/firecracker_combo_skill.png";
    }
}
