package BlockPower.Util;

/**
 * 包含粒子系统所需的数学辅助函数。
 * 部分函数参考自 Mowzie's Mobs 源码。
 */
public final class MathUtils {

    /**
     * 圆周率 Pi
     */
    public static final float PI = (float) StrictMath.PI;

    /**
     * 圆周率的两倍 Tau (2 * Pi)
     */
    public static final float TAU = (float) (2 * StrictMath.PI);

    // 缓动函数
    /**
     * 慢速开始，快速结束 (t^5 曲线)。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (0.0 到 1.0)
     */
    public static float easeInQuint(float t) {
        return t * t * t * t * t;
    }

    /**
     * 快速开始，慢速结束 (1 - (1-t)^4 曲线)。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (0.0 到 1.0)
     */
    public static float easeOutQuart(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f * f;
    }

    /**
     * 快速开始，慢速结束 (二次曲线)。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (0.0 到 1.0)
     */
    public static float easeOutQuad(float t){
        float f = 1.0f - t;
        return 1.0f - f * f;
    }

    /**
     * 稍微超过终点然后回弹。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (在接近结束时可能略微超过 1.0)
     */
    public static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        float f = t - 1.0f;
        return 1 + c3 * f * f * f + c1 * f * f;
    }

    /**
     * 非常快速地开始，慢速结束 (基于 sqrt)。
     * 粒子系统需要此函数。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (0.0 到 1.0)
     */
    public static float easeOutCirc(float t) {
        float f = t - 1.0f;
        return (float) Math.sqrt(1 - f * f);
    }

    /**
     * 极慢速开始，极快速结束。
     * 粒子系统需要此函数。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (0.0 到 1.0)
     */
    public static float easeInExpo(float t) {
        return (float) (t == 0 ? 0 : Math.pow(2, 10 * t - 10));
    }

    /**
     * E极快速开始，极慢速结束。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (0.0 到 1.0)
     */
    public static float easeOutExpo(float t) {
        return (float) (t == 1 ? 1 : 1 - Math.pow(2, -10 * t));
    }

    /**
     * 快速开始，慢速结束 (三次曲线)。
     * 粒子系统需要此函数。
     * @param t 输入时间比例 (0.0 到 1.0)
     * @return 缓动后的值 (0.0 到 1.0)
     */
    public static float easeOutCubic(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }

    /**
     * 在两个值之间进行线性插值。
     * @param start 起始值
     * @param end 结束值
     * @param delta 插值因子 (0.0 到 1.0)
     * @return 插值结果
     */
    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

}