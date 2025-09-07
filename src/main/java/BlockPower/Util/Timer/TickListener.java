package BlockPower.Util.Timer;

import BlockPower.Util.TaskManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickListener {
    private static long serverTicks = 0;
    private static long clientTicks = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            serverTicks++;
            TaskManager.getInstance(false).updateTick();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            clientTicks++;
            TaskManager.getInstance(true).updateTick();
        }
    }

    public static long getServerTicks() {
        return serverTicks;
    }

    public static long getClientTicks() {
        return clientTicks;
    }
}
