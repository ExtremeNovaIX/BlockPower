package BlockPower.Events;

import BlockPower.Capability.AirJump.PlayerAirJumpDataProvider;
import BlockPower.Capability.MinerState.PlayerMinerStateProvider;
import BlockPower.Capability.ModCapabilities;
import BlockPower.Capability.PixelCoreLevelProvider;
import BlockPower.Main.Main;
import BlockPower.ModItems.ModItems;
import BlockPower.Skills.MinerState.server.ResourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumMap;

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
    public static void onAttachCapabilitiesItem(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == ModItems.PIXEL_CORE.get()) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "pixel_core_level"), new PixelCoreLevelProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(state -> state.syncToClient(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 确认玩家是因死亡而重生
        if (!event.isWasDeath()) {
            return;
        }

        // 从新的玩家实体上获取能力
        Player newPlayer = event.getEntity();

        // 清空资源
        newPlayer.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(newState -> {
            newState.setResourceCounts(new EnumMap<>(ResourceType.class));
            if (newPlayer instanceof ServerPlayer serverPlayer) {
                newState.setMinerModeFromNBT(false);
                newState.syncToClient(serverPlayer);
            }
        });

        // 重置所有像素核心的等级
        for (int i = 0; i < newPlayer.getInventory().getContainerSize(); i++) {
            ItemStack stack = newPlayer.getInventory().getItem(i);
            if (stack.getItem() == ModItems.PIXEL_CORE.get()) {
                stack.getCapability(ModCapabilities.PIXEL_CORE_LEVEL).ifPresent(coreLevel -> {
                    coreLevel.setLevel(1);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(state -> state.syncToClient(player));
        }
    }
}
