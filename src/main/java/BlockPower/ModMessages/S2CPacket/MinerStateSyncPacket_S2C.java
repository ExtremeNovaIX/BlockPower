package BlockPower.ModMessages.S2CPacket;

import BlockPower.Skills.MinerState.client.ClientMinerState;
import net.minecraft.network.FriendlyByteBuf;

public class MinerStateSyncPacket_S2C extends AbstractS2CPacket {
    private final boolean isMinerMode;

    public MinerStateSyncPacket_S2C(FriendlyByteBuf buf) {
        this.isMinerMode = buf.readBoolean();
    }

    public MinerStateSyncPacket_S2C(boolean isMinerMode) {
        this.isMinerMode = isMinerMode;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isMinerMode);
    }

    @Override
    protected void handleClientSide() {
        ClientMinerState.setMinerState(this.isMinerMode);
    }
}
