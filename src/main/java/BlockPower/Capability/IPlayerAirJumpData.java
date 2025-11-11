package BlockPower.Capability;

/**
 * 该接口定义了玩家空中跳跃数据的能力。
 * 它用于存储和管理玩家在空中的跳跃次数。
 */
public interface IPlayerAirJumpData {
    /**
     * 获取当前的跳跃次数。
     * @return 当前的跳跃次数。
     */
    int getJumpCount();

    /**
     * 设置跳跃次数。
     * @param count 要设置的跳跃次数。
     */
    void setJumpCount(int count);

    /**
     * 将跳跃次数增加1。
     */
    void incrementJumpCount();

    /**
     * 重置跳跃次数为0。
     */
    void resetJumpCount();
}
