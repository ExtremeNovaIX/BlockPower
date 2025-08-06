package BlockPower.ModMessages;

import BlockPower.ModMessages.C2SPacket.SpawnDropAnvilPacket_C2S;
import BlockPower.ModMessages.C2SPacket.SpawnRushMinecartPacket_C2S;
import BlockPower.ModMessages.S2CPacket.HitStopPacket_S2C;
import BlockPower.ModMessages.S2CPacket.ShakePacket_S2C;
import BlockPower.ModMessages.S2CPacket.SneakPacket_S2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static BlockPower.Main.Main.MOD_ID;

/**
 * 这个类用于注册Mod的消息
 * 用于从客户端向服务端发送消息
 */
public class ModMessages {

    private static SimpleChannel INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ModMessages.class);

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

        //Server
        net.messageBuilder(SpawnDropAnvilPacket_C2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpawnDropAnvilPacket_C2S::new)
                .encoder(SpawnDropAnvilPacket_C2S::toBytes)
                .consumerMainThread(SpawnDropAnvilPacket_C2S::handle)
                .add();

        net.messageBuilder(SpawnRushMinecartPacket_C2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpawnRushMinecartPacket_C2S::new)
                .encoder(SpawnRushMinecartPacket_C2S::toBytes)
                .consumerMainThread(SpawnRushMinecartPacket_C2S::handle)
                .add();

        //Client
        net.messageBuilder(HitStopPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HitStopPacket_S2C::new)
                .encoder(HitStopPacket_S2C::toBytes)
                .consumerMainThread(HitStopPacket_S2C::handle)
                .add();

        net.messageBuilder(SneakPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SneakPacket_S2C::new)
                .encoder(SneakPacket_S2C::toBytes)
                .consumerMainThread(SneakPacket_S2C::handle)
                .add();

        net.messageBuilder(ShakePacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ShakePacket_S2C::new)
                .encoder(ShakePacket_S2C::toBytes)
                .consumerMainThread(ShakePacket_S2C::handle)
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

    // 一个辅助方法，用于从服务端向所有玩家发包
    public static <MSG> void sendToAllClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
