package BlockPower.Skills;

import net.minecraft.network.FriendlyByteBuf;

public interface IPacketSerializable {
    // 客户端调用：将技能的参数写入网络缓冲区
    void writeParams(FriendlyByteBuf buf);

    // 服务器端调用：从网络缓冲区读取参数，并配置自身状态
    void readParams(FriendlyByteBuf buf);
}
