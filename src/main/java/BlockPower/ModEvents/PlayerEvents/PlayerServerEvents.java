package BlockPower.ModEvents.PlayerEvents;


import BlockPower.Main.Main;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerServerEvents {
    private static final TaskManager taskManager = TaskManager.getInstance();

    private static final TimerManager timerManager = TimerManager.getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

    private static final Map<Player, Integer> playerAirTicks = new WeakHashMap<>();//记录玩家滞空时间

    //TODO 为二段跳和冲刺加粒子和音效
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player p = event.player;
        if (p.level().isClientSide()) return;
        if (event.phase != TickEvent.Phase.END) return;
        ServerPlayer player = (ServerPlayer) p;
        if (Commons.isSpectatorOrCreativeMode(player)) return;
        handleAirJump(player);
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 检查玩家是否有免疫摔落伤害的任务
            if (timerManager.isTimerActive(player,"noFallDamage") && !timerManager.isFinished(player, "noFallDamage")) {
                event.setCanceled(true);
            }
        }
    }

    private static void handleAirJump(ServerPlayer player) {
        if (player.onGround() && taskManager.queryRemainExecutions(player, "airJump") == 0) {
            LOGGER.info("玩家 {} 落地,airJump flush", player.getGameProfile().getName());
            taskManager.flushTasks(player, "airJump");
        }

        if (player.onGround()) {
            // 如果玩家在地上，移除计时器
            playerAirTicks.remove(player);
        } else {
            // 如果玩家在空中，将计时器+1
            playerAirTicks.merge(player, 1, Integer::sum);
        }
    }

    /**
     * 获取玩家的滞空时长
     *
     * @param player 目标玩家
     * @return 玩家在空中的tick数，如果在地上则为0
     */
    public static int getPlayerAirTicks(Player player) {
        return playerAirTicks.getOrDefault(player, 0);
    }
}
