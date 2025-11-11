package BlockPower.Events;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Capability.PlayerAirJumpDataProvider;
import BlockPower.Main.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class ModCapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(ModCapabilities.PLAYER_AIR_JUMP_DATA).isPresent()) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "player_air_jump_data"), new PlayerAirJumpDataProvider());
            }
        }
    }
}
