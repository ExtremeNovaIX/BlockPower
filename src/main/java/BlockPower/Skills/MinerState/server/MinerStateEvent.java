package BlockPower.Skills.MinerState.server;

import BlockPower.ModEntities.FakeItem.FakeItem;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.ResourceSyncPacket_S2C;
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

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class MinerStateEvent {
    private static final TaskManager taskManager = TaskManager.getInstance(false);
    public static final Map<Player, Boolean> minerStateMap = new WeakHashMap<>();
    private static final Random random = new Random();
    private static final PlayerResourceManager resourceManager = PlayerResourceManager.getInstance();

    @SubscribeEvent
    public static void onBreakingBlock(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        // 确认玩家处于minerState状态
        if (minerStateMap.getOrDefault(player, false)) {
            if (player.level().isClientSide()) {
                event.setNewSpeed(0F);
            } else {
                event.setNewSpeed(0F);
                ResourceGenerationStrategy strategy = ResourceStrategyFactory.getStrategy(player.getMainHandItem());
                taskManager.runOnceWithCooldown(player, "minerState", strategy.getDigCoolDown(), () -> {
                    AllResourceType result = strategy.generateResource();
                    spawnSource(event, player, result);
                });
            }
        }
    }

    /**
     * 执行资源生成和视觉效果的核心逻辑。
     *
     * @param event  事件对象，用于获取位置信息。
     * @param player 触发事件的玩家。
     */
    private static void spawnSource(PlayerEvent.BreakSpeed event, Player player, AllResourceType result) {
        Level level = player.level();
        //TODO 添加高等级策略一次可以挖出双份甚至多份资源
        //TODO 添加对镐子耐久的消耗
        //TODO 对附魔的支持（效率，时运，耐久）

        // 数据更新：调用资源管理器，为玩家添加新获取的资源。
        var playerData = resourceManager.getPlayerData(player);
        playerData.addResource(result);

        // 当服务端数据更新后，发送数据包给客户端。
        if (player instanceof ServerPlayer serverPlayer) {
            Map<AllResourceType, Double> visualData = playerData.getResourceCounts();
            ModMessages.sendToPlayer(new ResourceSyncPacket_S2C(visualData), serverPlayer);
        }

        // 视觉与音效表现
        Vec3 position = event.getPosition().get().getCenter().add(0, 0.4, 0);
        Vec3 velocity = new Vec3(0, 0.35, 0);
        // 根据随机到的资源类型，创建一个对应的ItemStack用于显示。
        ItemStack displayStack = new ItemStack(result.getCorrespondingItem());
        FakeItem fakeItem = new FakeItem(level, position, velocity, displayStack, 6);
        level.addFreshEntity(fakeItem);

        // 根据不同资源类型获取不同的音效
        switch (result) {
            case DIRT:
            case WOOD:
            case STONE:
            case IRON:
            case GOLD:
                level.playSound(null,
                        position.x(), position.y(), position.z(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                        0.5F, random.nextFloat() * 0.1F + 0.9F);
                break;

            case DIAMOND:
                level.playSound(null,
                        position.x(), position.y(), position.z(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.7F, 1F);
                break;

            case NETHERITE:
                level.playSound(null,
                        position.x(), position.y(), position.z(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS,
                        1F, 1F);
                break;

            default:
                break;
        }
    }
}
