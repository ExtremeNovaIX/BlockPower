package BlockPower.Effects;

import BlockPower.Util.Timer.TickTimer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class CloudParticlesEffect {
    public static final Map<Entity, TickTimer> cloudParticleTimers = new HashMap<>();//全局击飞粒子效果计时器

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            cloudParticleTimers.entrySet().removeIf(
                    entry -> {
                        Entity entity = entry.getKey();
                        ServerLevel serverLevel = (ServerLevel) entity.level();

                        if (!(entity.level() instanceof ServerLevel) || entity.isRemoved()) {
                            return true;
                        }

                        TickTimer timer = entry.getValue();
                        if (timer.isFinished()) {
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
}

