package BlockPower.Capability;

import BlockPower.Capability.AirJump.IPlayerAirJumpData;
import BlockPower.Capability.KBPercent.IKBPercent;
import BlockPower.Capability.MinerState.IPlayerMinerState;
import BlockPower.Main.Main;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    public static final Capability<IKBPercent> KNOCKBACK_VALUE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IPlayerAirJumpData> PLAYER_AIR_JUMP_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IPlayerMinerState> PLAYER_MINER_STATE = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IPixelCoreLevel> PIXEL_CORE_LEVEL = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IKBPercent.class);
        event.register(IPlayerAirJumpData.class);
        event.register(IPlayerMinerState.class);
        event.register(IPixelCoreLevel.class);
    }
}
