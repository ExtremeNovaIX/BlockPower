package BlockPower.ModMessages.C2SPacket;


import BlockPower.Entities.RushMinecart.RushMinecartEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.LoggerFactory;

public class SpawnRushMinecartPacket_C2S extends AbstractC2SPacket {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpawnRushMinecartPacket_C2S.class);

    public SpawnRushMinecartPacket_C2S() {
    }

    public SpawnRushMinecartPacket_C2S(FriendlyByteBuf buf) {
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        LOGGER.info("createRushMinecart by player: {}", player.getGameProfile().getName());
        RushMinecartEntity.createRushMinecart(player);
    }
}
