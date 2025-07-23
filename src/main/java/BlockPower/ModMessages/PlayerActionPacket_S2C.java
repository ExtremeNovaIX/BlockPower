package BlockPower.ModMessages;

import BlockPower.DTO.ActionData;
import BlockPower.DTO.S2C.ShakeData;
import BlockPower.Util.ScreenShake.ScreenShakeHandler;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static BlockPower.DTO.S2C.ShakeData.SHAKE_DATA;

public class PlayerActionPacket_S2C {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerActionPacket_S2C");
    private ActionData actionData;

    // 构造函数：创建信件时，必须要传入ModMessageDTO对象
    public PlayerActionPacket_S2C(ActionData actionData) {
        this.actionData = actionData;
    }

    // 解码器：从字节流中读取是哪个动作
    public PlayerActionPacket_S2C(FriendlyByteBuf buf) {
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

    /**
     * 处理器：客户端接收后，根据动作执行对应逻辑
     *
     * @param supplier 网络上下文
     * @return 是否成功处理
     */
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            // 客户端逻辑
            Minecraft mc = Minecraft.getInstance();
            String action = actionData.getActionType();

            if (mc.player == null) {
                LOGGER.warn("客户端玩家实体为空，无法处理动作：{}", action);
                return;
            }
            //TODO:修复震动失效问题
            //这里怎么都匹配不上
            if (action.trim().equals(SHAKE_DATA)) {
                ShakeData shakeData = (ShakeData) actionData;
                LOGGER.info("收到震动包 - duration: {}, strength: {}",
                        shakeData.getDuration(), shakeData.getStrength());
                ScreenShakeHandler.shakeTrigger(shakeData.getDuration(), shakeData.getStrength());
            } else if ("PLAY_SOUND".equals(action.trim())) {
                // 播放声音逻辑
            } else {
                LOGGER.warn("未知动作：[{}]", action);
            }
            LOGGER.info("客户端处理完成: {}", action);
        });
        return true;
    }
}