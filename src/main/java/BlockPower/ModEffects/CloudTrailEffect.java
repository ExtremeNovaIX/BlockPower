package BlockPower.ModEffects;

import BlockPower.Util.Timer.TickTimer;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class CloudTrailEffect implements ITickBasedEffect {
    private final Entity entity;
    private final TickTimer tickTimer;

    public CloudTrailEffect(Entity entity, int duration) {
        this.entity = entity;
        tickTimer = new TickTimer(duration, false);
    }

    @Override
    public void tick() {
        ServerLevel serverLevel = (ServerLevel) entity.level();
        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                entity.getX(),
                entity.getY() + 1.0,
                entity.getZ(),
                3,
                0.3, 0.3, 0.3,
                0.05
        );
    }

    @Override
    public boolean isFinished() {
        return tickTimer.isFinished() || entity.isRemoved();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
