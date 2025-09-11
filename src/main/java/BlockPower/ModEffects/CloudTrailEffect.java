package BlockPower.ModEffects;

import BlockPower.Util.Timer.TickTimer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.WeakHashMap;

public class CloudTrailEffect {
    /**
     * 全局击飞粒子效果计时器
     */
    public static final Map<Entity, TickTimer> cloudParticleTimers = new WeakHashMap<>();

    public static void handleCloudParticleTimer() {
        cloudParticleTimers.entrySet().removeIf(
                entry -> {
                    Entity entity = entry.getKey();
                    ServerLevel serverLevel = (ServerLevel) entity.level();
                    TickTimer timer = entry.getValue();

                    if (timer.isFinished() || entity.isRemoved()) {
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
