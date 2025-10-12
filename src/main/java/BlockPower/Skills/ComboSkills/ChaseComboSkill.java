package BlockPower.Skills.ComboSkills;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChaseComboSkill implements ComboSkill {

    @Override
    public String getSkillName() {
        return "Chase";
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
        //TODO 实现追逐连携技
        Level level = player.level();
        Vec3 pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        level.playSound(null,
                pos.x(), pos.y(), pos.z(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                0.7F, 1F);
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
}
