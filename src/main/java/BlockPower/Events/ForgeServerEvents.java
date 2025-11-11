package BlockPower.Events;

import BlockPower.Main.Main;
import BlockPower.Skills.MinerState.server.PlayerResourceManager;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.ModEffects.ModEffectManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeServerEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgeServerEvents.class);
    private static final TimerManager timerManager = TimerManager.getInstance(false);

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ModEffectManager.tickAll(false);
            SkillLockManager.serverTick();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PlayerResourceManager.getInstance().clear();

        LOGGER.info("PlayerResourceManager cleared");
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (timerManager.isTimerActive(player, "noFallDamage") && !timerManager.isFinished(player, "noFallDamage", true)) {
                event.setCanceled(true);
            }
        }
    }
}
