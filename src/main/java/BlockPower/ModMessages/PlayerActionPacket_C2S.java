package BlockPower.ModMessages;

import BlockPower.DTO.ActionData;
import com.google.gson.Gson;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static BlockPower.DTO.ActionData.MINECART_RUSH;
import static BlockPower.Entities.RushMinecart.RushMinecartEntity.createRushMinecart;

public class PlayerActionPacket_C2S {

    private static final Gson GSON = new Gson();
    private ActionData actionData;
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerActionPacket_C2S");

    // 构造函数：创建信件时，必须指明要执行哪个动作
    public PlayerActionPacket_C2S(ActionData actionData) {
        this.actionData = actionData;
    }

    public PlayerActionPacket_C2S(FriendlyByteBuf buf) {
        try {
            int length = buf.readVarInt();
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            // 3. 使用 Gson 进行解码 (byte[] -> String -> Object)
            String json = new String(bytes, StandardCharsets.UTF_8);
            this.actionData = GSON.fromJson(json, ActionData.class);
        } catch (Exception e) {
            LOGGER.error("使用Gson解码消息失败", e);
        }
    }

    // 编码器：将动作指令写入字节流
    public void toBytes(FriendlyByteBuf buf) {
        try {
            // 4. 使用 Gson 进行编码 (Object -> String -> byte[])
            String json = GSON.toJson(this.actionData);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            buf.writeVarInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (Exception e) {
            LOGGER.error("使用Gson编码消息失败", e);
            buf.writeVarInt(0); // 写入0长度表示失败，防止对面读取错误
        }
    }

    // 处理器：根据不同的动作指令，执行不同的逻辑
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            String action = actionData.getActionType();
            // 服务端逻辑
            ServerPlayer player = context.getSender();

            // 使用 switch 语句来分发任务
            switch (action) {
                case MINECART_RUSH:
                    // 执行“矿车冲刺”操作

                    if (player != null) {
                        createRushMinecart(player);
                    }
                    break;

            }
            LOGGER.info("服务端指令处理完成:{}", action);
        });
        return true;
    }
}