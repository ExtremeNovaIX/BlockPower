package BlockPower.Client.Events;

import BlockPower.Client.gui.KBHud;
import BlockPower.Main.Main;
import BlockPower.Client.ComboSkills.ComboManager.Client.ClientComboData;
import BlockPower.Util.ClientCameraTrackingManager;
import BlockPower.Util.ModEffects.ModEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static BlockPower.Client.gui.KBHud.renderEntityKBHud;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    private static final RandomSource random = RandomSource.create();

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        KBHud.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        LivingEntity entity = event.getEntity();

        renderEntityKBHud(event, player, entity, minecraft);
    }

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
