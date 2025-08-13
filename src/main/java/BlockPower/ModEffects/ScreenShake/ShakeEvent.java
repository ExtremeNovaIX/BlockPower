package BlockPower.ModEffects.ScreenShake;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import BlockPower.Main.Main;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShakeEvent {
    private static final Random random = new Random();
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
            float yawShake = strength * (random.nextFloat() * 2.0F - 1.0F);
            float pitchShake = strength * (random.nextFloat() * 2.0F - 1.0F);
            float rollShake = strength * (random.nextFloat() * 2.0F - 1.0F);

            // 在原有的摄像机角度上，加上抖动偏移量
            event.setYaw(event.getYaw() + yawShake);
            event.setPitch(event.getPitch() + pitchShake);
            event.setRoll(event.getRoll() + rollShake);
        }
    }
}
