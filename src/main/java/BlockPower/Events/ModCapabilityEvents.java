package BlockPower.Events;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Capability.PlayerAirJumpDataProvider;
import BlockPower.Capability.PlayerMinerStateProvider;
import BlockPower.Main.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
            if (!event.getObject().getCapability(ModCapabilities.PLAYER_MINER_STATE).isPresent()) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "player_miner_state"), new PlayerMinerStateProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 玩家登录时，将服务器的MinerState数据同步到客户端
            player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(state -> state.syncToClient(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 玩家重生时，将服务器的MinerState数据同步到客户端
            player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(state -> state.syncToClient(player));
        }
    }
}
