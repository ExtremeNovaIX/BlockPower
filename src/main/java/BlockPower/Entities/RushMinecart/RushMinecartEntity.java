package BlockPower.Entities.RushMinecart;

import BlockPower.Entities.FakeRail.FakeRailEntity;
import BlockPower.Entities.ModEntities;
import BlockPower.Main.Main;
import BlockPower.ModMessages.PlayerActionPacket_S2C;
import BlockPower.ModMessages.ServerAction;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static BlockPower.Main.Main.sendDebugMessage;
import static BlockPower.ModMessages.ModMessages.sendToPlayer;
import static BlockPower.Util.ScreenShake.ScreenShakeHandler.shakeTrigger;

public class RushMinecartEntity extends AbstractMinecart {

    private static final Logger LOGGER = LoggerFactory.getLogger("Rush_Minecart");

    private final Player player;

    private int rideDelayTicks = 2;

    private int cooldownTicks = 0; //无法挣脱的计时器

    private static final int COOLDOWN_DURATION = 20; //无法挣脱的持续时间

    private Vec3 lastRailPlacementPos = Vec3.ZERO;//记录上一个生成点的位置

    private boolean isActive = false;//矿车运行状态

    private boolean physicsEnabled = false;

    private TickTimer rideDelayTimer;



    public RushMinecartEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
    }

    protected RushMinecartEntity(EntityType<?> entityType, Level level, double x, double y, double z, ServerPlayer player) {
        super(entityType, level, x, y, z);
        this.player = player;
    }


    @Override
    public void tick() {
        this.checkBelowWorld(); // 检查是否掉出世界
        this.checkInsideBlocks();
        if (this.isVehicle()) {
            for (net.minecraft.world.entity.Entity entity : this.getPassengers()) {
                entity.setPos(this.getX(), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset(), this.getZ());
            }
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        handleMinecartMovement();
        //服务端逻辑
        if (!this.level().isClientSide) {
            handleTrailSpawning();
            handleRushMinecart();
        }
    }

    private void handleMinecartMovement() {
        if (this.player != null) {
            if (physicsEnabled) {
                //保持矿车无重力和阻力
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(new Vec3(motion.x, 0, motion.z));
            } else {
                //模拟重力和阻力
                this.setDeltaMovement(this.getDeltaMovement()
                        .multiply(0.95, 1.0, 0.95)//x和z轴应用阻力
                        .add(0.0, -0.03D, 0.0)//y轴应用重力
                );
            }
        }
    }

    private void handleTrailSpawning() {
        Vec3 currentPos = this.position();
        // 第一次生成时，直接在脚下生成一个并设置记录点
        if (lastRailPlacementPos.equals(Vec3.ZERO)) {
            spawnRailAt(currentPos);
            lastRailPlacementPos = currentPos;
            return;
        }

        Vec3 moveVector = currentPos.subtract(lastRailPlacementPos);
        double moveDistance = moveVector.length();

        // 如果移动距离大于等于1个方块，就需要填充
        if (moveDistance >= 1.0D) {
            Vec3 moveDirection = moveVector.normalize();
            // 计算需要填充多少个铁轨
            int segmentsToSpawn = (int) Math.floor(moveDistance);

            // 循环填充空隙
            for (int i = 1; i <= segmentsToSpawn; i++) {
                Vec3 spawnPos = lastRailPlacementPos.add(moveDirection.scale(i));
                spawnRailAt(spawnPos);
            }

            // 更新记录点，只加上已经填充过的整数距离
            lastRailPlacementPos = lastRailPlacementPos.add(moveDirection.scale(segmentsToSpawn));
        }
    }

    private void spawnRailAt(Vec3 pos) {
        FakeRailEntity fakeRail = new FakeRailEntity(this.level(), pos.x(), pos.y(), pos.z(), this.getYRot());
        this.level().addFreshEntity(fakeRail);
    }

    private void handleRushMinecart() {

        if (rideDelayTicks-- == 0) {
            if (player != null) {
                player.startRiding(this);
            }
        }
        // 更新冷却计时器
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        //如果技能释放者在矿车上，那么伤害撞到的实体
        if (player != null) {

            if (rideDelayTimer == null) {
                rideDelayTimer = new TickTimer();
            } else {
                //矿车逻辑
                Entity passenger = this.getFirstPassenger();
                if (passenger == player) {
                    isActive = true;//标志初始化完成，可以开始执行逻辑
                    hurtEntity(this.player);
                } else {
                    if (isActive) {//初始化完成且玩家下车，执行强制骑乘逻辑
                        //如果技能释放者不在矿车上，那么让碰撞到的第一个实体强制骑乘矿车并在一段时间内无法挣脱
                        List<Entity> entities = detectEntity(this.player, 5);
                        if (!entities.isEmpty()) {
                            entities.get(0).startRiding(this);
                            cooldownTicks = COOLDOWN_DURATION;
                            isActive = false;//技能结束，进入销毁逻辑
                            physicsEnabled = true;
                        }
                    }
                }
            }
        }
    }

    public static void createRushMinecart(@NotNull ServerPlayer player) {
        Vec3 lookAngle = player.getLookAngle();
        lookAngle = new Vec3(lookAngle.x, 0, lookAngle.z).normalize();
        double distance = 1.5;
        //创建一个新的矿车实体
        RushMinecartEntity minecart = new RushMinecartEntity(
                ModEntities.RUSH_MINECART.get(),
                player.level(),
                player.getX() + lookAngle.x * distance,
                player.getY() + 0.1,
                player.getZ() + lookAngle.z * distance
                , player
        );

        //设置矿车的速度和方向
        minecart.setYRot(player.getYRot() + 90);
        minecart.setXRot(0.0F);
        minecart.yRotO = minecart.getYRot() + 90;
        minecart.xRotO = minecart.getXRot();
        double scale = 1.5;
        minecart.setDeltaMovement(lookAngle.scale(scale));

        //将矿车添加到世界中
        player.level().addFreshEntity(minecart);
        LOGGER.info("生成冲刺矿车");

        if (player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.5, 0));
        }

        //使玩家被矿车吸引
        Vec3 attractionVector = minecart.position().subtract(player.position());
        attractionVector = attractionVector.normalize().scale(1);
        Vec3 newMotion = player.getDeltaMovement().add(attractionVector);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), newMotion));
    }

    /**
     * 检测半径内的非技能释放者的LivingEntity
     *
     * @param player 释放技能的玩家
     * @param radius 检测半径
     * @return 半径内的非技能释放者和非自身LivingEntity列表
     */
    private List<Entity> detectEntity(@NotNull Player player, double radius) {
        //创建一个默认半径为radius的检测区域
        AABB detectionArea = new AABB(
                this.getX() - radius,
                this.getY() - radius,
                this.getZ() - radius,
                this.getX() + radius,
                this.getY() + radius,
                this.getZ() + radius
        );

        //获取半径内的非技能释放者的LivingEntity
        return this.level().getEntities(
                this,
                detectionArea,
                entity -> entity != this
                        && entity.distanceToSqr(this) <= radius * radius
                        && entity != player
                        && entity instanceof LivingEntity
        );
    }

    /**
     * 伤害撞到的实体
     *
     * @param player 释放技能的玩家
     */
    private void hurtEntity(@NotNull Player player) {
        List<Entity> entities = detectEntity(player, 3);
        if (!entities.isEmpty()) {
            entities.forEach(entity -> {
                entity.hurt(this.level().damageSources().mobAttack(player), 15F);
                if (entity instanceof ServerPlayer) {
                    sendToPlayer(new PlayerActionPacket_S2C(ServerAction.SHAKE), (ServerPlayer) entity);
                }
                knockBackEntity(entity, 2);
            });
            //触发屏幕震动并线性衰减
            shakeTrigger(5, 3f);
        }
    }

    private void knockBackEntity(Entity entity, double strength) {
        Vec3 knockbackVector = entity.position().subtract(this.position()).normalize();
        entity.setDeltaMovement(entity.getDeltaMovement().add(
                knockbackVector.x * strength,
                0.6 * strength,
                knockbackVector.z * strength
        ));
    }

    @Override
    public boolean canRiderInteract() {
        if (this.getFirstPassenger() == player) {
            return true;
        } else {
            return cooldownTicks <= 0; // 冷却期间禁止下车
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

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity pEntity) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
