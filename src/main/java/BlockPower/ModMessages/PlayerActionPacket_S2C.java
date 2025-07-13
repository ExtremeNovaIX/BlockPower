package BlockPower.ModMessages;

import BlockPower.Util.ScreenShake.ScreenShakeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class PlayerActionPacket_S2C {

    private final ServerAction action;
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerActionPacket_S2C");

    // 构造函数：创建信件时，必须指明要执行哪个动作
    public PlayerActionPacket_S2C(ServerAction action) {
        this.action = action;
    }

    // 解码器：从字节流中读取是哪个动作
    public PlayerActionPacket_S2C(FriendlyByteBuf buf) {
        this.action = buf.readEnum(ServerAction.class);
    }

    // 编码器：将动作指令写入字节流
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
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
            if (mc.player == null) {
                LOGGER.warn("客户端玩家实体为空，无法处理动作：" + action);
                return;
            }

            switch (this.action) {
                case SHAKE:
                    LOGGER.info("客户端收到指令：SHAKE");
                    ScreenShakeHandler.shakeTrigger(5, 3f);
                    break;
                default:
                    LOGGER.warn("未知动作：" + action);
                    break;
            }
        });
        return true;
    }
}