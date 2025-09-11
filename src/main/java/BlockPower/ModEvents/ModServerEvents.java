package BlockPower.ModEvents;

import BlockPower.Main.Main;
import BlockPower.ModEffects.CloudTrailEffect;
import BlockPower.ModEffects.HitStopEffect;
import BlockPower.Skills.MinerState.server.PlayerResourceManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModServerEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModServerEvents.class);

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CloudTrailEffect.handleCloudParticleTimer();
            HitStopEffect.handleHitStopTimer();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PlayerResourceManager.getInstance().clear();
        LOGGER.info("PlayerResourceManager cleared");
    }
}
