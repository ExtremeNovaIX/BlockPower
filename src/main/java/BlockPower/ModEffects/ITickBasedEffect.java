package BlockPower.ModEffects;

public interface ITickBasedEffect {
    /**
     * 每个tick都调用一次处理相关逻辑
     */
    void tick();

    /**
     * 判断效果是否已经结束，可以被移除了
     * @return 如果效果结束则返回 true
     */
    boolean isFinished();

    /**
     * 判断效果是否是客户端生效
     * @return 如果是客户端效果则返回 true
     * 如果是服务端效果则返回 false
     */
    boolean isClientSide();
}
