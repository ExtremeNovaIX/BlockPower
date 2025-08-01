package BlockPower.ModMessages;

import BlockPower.DTO.ActionData;
import BlockPower.DTO.C2S.MinecartData;
import BlockPower.Entities.RushMinecart.RushMinecartEntity;
import BlockPower.Util.Gson.ModGson;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;


public class PlayerActionPacket_C2S {

    private final ActionData data;
    private static final Gson GSON = ModGson.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerActionPacket_C2S");

    // 构造函数：创建信件时，必须指明要执行哪个动作
    public PlayerActionPacket_C2S(ActionData data) {
        this.data = data;
    }

    // 解码器 (接收时使用): 从字节流中恢复出 ActionData 对象
    public PlayerActionPacket_C2S(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        String json = buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
        // GSON会根据json中的"actionType"字段，自动创建出正确的子类对象，例如 MinecartRushData
        this.data = GSON.fromJson(json, ActionData.class);
    }

    // 编码器 (发送时使用): 将 ActionData 对象打包成字节流
    public void toBytes(FriendlyByteBuf buf) {
        String json = GSON.toJson(this.data, ActionData.class);
        buf.writeUtf(json);
    }

    // 处理器：根据不同的动作指令，执行不同的逻辑
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 获取发送方玩家
            ServerPlayer player = context.getSender();
            if (player == null) {
                LOGGER.warn("无法获取发送方玩家");
                return;
            }

            // 检查接收到的数据是否为空
            if (this.data == null) {
                LOGGER.warn("从玩家 {} 收到了一个空的C2S数据包", player.getGameProfile().getName());
                return;
            }

            if (this.data instanceof MinecartData) {
                LOGGER.info("服务端收到来自 {} 的指令：MINECART_RUSH", player.getGameProfile().getName());
                RushMinecartEntity.createRushMinecart(player);
            }
            // else if (this.data instanceof AnotherC2SData anotherData) {
            //     // 如果未来有其他C2S动作，在这里继续添加 else if

            else {
                // 如果收到了一个未知的动作类型
                LOGGER.warn("收到了来自 {} 的未知C2S动作类型: {}", player.getGameProfile().getName(), this.data.getClass().getName());
            }
        });
        return true;
    }
}