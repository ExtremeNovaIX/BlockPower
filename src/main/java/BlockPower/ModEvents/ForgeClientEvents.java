package BlockPower.ModEvents;

import BlockPower.Main.Main;
import BlockPower.ModItems.ModItems;
import BlockPower.Util.ClientComboManager;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {

    /**
     * 客户端事件：客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ModEffectManager.tickAll(true);
        }
    }

    /**
     * 客户端事件：渲染Tick事件
     */
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.RenderTickEvent.Phase.START) {
            ClientComboManager.handleCameraTick();
        }
    }
}