package BlockPower.Skills;

import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.ServerTickListener;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.client.Timer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    @SubscribeEvent
    public static void handleMinerState(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (minerStateMap.getOrDefault(player, false)) {
            event.setCanceled(true);
            taskManager.runOnceWithCooldown(player, "minerState", 6, () -> {
                spawnSource(event, player);
            });
        }
    }

    private static void spawnSource(PlayerInteractEvent.LeftClickBlock event, Player player) {
        Level level = player.level();
        Vec3 position = event.getPos().getCenter();
        ItemEntity itemEntity = new ItemEntity(level, position.x(), position.y() + 0.3, position.z(), new ItemStack(Items.IRON_INGOT));
        itemEntity.setPickUpDelay(32767);
        if (!level.isClientSide()) {
            level.playSound(null, position.x(), position.y(), position.z(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 3F, random.nextFloat() * 0.1F + 0.9F);
        }
        taskManager.runTaskAfterTicks(6, () -> {
            if (!itemEntity.isRemoved()) {
                itemEntity.discard();
            }
        });
        level.addFreshEntity(itemEntity);
    }
}
