package BlockPower.Skills.MinerState.server;

import BlockPower.ModEntities.FakeItem.FakeItem;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.ResourceSyncPacket_S2C;
import BlockPower.Util.TaskManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class MinerStateEvent {
    private static final TaskManager taskManager = TaskManager.getInstance();
    public static final Map<Player, Boolean> minerStateMap = new WeakHashMap<>();
    private static final Random random = new Random();
    private static final PlayerResourceManager resourceManager = PlayerResourceManager.getInstance();

    @SubscribeEvent
    public static void handleMinerState(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (minerStateMap.getOrDefault(player, false)) {
            event.setCanceled(true);
            taskManager.runOnceWithCooldown(player, "minerState", 3, () -> {
                spawnSource(event, player);
            });
        }
    }

    /**
     * 执行资源生成和视觉效果的核心逻辑。
     *
     * @param event  事件对象，用于获取位置信息。
     * @param player 触发事件的玩家。
     */
    private static void spawnSource(PlayerInteractEvent.LeftClickBlock event, Player player) {
        Level level = player.level();

        // 资源生成：通过带权重的随机算法决定本次获得的资源类型。
        AllResourceType allResourceType = getRandomResourceType();

        // 数据更新：调用资源管理器，为玩家添加新获取的资源。
        var playerData = resourceManager.getPlayerData(player);
        playerData.addResource(allResourceType);

        // 当服务端数据更新后，发送数据包给客户端。
        if (player instanceof ServerPlayer serverPlayer) {
            Map<AllResourceType, Double> visualData = playerData.getResourceCounts();
            ModMessages.sendToPlayer(new ResourceSyncPacket_S2C(visualData), serverPlayer);
        }

        // 视觉与音效表现
        //TODO 根据不同资源类型获取不同的音效
        Vec3 position = event.getPos().getCenter().add(0, 0.4, 0);
        Vec3 velocity = new Vec3(0, 0.35, 0);
        // 根据随机到的资源类型，创建一个对应的ItemStack用于显示。
        ItemStack displayStack = new ItemStack(allResourceType.getCorrespondingItem());
        FakeItem fakeItem = new FakeItem(level, position, velocity, displayStack, 6);
        level.addFreshEntity(fakeItem);

        if (!level.isClientSide()) {
            level.playSound(null, position.x(), position.y(), position.z(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 3F, random.nextFloat() * 0.1F + 0.9F);
        }
    }

    /**
     * 根据预设的权重概率，随机获取一种资源类型。
     *
     * @return 随机选中的资源类型 (ResourceType)。
     */
    //TODO 根据工具不同使用不同的权重算法
    private static AllResourceType getRandomResourceType() {
        double chance = random.nextDouble();
        if (chance < 0.40) { // 40% 的概率落在 [0.0, 0.40)
            return AllResourceType.DIRT;
        } else if (chance < 0.70) { // 30% 的概率落在 [0.40, 0.70)
            return AllResourceType.WOOD;
        } else if (chance < 0.90) { // 20% 的概率落在 [0.70, 0.90)
            return AllResourceType.STONE;
        } else if (chance < 0.98) { // 8% 的概率落在 [0.90, 0.98)
            return AllResourceType.IRON;
        } else { // 剩下 2% 的概率落在 [0.98, 1.0)
            return AllResourceType.GOLD;
        }
    }
}
