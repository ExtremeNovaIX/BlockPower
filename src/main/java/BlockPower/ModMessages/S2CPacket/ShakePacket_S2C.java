package BlockPower.ModMessages.S2CPacket;

import BlockPower.ModEffects.ScreenShakeEffect;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShakePacket_S2C extends AbstractS2CPacket {
    private int duration;
    private float strength;
    private static final Logger LOGGER = LoggerFactory.getLogger(ShakePacket_S2C.class);

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
        LOGGER.info("ShakePacket_S2C received, duration: {}, strength: {}", duration, strength);
        ModEffectManager.addEffect(Minecraft.getInstance().player, new ScreenShakeEffect(duration, strength));
    }
}
