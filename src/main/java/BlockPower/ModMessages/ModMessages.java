package BlockPower.ModMessages;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static BlockPower.Main.Main.MOD_ID;

/**
 * 这个类用于注册Mod的消息
 * 用于从客户端向服务端发送消息
 */
public class ModMessages {

    private static SimpleChannel INSTANCE;

    //数据包的唯一ID,每个数据包类型都必须有一个不同的ID。
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    //定义通信频道
    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(PlayerActionPacket_C2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PlayerActionPacket_C2S::new)
                .encoder(PlayerActionPacket_C2S::toBytes)
                .consumerMainThread(PlayerActionPacket_C2S::handle)
                .add();

    }

    // 一个辅助方法，用于从客户端向服务端发包
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    // 一个辅助方法，用于从服务端向特定玩家发包
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
