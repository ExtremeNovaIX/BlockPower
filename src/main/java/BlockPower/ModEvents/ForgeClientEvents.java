package BlockPower.ModEvents;

import BlockPower.Main.Main;
import BlockPower.Skills.ComboSkills.ComboManager.Client.ClientComboData;
import BlockPower.Util.ClientCameraTrackingManager;
import BlockPower.ModEffects.ModEffectManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {

    /**
     * 客户端事件：客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ModEffectManager.tickAll(true);
            ClientComboData.clientTick();
        }
    }

    /**
     * 客户端事件：渲染Tick事件
     */
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.RenderTickEvent.Phase.START) {
            ClientCameraTrackingManager.handleCameraTick();
        }
    }
}