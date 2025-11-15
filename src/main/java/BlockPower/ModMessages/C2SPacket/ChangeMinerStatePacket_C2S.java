package BlockPower.ModMessages.C2SPacket;

import BlockPower.Capability.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // 从Capability系统中获取当前的MinerState
        player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(minerState -> {
            // 计算新的状态并设置
            boolean newState = !minerState.isMinerMode();
            minerState.setMinerMode(newState, player);
            LOGGER.info("{} changed miner state to {}", player.getGameProfile().getName(), newState);
        });
    }
}
