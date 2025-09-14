package BlockPower.ModEvents.SkillEvents;


import BlockPower.Main.Main;
import BlockPower.Skills.AirJumpSkill;
import BlockPower.Util.Commons;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AirJumpSkillEvents {
    private static final TimerManager timerManager = TimerManager.getInstance(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player p = event.player;
        if (p.level().isClientSide()) return;
        if (event.phase != TickEvent.Phase.END) return;
        ServerPlayer player = (ServerPlayer) p;
        if (Commons.isSpectatorOrCreativeMode(player)) return;

        AirJumpSkill.handleAirJump(player);
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 检查玩家是否有免疫摔落伤害的任务
            if (timerManager.isTimerActive(player,"noFallDamage") && !timerManager.isFinished(player, "noFallDamage",true)) {
                event.setCanceled(true);
            }
        }
    }
}
