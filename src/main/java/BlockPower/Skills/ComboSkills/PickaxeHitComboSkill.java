package BlockPower.Skills.ComboSkills;

import BlockPower.ModEffects.ComboSkillEffect.PickaxeHitEffect;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PickaxeHitComboSkill implements ComboSkill {

    @Override
    public String getSkillName() {
        return "PickaxeHit";
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
        //TODO 实现PickaxeHit连携技
        Level level = player.level();
        Vec3 pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        level.playSound(null,
                pos.x(), pos.y(), pos.z(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                0.5F, 1F);
        // 改变玩家像素核心，显示镐
        //TODO 后续需要从玩家能力系统获取等级
        Commons.changePixelCoreNBT(player, 3.0f, 2.0f, 1.0f);

        // 检测7格内的实体作为目标
        Entity target = Commons.detectEntity(player, 7, null).get(0);
        // 应用镐击效果
        ModEffectManager.addEffect(player, new PickaxeHitEffect(target, (ServerPlayer) player));
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
