package BlockPower.Entities.DropAnvil;

import BlockPower.Entities.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;

public class DropAnvilEntity extends Entity {
    public DropAnvilEntity(EntityType<? extends DropAnvilEntity> entityType, Level level) {
        super(entityType, level);
    }

    public DropAnvilEntity(ServerPlayer player) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
    }

    public DropAnvilEntity(ServerPlayer player, double x, double y, double z) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));

    }

    public static void createDropAnvil(ServerPlayer player) {
        DropAnvilEntity dropAnvil = new DropAnvilEntity(ModEntities.DROP_ANVIL.get(), player.level());
        dropAnvil.setPos(player.getX(), player.getY(), player.getZ());
        player.level().addFreshEntity(dropAnvil);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}
