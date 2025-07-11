package BlockPower.Util.ScreenShake;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import BlockPower.Main.Main;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShakeEvent {

    /**
     * 监听客户端Tick事件，用于更新震动状态
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ScreenShakeHandler.tick();
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (ScreenShakeHandler.shakeDuration > 0 && ScreenShakeHandler.shakeStrength > 0) {
            float strength = ScreenShakeHandler.shakeStrength;

            // 计算随机的角度偏移量
            float pitchShake = strength * (float) (Math.random() * 2 - 1);
            float rollShake = strength * (float) (Math.random() * 2 - 1);

            // 在原有的角度上，加上我们的抖动偏移量
            event.setPitch(event.getPitch() + pitchShake);
            event.setRoll(event.getRoll() + rollShake);
        }
    }
}
