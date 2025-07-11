package BlockPower.Entities.FakeRail;

import BlockPower.Entities.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class FakeRailEntity extends Entity {

    private int lifeTime = 10;

    private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(FakeRailEntity.class, EntityDataSerializers.FLOAT);

    public FakeRailEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public FakeRailEntity(Level level, double x, double y, double z, float yRot) {
        super(ModEntities.FAKE_RAIL_ENTITY.get(), level);
        this.setPos(x, y, z);
        this.setYRot(yRot);
    }

    @Override
    public void tick() {
        super.tick();
        //服务器端逻辑
        if (!this.level().isClientSide) {
            if (lifeTime-- <= 0) {
                this.discard(); // 销毁实体
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_YAW, 0.0F);
    }

    @Override
    public void setYRot(float yRot) {
        this.entityData.set(DATA_YAW, yRot);
        super.setYRot(yRot);
    }

    @Override
    public float getYRot() {
        return this.entityData.get(DATA_YAW);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        //让客户端知道实体被创建的数据包
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}