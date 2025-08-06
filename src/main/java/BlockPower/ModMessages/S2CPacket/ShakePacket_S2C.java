package BlockPower.ModMessages.S2CPacket;

import BlockPower.Effects.ScreenShake.ScreenShakeHandler;
import net.minecraft.network.FriendlyByteBuf;

public class ShakePacket_S2C extends AbstractS2CPacket {
    private int duration;
    private float strength;

    public ShakePacket_S2C(FriendlyByteBuf buf) {
        this.duration = buf.readInt();
        this.strength = buf.readFloat();
    }

    public ShakePacket_S2C(int duration, float strength) {
        this.duration = duration;
        this.strength = strength;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(duration);
        buf.writeFloat(strength);
    }

    @Override
    protected void handleClientSide() {
        ScreenShakeHandler.shakeTrigger(duration, strength);
    }
}
