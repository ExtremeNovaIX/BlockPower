package BlockPower.ModMessages.C2SPacket;

import BlockPower.Entities.DropAnvil.DropAnvilEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnDropAnvilPacket_C2S extends AbstractC2SPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnDropAnvilPacket_C2S.class);

    public SpawnDropAnvilPacket_C2S(FriendlyByteBuf buf) {
    }

    public SpawnDropAnvilPacket_C2S() {
    }
    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        LOGGER.info("createDropAnvil by {}", player.getGameProfile().getName());
        DropAnvilEntity.createDropAnvil(player);
    }
}
