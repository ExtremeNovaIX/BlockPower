package BlockPower.ModMessages.S2CPacket;

import BlockPower.Skills.MinerState.client.ClientResourceData;
import BlockPower.Skills.MinerState.server.AllResourceType;
import net.minecraft.network.FriendlyByteBuf;
import java.util.EnumMap;
import java.util.Map;

/**
 * 从服务端到客户端（S2C）的数据包，用于同步玩家的资源条视觉数据。
 */
public class ResourceSyncPacket_S2C extends AbstractS2CPacket {

    // 存储要同步的资源数据
    private final Map<AllResourceType, Double> resourceData;

    /**
     * 客户端接收并解码时使用的构造函数。
     * @param buf 包含网络数据的字节缓冲
     */
    public ResourceSyncPacket_S2C(FriendlyByteBuf buf) {
        // 从字节流中读取Map的大小
        int size = buf.readInt();
        // 创建一个新的EnumMap用于存放解码后的数据
        this.resourceData = new EnumMap<>(AllResourceType.class);
        // 根据大小循环读取每个键值对
        for (int i = 0; i < size; i++) {
            AllResourceType type = buf.readEnum(AllResourceType.class);
            double amount = buf.readDouble();
            this.resourceData.put(type, amount);
        }
    }

    /**
     * 服务端创建并发送时使用的构造函数。
     * @param resourceData 包含当前视觉资源数据的Map
     */
    public ResourceSyncPacket_S2C(Map<AllResourceType, Double> resourceData) {
        this.resourceData = resourceData;
    }

    /**
     * 将数据包内容写入字节流（编码）。
     * @param buf 字节缓冲
     */
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        // 首先写入Map的大小，方便客户端解码时循环
        buf.writeInt(this.resourceData.size());
        // 遍历Map，将每个键值对写入字节流
        for (Map.Entry<AllResourceType, Double> entry : this.resourceData.entrySet()) {
            buf.writeEnum(entry.getKey());      // 写入枚举类型的Key
            buf.writeDouble(entry.getValue());  // 写入双精度浮点数的Value
        }
    }

    /**
     * 在客户端线程处理接收到的数据包。
     * 这个方法由你的抽象类 AbstractS2CPacket 调用。
     */
    @Override
    protected void handleClientSide() {
        // 调用客户端缓存类，用接收到的新数据覆盖旧数据
        ClientResourceData.setResources(this.resourceData);
    }
}