package BlockPower.Events.SkillEvents;

import BlockPower.Capability.IPixelCoreLevel;
import BlockPower.Capability.MinerState.IPlayerMinerState;
import BlockPower.Capability.ModCapabilities;
import BlockPower.ModEntities.FakeItem;
import BlockPower.ModItems.ModItems;
import BlockPower.ModItems.PixelCore.PixelCoreSkillState;
import BlockPower.Skills.MinerState.client.ClientMinerState;
import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;
import BlockPower.Skills.MinerState.server.strategy.ResourceStrategyFactory;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class MinerStateEvent {
    private static final TaskManager taskManager = TaskManager.getInstance(false);
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onBreakingBlock(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            if (ClientMinerState.isMinerMode()) {
                event.setNewSpeed(0F);
            }
        } else {
            if (player.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) {
                return;
            }

            player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(minerState -> {
                if (minerState.isMinerMode()) {
                    event.setNewSpeed(0F);

                    player.getMainHandItem().getCapability(ModCapabilities.PIXEL_CORE_LEVEL).ifPresent(coreLevel -> {
                        int level = coreLevel.getLevel();
                        ResourceGenerationStrategy strategy = ResourceStrategyFactory.getStrategy(level);
                        Commons.changePixelCoreNBT(player, PixelCoreSkillState.TOOL, 2.0f, (float) level);

                        taskManager.runOnceWithCooldown(player, "minerState", strategy.getDigCoolDown(), () -> {
                            ResourceType result = strategy.generateResource();
                            spawnSource(event, (ServerPlayer) player, result, minerState, coreLevel);
                        });
                    });
                }
            });
        }
    }

    private static void spawnSource(PlayerEvent.BreakSpeed event, ServerPlayer player, ResourceType result, IPlayerMinerState minerState, IPixelCoreLevel coreLevel) {
        Level level = player.level();

        minerState.addResource(result, player);

        // 自动升级逻辑（暂定）
        int resourceLevel = result.getLevel();
        int currentCoreLevel = coreLevel.getLevel();
        if (resourceLevel > currentCoreLevel) {
            coreLevel.setLevel(resourceLevel);
        }

        Vec3 position = event.getPosition().get().getCenter().add(0, 0.4, 0);
        Vec3 velocity = new Vec3(0, 0.35, 0);
        ItemStack displayStack = new ItemStack(result.getCorrespondingItem());
        FakeItem fakeItem = new FakeItem(level, position, velocity, displayStack, 6);
        level.addFreshEntity(fakeItem);

        switch (result) {
            case DIRT:
            case WOOD:
            case STONE:
            case IRON:
            case GOLD:
                level.playSound(null, position.x(), position.y(), position.z(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, random.nextFloat() * 0.1F + 0.9F);
                break;
            case DIAMOND:
                level.playSound(null, position.x(), position.y(), position.z(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1F);
                break;
            case NETHERITE:
                level.playSound(null, position.x(), position.y(), position.z(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1F, 1F);
                break;
            default:
                break;
        }
    }
}
