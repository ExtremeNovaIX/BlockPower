package BlockPower.ModMessages.S2CPacket;

import BlockPower.Skills.MinerState.client.ClientMinerState;
import BlockPower.Skills.MinerState.client.ClientResourceData;
import BlockPower.Skills.MinerState.server.ResourceType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.EnumMap;
import java.util.Map;

/**
 * 从服务端到客户端（S2C）的数据包，用于同步玩家完整的MinerState数据。
 */
public class ResourceSyncPacket_S2C extends AbstractS2CPacket {

    private final boolean isMinerMode;
    private final Map<ResourceType, Double> resourceData;

    /**
     * 客户端接收并解码时使用的构造函数。
     */
    public ResourceSyncPacket_S2C(FriendlyByteBuf buf) {
        this.isMinerMode = buf.readBoolean();
        int size = buf.readInt();
        this.resourceData = new EnumMap<>(ResourceType.class);
        for (int i = 0; i < size; i++) {
            ResourceType type = buf.readEnum(ResourceType.class);
            double amount = buf.readDouble();
            this.resourceData.put(type, amount);
        }
    }

    /**
     * 服务端创建并发送时使用的构造函数。
     */
    public ResourceSyncPacket_S2C(boolean isMinerMode, Map<ResourceType, Double> resourceData) {
        this.isMinerMode = isMinerMode;
        this.resourceData = resourceData;
    }

    /**
     * 将数据包内容写入字节流（编码）。
     */
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isMinerMode);
        buf.writeInt(this.resourceData.size());
        for (Map.Entry<ResourceType, Double> entry : this.resourceData.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeDouble(entry.getValue());
        }
    }

    /**
     * 在客户端线程处理接收到的数据包。
     */
    @Override
    protected void handleClientSide() {
        // 将数据正确分发给两个独立的客户端缓存类
        ClientMinerState.setMinerMode(this.isMinerMode);
        ClientResourceData.setResources(this.resourceData);
    }
}
