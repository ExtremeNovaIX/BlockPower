package BlockPower.ModEffects.ServerEffect;

import BlockPower.ModEffects.ITickBasedEffect;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AttractEntityEffect implements ITickBasedEffect {
    private List<Entity> entitiesToAttract = new ArrayList<>();
    private Entity mainEntity;
    private int duration;
    private double springConstant = 0.6;

    public AttractEntityEffect(int duration, Entity mainEntity, List<Entity> entitiesToAttract) {
        this.duration = duration;
        this.mainEntity = mainEntity;
        this.entitiesToAttract = entitiesToAttract;
    }

    public AttractEntityEffect(int duration, Entity mainEntity, Entity... entitiesToAttract) {
        this.duration = duration;
        this.mainEntity = mainEntity;
        this.entitiesToAttract = List.of(entitiesToAttract);
    }

    public AttractEntityEffect(int duration, int springConstant, Entity mainEntity, List<Entity> entitiesToAttract) {
        this.duration = duration;
        this.mainEntity = mainEntity;
        this.entitiesToAttract = entitiesToAttract;
        this.springConstant = springConstant;
    }

    public AttractEntityEffect(int duration, int springConstant, Entity mainEntity, Entity... entitiesToAttract) {
        this.duration = duration;
        this.mainEntity = mainEntity;
        this.entitiesToAttract = List.of(entitiesToAttract);
        this.springConstant = springConstant;
    }


    @Override
    public void tick() {
        duration--;

        entitiesToAttract.forEach(entity -> {
            if (entity.isRemoved() || mainEntity.isRemoved()) return;

            // 计算方向向量
            Vec3 directionVec = (mainEntity.getPosition(0).add(0, 1, 0)).subtract(entity.getPosition(0)).normalize();
            Vec3 forceVector = directionVec.scale(springConstant);

            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), forceVector));
            }
            entity.setDeltaMovement(forceVector);

        });
    }

    @Override
    public boolean isFinished() {
        return duration <= 0;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
