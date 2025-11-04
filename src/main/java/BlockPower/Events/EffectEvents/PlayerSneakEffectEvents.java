package BlockPower.Events.EffectEvents;

import BlockPower.Main.Main;
import BlockPower.Util.ModEffects.ClientEffect.PlayerSneakEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerSneakEffectEvents {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        PlayerSneakEffect.handlePlayerSneakEffect(event);
    }
}
