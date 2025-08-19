package BlockPower.ModMessages.C2SPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static BlockPower.Skills.MinerState.server.MinerStateEvent.minerStateMap;


public class ChangeMinerStatePacket_C2S extends AbstractC2SPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeMinerStatePacket_C2S.class);

    public ChangeMinerStatePacket_C2S() {
    }

    public ChangeMinerStatePacket_C2S(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        boolean b = !minerStateMap.getOrDefault(player, false);
        minerStateMap.put(player, b);
        LOGGER.info("{} changed miner state to {}", player.getGameProfile().getName(), b);
    }
}
