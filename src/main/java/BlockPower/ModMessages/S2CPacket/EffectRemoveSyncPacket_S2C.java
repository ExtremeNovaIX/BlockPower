package BlockPower.ModMessages.S2CPacket;

import BlockPower.ModEffects.ClientEffect.ClientEffectEnum;
import BlockPower.ModEffects.ClientEffect.IClientTickBasedEffect;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectRemoveSyncPacket_S2C extends AbstractS2CPacket {

    private static final Logger log = LoggerFactory.getLogger(EffectRemoveSyncPacket_S2C.class);
    private final ClientEffectEnum effectType;

    // 服务端构造函数
    public EffectRemoveSyncPacket_S2C(Class<? extends IClientTickBasedEffect> clazz) {
        this.effectType = ClientEffectEnum.findFromClass(clazz);
    }

    // 客户端构造函数
    public EffectRemoveSyncPacket_S2C(FriendlyByteBuf buf) {
        this.effectType = buf.readEnum(ClientEffectEnum.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(effectType);
    }

    @Override
    protected void handleClientSide() {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            log.error("Local player is null when handling EffectRemoveSyncPacket_S2C");
            return;
        }
        ModEffectManager.removeEffect(localPlayer, effectType.getEffectClass());
    }
}
