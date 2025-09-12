package BlockPower.ModEffects;

import BlockPower.Util.ModEffect.ModEffectManager;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;

import java.util.Random;

/**
 * 屏幕震动，仅用于客户端
 */
@OnlyIn(Dist.CLIENT)
public class ScreenShakeEffect implements ITickBasedEffect {
    private static final Random random = new Random();

    private static TickTimer timer;

    private static float shakeStrength = 0.0f;

    public ScreenShakeEffect(int duration, float strength) {
        timer = new TickTimer(duration, true);
        shakeStrength = strength;
    }

    public static void applyScreenShakeIfActive(ViewportEvent.ComputeCameraAngles event) {
        // 检查是否有屏幕震动效果
        if (ModEffectManager.getEntityEffect(Minecraft.getInstance().player, ScreenShakeEffect.class).isEmpty()) return;
        // 计算随机的角度偏移量
        float yawShake = shakeStrength * (random.nextFloat() * 2.0F - 1.0F);
        float pitchShake = shakeStrength * (random.nextFloat() * 2.0F - 1.0F);
        float rollShake = shakeStrength * (random.nextFloat() * 2.0F - 1.0F);

        // 在原有的摄像机角度上，加上抖动偏移量
        event.setYaw(event.getYaw() + yawShake);
        event.setPitch(event.getPitch() + pitchShake);
        event.setRoll(event.getRoll() + rollShake);
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean isFinished() {
        return timer.isFinished();
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
