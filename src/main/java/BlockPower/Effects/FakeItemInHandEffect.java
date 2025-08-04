package BlockPower.Effects;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [仅服务端] 一个通过临时替换玩家主手物品来实现视觉动画的处理器。
 * 使用 ItemStack.copy() 来确保安全暂存和恢复带有NBT的Mod物品。
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FakeItemInHandEffect {

    private static final Map<UUID, Map.Entry<ItemStack, Integer>> effectDataMap = new ConcurrentHashMap<>();

    private FakeItemInHandEffect() {
    }

    /**
     * 让指定玩家播放一次手持指定物品并挥动的动画。
     *
     * @param player        目标玩家。
     * @param itemToDisplay 要在动画中临时显示的物品。
     * @param durationTicks 效果持续时间(tick)，之后会自动恢复原物品。
     */
    public static void playItemAnimation(ServerPlayer player, ItemStack itemToDisplay, int durationTicks) {
        // 如果该玩家的动画正在播放中，则不执行任何操作，防止重复触发
        if (effectDataMap.containsKey(player.getUUID())) {
            return;
        }

        // 获取玩家主手物品，并使用 .copy() 创建一个完整的数据快照
        ItemStack originalItemSnapshot = player.getMainHandItem().copy();

        // 将快照和持续时间存入Map中
        Map.Entry<ItemStack, Integer> effectData = new AbstractMap.SimpleEntry<>(originalItemSnapshot, durationTicks);
        effectDataMap.put(player.getUUID(), effectData);

        // 在服务端将玩家主手上的物品临时设置为新物品
        player.setItemInHand(InteractionHand.MAIN_HAND, itemToDisplay.copy());

        // 播放主手的挥动动画
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    /**
     * 监听服务端的Tick事件，驱动我们的计时和恢复逻辑。
     * @param event a {@link TickEvent.ServerTickEvent} object.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tick(event.getServer());
        }
    }

    /**
     * 在每个服务端tick被调用，用于更新计时器和恢复物品。
     * @param server a {@link MinecraftServer} object.
     */
    private static void tick(MinecraftServer server) {
        if (effectDataMap.isEmpty()) {
            return;
        }

        // 使用迭代器来安全地遍历和移除Map中的元素
        for (Iterator<Map.Entry<UUID, Map.Entry<ItemStack, Integer>>> it = effectDataMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Map.Entry<ItemStack, Integer>> entry = it.next();
            UUID playerUUID = entry.getKey();
            int ticksLeft = entry.getValue().getValue() - 1;

            if (ticksLeft <= 0) {
                // 时间到，恢复玩家的物品
                ServerPlayer playerToRestore = server.getPlayerList().getPlayer(playerUUID);
                if (playerToRestore != null) {
                    ItemStack originalItemSnapshot = entry.getValue().getKey();
                    // 将玩家手中的物品恢复为之前保存的“快照”
                    playerToRestore.setItemInHand(InteractionHand.MAIN_HAND, originalItemSnapshot);
                }
                // 从Map中移除该玩家，表示效果已结束
                it.remove();
            } else {
                // 更新剩余时间
                entry.getValue().setValue(ticksLeft);
            }
        }
    }
}