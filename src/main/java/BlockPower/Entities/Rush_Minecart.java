package BlockPower.Entities;

import BlockPower.Main.Main;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.min;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Rush_Minecart extends AbstractMinecart {

    private static final Logger LOGGER = LoggerFactory.getLogger("Rush_Minecart");

    private ServerPlayer player;

    protected Rush_Minecart(EntityType<?> entityType, Level level, double x, double y, double z,ServerPlayer player) {
        super(entityType, level, x, y, z);
        this.player = player;
    }

    public static void createRushMinecart(@NotNull ServerPlayer player) {
        Vec3 lookAngle = player.getLookAngle();
        lookAngle = new Vec3(lookAngle.x, 0, lookAngle.z).normalize();
        double distance = 1.5;
        //创建一个新的矿车实体
        Rush_Minecart minecart = new Rush_Minecart(
                EntityType.MINECART,
                player.level(),
                player.getX() + lookAngle.x * distance,
                player.getY() - 0.5,
                player.getZ() + lookAngle.z * distance
                ,player
        );

        //设置矿车的速度和方向
        minecart.setYRot(player.getYRot() + 90);
        minecart.setXRot(0.0F);
        minecart.yRotO = minecart.getYRot() + 90;
        minecart.xRotO = minecart.getXRot();
        double scale = 1.0;
        minecart.setDeltaMovement(lookAngle.scale(scale));

        //将矿车添加到世界中
        player.level().addFreshEntity(minecart);
        minecart.setNoGravity(true);

        //发送调试信息
        Main.sendDebugMessage(player, "生成冲刺矿车");
        LOGGER.info("生成冲刺矿车");

        //使玩家被矿车吸引
        Vec3 attractionVector = minecart.position().subtract(player.position());
        attractionVector = attractionVector.normalize().scale(0.8);
        Vec3 newMotion = player.getDeltaMovement().add(attractionVector);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), newMotion));
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        //让玩家骑乘矿车
        player.startRiding(minecart);
    }

    private List<Entity> detectEntity(@NotNull ServerPlayer player) {
        //创建一个半径为radius的检测区域
        double radius = 2;
        AABB detectionArea = new AABB(
                this.getX() - radius,
                this.getY() - radius,
                this.getZ() - radius,
                this.getX() + radius,
                this.getY() + radius,
                this.getZ() + radius
        );

        //获取半径内的非乘坐者实体
        return this.level().getEntities(
                this,
                detectionArea,
                entity -> entity != this
                        && entity.distanceToSqr(this) <= radius * radius
                        && entity != player
        );
    }

    private void hurtEntity(@NotNull ServerPlayer player) {
        List<Entity> entities = detectEntity(player);
        if (!entities.isEmpty()) {
            entities.forEach(entity -> {
                entity.hurt(this.level().damageSources().magic(), 20.0F);
            });
        }
    }

    @Override
    public void tick() {
        this.checkBelowWorld(); // 检查是否掉出世界
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.checkInsideBlocks();
        if (this.isVehicle()) {
            for (net.minecraft.world.entity.Entity entity : this.getPassengers()) {
                entity.setPos(this.getX(), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset(), this.getZ());
            }
        }
        //伤害撞到的实体
        if (this.player != null) {
            hurtEntity(this.player);
        }
    }

    @Override
    protected @NotNull Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public @NotNull Type getMinecartType() {
        return Type.RIDEABLE;
    }

    //去除空气阻力
    @Override
    public double getDragAir() {
        return 1.0;
    }

    @Override
    protected void comeOffTrack() {
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(this.getDragAir()));
        }
    }
}
