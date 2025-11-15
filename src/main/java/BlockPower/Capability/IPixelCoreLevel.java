package BlockPower.Capability;

/**
 * 该接口定义了像素核心（PixelCoreItem）的等级能力。
 * 它将附加到 ItemStack 上，用于存储其挖掘等级。
 */
public interface IPixelCoreLevel {
    /**
     * 获取核心的等级。
     * @return 当前等级。
     */
    int getLevel();

    /**
     * 设置核心的等级。
     * @param level 要设置的等级。
     */
    void setLevel(int level);

    /**
     * 将核心的等级提升一级。
     * 如果超过最大等级，则循环回1。
     * @return 提升后的新等级。
     */
    int upgradeLevel();
}
