package BlockPower.ModMessages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

import static BlockPower.Entities.Rush_Minecart.createRushMinecart;
import static BlockPower.Main.Main.sendDebugMessage;

// 通用数据包类
// 用于定义客户端和服务端之间的通用数据包类型
// 该类包含一个枚举类型 PlayerAction，用于定义客户端可以执行的不同操作
public class PlayerActionPacket_C2S {

    private final PlayerAction action;
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerActionPacket_C2S");

    // 构造函数：创建信件时，必须指明要执行哪个动作
    public PlayerActionPacket_C2S(PlayerAction action) {
        this.action = action;
    }

    // 解码器：从字节流中读取出是哪个动作
    public PlayerActionPacket_C2S(FriendlyByteBuf buf) {
        this.action = buf.readEnum(PlayerAction.class);
    }

    // 编码器：将动作指令写入字节流
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
    }

    // 处理器：根据不同的动作指令，执行不同的逻辑
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 服务端逻辑
            ServerPlayer player = context.getSender();

            // 使用 switch 语句来分发任务
            switch (this.action) {
                case MINECART_RUSH:
                    // 执行“矿车冲刺”操作
                    LOGGER.info("服务端受到指令：MINECART_RUSH");
                    if (player != null) {
                        sendDebugMessage(player, "服务端收到指令：MINECART_RUSH");
                            createRushMinecart(player);
                    }
                    break;
            }
        });
        return true;
    }
}