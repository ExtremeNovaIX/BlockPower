package BlockPower.ModMessages.S2CPacket;

import BlockPower.ModEffects.PlayerSneakEffect;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SneakPacket_S2C extends AbstractS2CPacket {
    private boolean state;
    private static final Logger LOGGER = LoggerFactory.getLogger(SneakPacket_S2C.class);

    public SneakPacket_S2C(FriendlyByteBuf buf) {
        this.state = buf.readBoolean();
    }

    public SneakPacket_S2C(boolean state) {
        this.state = state;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.state);
    }

    @Override
    protected void handleClientSide() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (this.state) {
            ModEffectManager.addEffect(player, new PlayerSneakEffect());
        } else {
            ModEffectManager.removeEffect(player, PlayerSneakEffect.class);
        }
        LOGGER.info("SneakPacket_S2C: {} {}", this.state, player);
    }
}
