package BlockPower.Util.ModEffects.ClientEffect;

import BlockPower.Util.ModEffects.ITickBasedEffect;
import net.minecraft.network.FriendlyByteBuf;

public interface IClientTickBasedEffect extends ITickBasedEffect {
    /**
     * 将此效果实例的特定数据写入数据包。
     * 用于服务器 -> 客户端的同步。
     */
    void writeToBuffer(FriendlyByteBuf buf);

    /**
     * 获取此效果的类型。
     * 用于在客户端识别和处理不同类型的效果。
     *
     * @return 此效果的类型
     */
    ClientEffectEnum getType();

    @Override
    default boolean isClientSide() {
        return true;
    }

}
