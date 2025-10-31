package BlockPower.ModMessages.S2CPacket;

import BlockPower.ModEffects.ClientEffect.ClientEffectEnum;
import BlockPower.ModEffects.ClientEffect.IClientTickBasedEffect;
import BlockPower.ModEffects.ModEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端效果添加同步包，用于服务器向客户端同步添加效果。
 */
public class EffectAddSyncPacket_S2C extends AbstractS2CPacket {

    private static final Logger log = LoggerFactory.getLogger(EffectAddSyncPacket_S2C.class);
    private final ClientEffectEnum effectType;
    private final IClientTickBasedEffect effectInstance;

    // 服务端构造函数
    public EffectAddSyncPacket_S2C(IClientTickBasedEffect effect) {
        this.effectType = effect.getType();
        this.effectInstance = effect;
    }

    // 客户端构造函数
    public EffectAddSyncPacket_S2C(FriendlyByteBuf buf) {
        this.effectType = buf.readEnum(ClientEffectEnum.class);
        this.effectInstance = this.effectType.createEffect(buf);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(effectType);
        effectInstance.writeToBuffer(buf);
    }

    @Override
    protected void handleClientSide() {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            log.error("Local player is null when handling EffectAddSyncPacket_S2C");
            return;
        }
        ModEffectManager.addEffect(localPlayer, this.effectInstance);
    }
}
