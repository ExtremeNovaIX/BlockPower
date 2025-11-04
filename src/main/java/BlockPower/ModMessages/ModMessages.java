package BlockPower.ModMessages;

import BlockPower.ModMessages.C2SPacket.ChangeMinerStatePacket_C2S;
import BlockPower.ModMessages.ComboSkillPacket.ComboStandbyPacket_S2C;
import BlockPower.ModMessages.ComboSkillPacket.ComboTriggeredPacket_C2S;
import BlockPower.ModMessages.NormalSkillC2SPacket.*;
import BlockPower.ModMessages.S2CPacket.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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
        net.messageBuilder(NormalSkillPacket_C2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(NormalSkillPacket_C2S::new)
                .encoder(NormalSkillPacket_C2S::toBytes)
                .consumerMainThread(NormalSkillPacket_C2S::handle)
                .add();

        net.messageBuilder(ChangeMinerStatePacket_C2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ChangeMinerStatePacket_C2S::new)
                .encoder(ChangeMinerStatePacket_C2S::toBytes)
                .consumerMainThread(ChangeMinerStatePacket_C2S::handle)
                .add();

        net.messageBuilder(ComboTriggeredPacket_C2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ComboTriggeredPacket_C2S::new)
                .encoder(ComboTriggeredPacket_C2S::toBytes)
                .consumerMainThread(ComboTriggeredPacket_C2S::handle)
                .add();

        //Client
        net.messageBuilder(EffectAddSyncPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EffectAddSyncPacket_S2C::new)
                .encoder(EffectAddSyncPacket_S2C::toBytes)
                .consumerMainThread(EffectAddSyncPacket_S2C::handle)
                .add();
        net.messageBuilder(EffectRemoveSyncPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EffectRemoveSyncPacket_S2C::new)
                .encoder(EffectRemoveSyncPacket_S2C::toBytes)
                .consumerMainThread(EffectRemoveSyncPacket_S2C::handle)
                .add();
        net.messageBuilder(HitStopPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HitStopPacket_S2C::new)
                .encoder(HitStopPacket_S2C::toBytes)
                .consumerMainThread(HitStopPacket_S2C::handle)
                .add();
        net.messageBuilder(ComboStandbyPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ComboStandbyPacket_S2C::new)
                .encoder(ComboStandbyPacket_S2C::toBytes)
                .consumerMainThread(ComboStandbyPacket_S2C::handle)
                .add();

        net.messageBuilder(FireworkPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FireworkPacket_S2C::new)
                .encoder(FireworkPacket_S2C::toBytes)
                .consumerMainThread(FireworkPacket_S2C::handle)
                .add();

        net.messageBuilder(CameraLockPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CameraLockPacket_S2C::new)
                .encoder(CameraLockPacket_S2C::toBytes)
                .consumerMainThread(CameraLockPacket_S2C::handle)
                .add();

        net.messageBuilder(ResourceSyncPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResourceSyncPacket_S2C::new)
                .encoder(ResourceSyncPacket_S2C::toBytes)
                .consumerMainThread(ResourceSyncPacket_S2C::handle)
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

        net.messageBuilder(MinerStateSyncPacket_S2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MinerStateSyncPacket_S2C::new)
                .encoder(MinerStateSyncPacket_S2C::toBytes)
                .consumerMainThread(MinerStateSyncPacket_S2C::handle)
                .add();

        net.messageBuilder(SyncKnockbackValueS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncKnockbackValueS2CPacket::new)
                .encoder(SyncKnockbackValueS2CPacket::toBytes)
                .consumerMainThread(SyncKnockbackValueS2CPacket::handle)
                .add();
    }


    // 一个辅助方法，用于从客户端向服务端发包
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
        LOGGER.info("Server received: {}", message);
    }

    // 一个辅助方法，用于从服务端向特定玩家发包
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
        LOGGER.info("Sent packet to player {}: {}", player.getGameProfile().getName(), message);
    }

    // 一个辅助方法，用于从服务端向所有玩家发包
    public static <MSG> void sendToAllClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
        LOGGER.info("Sent packet to all clients: {}", message);
    }

    // 一个辅助方法，用于从服务端向特定坐标一定范围内的所有玩家发包
    public static <MSG> void sendToAllAround(MSG message, ResourceKey<Level> dimension, double x, double y, double z, double radius) {
        PacketDistributor.TargetPoint target = new PacketDistributor.TargetPoint(x, y, z, radius, dimension);
        INSTANCE.send(PacketDistributor.NEAR.with(() -> target), message);
        LOGGER.info("Sent packet to all around (dim: {}, pos: {},{},{}, radius: {}): {}", dimension.location(), x, y, z, radius, message);
    }
}
