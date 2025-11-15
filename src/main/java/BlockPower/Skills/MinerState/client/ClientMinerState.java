package BlockPower.Skills.MinerState.client;

/**
 * 客户端的MinerState激活状态存储。
 * 这是一个简单的静态类，其数据完全由服务器通过数据包驱动。
 */
public class ClientMinerState {

    private static boolean isMinerMode = false;

    /**
     * 由同步数据包调用，用于更新客户端的挖掘模式状态。
     * @param newIsMinerMode 最新的挖掘模式状态。
     */
    public static void setMinerMode(boolean newIsMinerMode) {
        isMinerMode = newIsMinerMode;
    }

    /**
     * 检查客户端是否处于挖掘模式。
     * @return 如果是，则返回true。
     */
    public static boolean isMinerMode() {
        return isMinerMode;
    }
}
