package BlockPower.ModEffects.ServerEffect;

import BlockPower.ModEffects.ITickBasedEffect;
import BlockPower.Util.Timer.TickTimer;
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
        double speedSquared = entity.getDeltaMovement().lengthSqr();
        if (speedSquared < Math.pow(0.2, 2)) return;
        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                entity.getX(),
                entity.getY() + 0.5,
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
