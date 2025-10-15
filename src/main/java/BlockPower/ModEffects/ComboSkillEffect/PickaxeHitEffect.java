package BlockPower.ModEffects.ComboSkillEffect;

import BlockPower.ModEffects.*;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.ClientCameraTrackingManager;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffect.ModEffectManager;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PickaxeHitEffect implements ITickBasedEffect {
    private Entity target;
    private ServerPlayer player;
    private static final int MAX_DURATION = 20;
    private int nowTicks;

    private boolean flag = false;

    public PickaxeHitEffect(Entity target, ServerPlayer player) {
        this.target = target;
        this.player = player;
        nowTicks = 0;
    }

    @Override
    public void tick() {
        // 让玩家被目标吸引
        //TODO 没做好
        if (!flag) {
            SpringAttractionEffect effect = new SpringAttractionEffect(player, target);
            effect.setCOMBO_DURATION_TICKS(20);
            effect.setSPRING_CONSTANT(0.7);
            effect.setDEAD_ZONE(0.1);
            effect.setOPTIMAL_DISTANCE(0.1);
            ModEffectManager.addEffect(player, effect);
            flag = true;
        }
        // 增加tick计数器
        nowTicks++;
    }

    private boolean isEffectActive() {
        return !(target.isRemoved() && player.isRemoved());
    }

    @Override
    public boolean isFinished() {
        return nowTicks >= MAX_DURATION || isEffectActive();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void onEnd() {
        if (!isEffectActive()) return;
        //播放声音
        Commons.playSoundWithRandomPitch(player, ModSounds.HIT_SOUND.get());
        //应用伤害和击退
        target.hurt(player.level().damageSources().mobAttack(player), 5f);
        Commons.knockBackEntity(player, List.of(target), 1.2f);

        //应用卡肉效果
        ModEffectManager.addEffect(player, new HitStopEffect(16));

        //为目标实体添加失衡和云迹效果
        ModEffectManager.addEffect(target, new UnBalanceEffect(target, 9));
        ModEffectManager.addEffect(target, new CloudTrailEffect(target, 40));
    }
}
