package BlockPower.ModMessages.S2CPacket;

import BlockPower.ModEffects.HitStopEffect;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HitStopPacket_S2C extends AbstractS2CPacket {
    private final int duration;
    private static final Logger LOGGER = LoggerFactory.getLogger(HitStopPacket_S2C.class);

    public HitStopPacket_S2C(FriendlyByteBuf buf) {
        this.duration = buf.readInt();
    }

    public HitStopPacket_S2C(int duration) {
        this.duration = duration;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(duration);
    }

    @Override
    protected void handleClientSide() {
        LOGGER.info("HitStopPacket_S2C received, duration: {}", duration);
        HitStopEffect.start(duration);
    }
}
