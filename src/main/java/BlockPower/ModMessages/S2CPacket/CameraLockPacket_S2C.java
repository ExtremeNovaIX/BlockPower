package BlockPower.ModMessages.S2CPacket;

import BlockPower.Util.ClientComboManager;
import net.minecraft.network.FriendlyByteBuf;

public class CameraLockPacket_S2C extends AbstractS2CPacket {
    private int entityId;

    public CameraLockPacket_S2C(int entityId) {
        this.entityId = entityId;
    }

    public CameraLockPacket_S2C(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    @Override
    protected void handleClientSide() {
        ClientComboManager.setCameraTarget(this.entityId);
    }
}
