package BlockPower.Effects;

import BlockPower.Util.Commons;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GlobalEffectHandler {
    public static final Map<Entity, TickTimer> cloudParticleTimers = new WeakHashMap<>();//全局击飞粒子效果计时器
    public static final Map<Entity, Map.Entry<Vec3, TickTimer>> hitStopTimers = new WeakHashMap<>();//全局卡帧效果计时器

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            handleCloudParticleTimer();
            handleHitStopTimer();
        }
    }

    private static void handleHitStopTimer() {
        hitStopTimers.entrySet().removeIf(
                entry -> {
                    Entity entity = entry.getKey();
                    Map.Entry<Vec3, TickTimer> onlyEntry = entry.getValue();
                    Vec3 speed = onlyEntry.getKey();
                    TickTimer timer = onlyEntry.getValue();

                    if (entity.isRemoved()){
                        return true;
                    }

                    if (timer.isFinished()) {
                        entity.setDeltaMovement(speed);
                        return true;
                    }
                    return false;
                }
        );
    }

    private static void handleCloudParticleTimer() {
        cloudParticleTimers.entrySet().removeIf(
                entry -> {
                    Entity entity = entry.getKey();
                    ServerLevel serverLevel = (ServerLevel) entity.level();
                    TickTimer timer = entry.getValue();

                    if (timer.isFinished()|| entity.isRemoved()) {
                        return true;
                    }

                    serverLevel.sendParticles(
                            ParticleTypes.CLOUD,
                            entity.getX(),
                            entity.getY() + 1.0,
                            entity.getZ(),
                            3,
                            0.3, 0.3, 0.3,
                            0.05
                    );
                    return false;
                }
        );
    }
}

