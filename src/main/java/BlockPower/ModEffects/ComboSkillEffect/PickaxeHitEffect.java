package BlockPower.ModEffects.ComboSkillEffect;

import BlockPower.ModEffects.*;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.ClientCameraTrackingManager;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffect.EffectSender;
import BlockPower.Util.ModEffect.ModEffectManager;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PickaxeHitEffect implements ITickBasedEffect {
    private static final Logger log = LoggerFactory.getLogger(PickaxeHitEffect.class);
    private Entity target;
    private ServerPlayer player;
    private static final int MAX_DURATION = 20;
    private int nowTicks;

    private boolean flag;

    public PickaxeHitEffect(Entity target, ServerPlayer player) {
        this.target = target;
        this.player = player;
        nowTicks = 0;
        flag = false;
    }

    @Override
    public void tick() {
        // 让玩家被目标吸引
        //TODO 没做好，以后找时间加日志调试
        if (!flag) {
            SpringAttractionEffect effect = new SpringAttractionEffect(player, target);
            effect.setCOMBO_DURATION_TICKS(15);
            effect.setSPRING_CONSTANT(1);
            effect.setOPTIMAL_DISTANCE(0.5);
            effect.setMAX_COMBO_RANGE(11);
            ModEffectManager.addEffect(player, effect);
            flag = true;
        }
        // 增加tick计数器
        nowTicks++;
    }

    private boolean shouldEffectEndEarly() {
        return target.isRemoved() || !player.isAlive() || target.isRemoved();
    }

    @Override
    public boolean isFinished() {
        return nowTicks >= MAX_DURATION || shouldEffectEndEarly();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void onEnd() {
        if (shouldEffectEndEarly()) return;
        //播放声音
        Commons.playSoundWithRandomPitch(player, ModSounds.HIT_SOUND.get());
        //应用伤害和击退
        target.hurt(player.level().damageSources().mobAttack(player), 5f);
        Commons.knockBackEntity(player, List.of(target), 1.2f);

        //应用卡肉效果
        EffectSender.sendHitStop(5, player, target);

        //为目标实体添加失衡和云迹效果
        ModEffectManager.addEffect(target, new UnBalanceEffect(target, 9));
        ModEffectManager.addEffect(target, new CloudTrailEffect(target, 40));
    }
}
