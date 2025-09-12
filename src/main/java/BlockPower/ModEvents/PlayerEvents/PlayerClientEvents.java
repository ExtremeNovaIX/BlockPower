package BlockPower.ModEvents.PlayerEvents;

import BlockPower.Main.Main;
import BlockPower.ModEffects.PlayerSneakEffect;
import BlockPower.ModEffects.ScreenShakeEffect;
import BlockPower.Util.ClientComboManager;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ModEffectManager.tickAll(true);
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.RenderTickEvent.Phase.START) {
            ClientComboManager.handleCameraTick();
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        ScreenShakeEffect.applyScreenShakeIfActive(event);
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        PlayerSneakEffect.handlePlayerSneakEffect(event);
    }

}
