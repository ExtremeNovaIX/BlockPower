package BlockPower.Events.SkillEvents;

import BlockPower.Capability.IPlayerMinerState;
import BlockPower.Capability.ModCapabilities;
import BlockPower.ModEntities.FakeItem;
import BlockPower.Skills.MinerState.client.ClientMinerState;
import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;
import BlockPower.Skills.MinerState.server.strategy.ResourceStrategyFactory;
import BlockPower.Util.TaskManager;
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
            player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(minerState -> {
                if (minerState.isMinerMode()) {
                    event.setNewSpeed(0F);
                    ResourceGenerationStrategy strategy = ResourceStrategyFactory.getStrategy(player.getMainHandItem());
                    taskManager.runOnceWithCooldown(player, "minerState", strategy.getDigCoolDown(), () -> {
                        ResourceType result = strategy.generateResource();
                        spawnSource(event, (ServerPlayer) player, result, minerState);
                    });
                }
            });
        }
    }

    private static void spawnSource(PlayerEvent.BreakSpeed event, ServerPlayer player, ResourceType result, IPlayerMinerState minerState) {
        Level level = player.level();
        
        // 数据更新：调用Capability的方法，并传入player以触发同步
        minerState.addResource(result, player);

        // 视觉与音效表现
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
