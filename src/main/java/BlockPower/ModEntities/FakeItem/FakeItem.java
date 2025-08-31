package BlockPower.ModEntities.FakeItem;

import BlockPower.ModEntities.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FakeItem extends ItemEntity {
    private int lifeTime = 10;//物品存活时间

    public FakeItem(Level level, double x, double y, double z, ItemStack stack, double velocityX, double velocityY, double velocityZ, int lifeTime) {
        super(level, x, y, z, stack, velocityX, velocityY, velocityZ);
        this.lifeTime = lifeTime;
        this.setPickUpDelay(2097152);
    }

    public FakeItem(Level level, Vec3 position, Vec3 velocity, ItemStack stack, int lifeTime) {
        super(ModEntities.FAKE_ITEM.get(), level);
        this.setPos(position);
        this.setDeltaMovement(velocity);
        this.setItem(stack);
        this.lifeTime = lifeTime;
        this.setPickUpDelay(2097152);
    }

    public FakeItem(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FakeItem(Level level, double x, double y, double z, ItemStack stack, int lifeTime) {
        super(level, x, y, z, stack);
        this.lifeTime = lifeTime;
        this.setPickUpDelay(2097152);
    }

    @Override
    public void tick() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.06, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = 0.98F;
        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98, friction));

        lifeTime--;
        if (lifeTime <= 0) {
            this.discard();
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

}
