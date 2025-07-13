package BlockPower.Util.ScreenShake;

import net.minecraft.world.entity.player.Player;

public class ScreenShakeHandler {

    public static int shakeDuration = 0;

    public static float shakeStrength = 0.0f;

    public static float k = 0.95f;

    public static Player player;

    /**
     * 引发屏幕震动
     *
     * @param duration  震动持续时间（tick）
     * @param strength  震动强度
     * @param decreaseK 震动强度数衰减线性系数
     */
    public static void shakeTrigger(Player p_player, int duration, float strength, float decreaseK) {
        shakeDuration = duration;
        shakeStrength = strength;
        k = decreaseK;
        player = p_player;
    }

    /**
     * 更新震动强度（线性衰减）
     *
     * @param k 线性衰减系数 (0 < k < 1)
     */
    public static void updateShake(float k) {
        if (shakeDuration > 0) {
            shakeStrength *= k;
            shakeDuration--;

            // 当强度低于阈值时提前结束
            if (shakeStrength < 0.01f) {
                shakeDuration = 0;
                shakeStrength = 0;
            }
        }
    }

    public static void tick() {
        if (shakeDuration > 0) {
            updateShake(k);
        } else {
            shakeStrength = 0.0f; //时间到，重置强度
        }
    }
}
