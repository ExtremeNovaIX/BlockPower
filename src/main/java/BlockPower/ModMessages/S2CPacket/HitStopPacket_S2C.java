package BlockPower.ModMessages.S2CPacket;

import BlockPower.ModEffects.ClientEffect.HitStopEffect;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public class HitStopPacket_S2C extends AbstractS2CPacket {
    private final int duration;

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
        ModEffectManager.addEffect(Minecraft.getInstance().player, new HitStopEffect(duration));
    }
}
