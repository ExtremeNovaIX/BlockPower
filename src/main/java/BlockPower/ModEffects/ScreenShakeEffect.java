package BlockPower.ModEffects;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;

import java.util.Random;

/**
 * 屏幕震动，仅用于客户端
 */
@OnlyIn(Dist.CLIENT)
public class ScreenShakeEffect {
    private static final Random random = new Random();

    private static int shakeDuration = 0;

    private static float shakeStrength = 0.0f;

    /**
     * 引发屏幕震动
     *
     * @param duration 震动持续时间（tick）
     * @param strength 震动强度
     */
    public static void shakeTrigger(int duration,float strength) {
        shakeDuration = duration;
        shakeStrength = strength;
    }

    public static void handleScreenShake() {
        if (shakeDuration > 0) {
            shakeDuration--;
        } else {
            shakeStrength = 0.0f; //时间到，重置强度
        }
    }

    public static void applyScreenShakeIfActive(ViewportEvent.ComputeCameraAngles event) {
        if (shakeDuration > 0 && shakeStrength > 0) {
            float strength = shakeStrength;

            // 计算随机的角度偏移量
            float yawShake = strength * (random.nextFloat() * 2.0F - 1.0F);
            float pitchShake = strength * (random.nextFloat() * 2.0F - 1.0F);
            float rollShake = strength * (random.nextFloat() * 2.0F - 1.0F);

            // 在原有的摄像机角度上，加上抖动偏移量
            event.setYaw(event.getYaw() + yawShake);
            event.setPitch(event.getPitch() + pitchShake);
            event.setRoll(event.getRoll() + rollShake);
        }
    }
}
