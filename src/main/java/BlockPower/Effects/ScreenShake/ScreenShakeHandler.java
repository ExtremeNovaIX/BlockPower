package BlockPower.Effects.ScreenShake;

public class ScreenShakeHandler {

    public static int shakeDuration = 0;

    public static float shakeStrength = 0.0f;

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

    public static void tick() {
        if (shakeDuration > 0) {
            shakeDuration--;
        } else {
            shakeStrength = 0.0f; //时间到，重置强度
        }
    }
}
