package BlockPower.Entities.RushMinecart;

import BlockPower.Entities.FakeRail.FakeRailEntity;
import BlockPower.Entities.ModEntities;
import BlockPower.Main.Main;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
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

import static BlockPower.Util.ScreenShake.ScreenShakeHandler.shakeTrigger;

public class RushMinecartEntity extends AbstractMinecart {

    private static final Logger LOGGER = LoggerFactory.getLogger("Rush_Minecart");

    private boolean rideFlag = false;//控制矿车只拉取一次生物

    private boolean isCrashed = false;//矿车是否撞击

    private final Player player;

    private int rideDelayTicks = 2;

    private int cooldownTicks = 0; //无法挣脱的计时器

    private static final int COOLDOWN_DURATION = 60; //无法挣脱的持续时间

    private Vec3 lastRailPlacementPos = Vec3.ZERO;//记录上一个生成点的位置

    private TickTimer crashTimer;

    private Vec3 forceDirection = null;     //锁定矿车前进方向

    public RushMinecartEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
    }

    protected RushMinecartEntity(EntityType<?> entityType, Level level, double x, double y, double z, ServerPlayer player) {
        super(entityType, level, x, y, z);
        this.player = player;
    }

    /**
     * 矿车主要逻辑
     */
    @Override
    public void tick() {
        initMinecart();
        //服务端逻辑
        if (!this.level().isClientSide) {
            handleTrailSpawning();//铁轨实体生成
            handleRushMinecart();//矿车逻辑
        }
    }

    private void initMinecart() {
        this.checkBelowWorld(); // 检查是否掉出世界
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.checkInsideBlocks();
        if (this.isVehicle()) {
            for (Entity entity : this.getPassengers()) {
                entity.setPos(this.getX(), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset(), this.getZ());
            }
        }
    }

    private void handleTrailSpawning() {
        if (isCrashed) return;
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
        //技能释放后2tick后让释放者骑乘矿车并开始执行矿车逻辑
        if (rideDelayTicks-- == 0) {
            player.startRiding(this);
            rideFlag = true;
            this.forceDirection = this.getDeltaMovement().normalize();//记录矿车方向
        }
        // 更新冷却计时器
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        //如果销毁逻辑触发，那么在40tick后销毁
        if (isCrashed) {
            hurtEntity(player);
            crashMinecart(player, 40);
        }

        //如果玩家下车，那么在80tick后销毁
        if (!this.player.isPassenger() && rideFlag) {
            crashMinecart(player, 80);
        }

        //如果技能释放者在矿车上，那么伤害撞到的实体
        if (this.player.isPassenger()) {
            hurtEntity(this.player);
            knockbackEntities(this.player, 5, 0.8);
            //如果技能释放者在矿车上，那么锁定矿车前进方向
            if (this.forceDirection != null) {
                double speed = this.getDeltaMovement().length();
                Vec3 newMotion;

                if (isCrashed) {
                    speed *= 0.95;
                    //模拟重力和阻力
                    newMotion = this.forceDirection.scale(speed).add(0.0, -0.03D, 0.0); // 保持方向，施加重力
                } else {
                    // 未撞毁：保持方向和速率，无视重力和碰撞
                    newMotion = this.forceDirection.scale(speed);
                }
                this.setDeltaMovement(newMotion);
            }

        } else {
            //如果技能释放者不在矿车上，那么让碰撞到的第一个实体强制骑乘矿车并在一段时间内无法挣脱
            if (rideFlag) {//初始化过后（rideFlag被设为true），开始执行逻辑

                if (!isCrashed) {//撞击导致的下车不执行此逻辑
                    List<Entity> entities = detectEntity(this.player, 3);

                    if (!entities.isEmpty()) {
                        entities.get(0).startRiding(this);
                        cooldownTicks = COOLDOWN_DURATION;
                        rideFlag = false;
                    }

                }

            }
            //模拟重力和阻力
            this.setDeltaMovement(this.getDeltaMovement()
                    .multiply(0.95, 1.0, 0.95)//x和z轴应用阻力
                    .add(0.0, -0.03D, 0.0)//y轴应用重力
            );
        }
    }

    private void crashMinecart(Player player, int duration) {
        isCrashed = true;
        if (crashTimer == null) {
            crashTimer = new TickTimer();
        }

        if (crashTimer.waitTicks(crashTimer, duration)) {
            player.stopRiding();
            this.remove(RemovalReason.DISCARDED);
        }
    }

    public static void createRushMinecart(@NotNull ServerPlayer player) {
        Vec3 lookAngle = player.getLookAngle();
        lookAngle = new Vec3(lookAngle.x, 0, lookAngle.z).normalize();
        double distance = 1.5;
        //创建一个新的矿车实体
        RushMinecartEntity minecart = new RushMinecartEntity(
                EntityType.MINECART,
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
                entity.hurt(this.level().damageSources().mobAttack(player), 10.0F);
                //如果是玩家，播放震动效果
                if (entity instanceof Player) {
                    shakeTrigger(player, 5, 3f, 0.7f);
                }
            });
            //触发屏幕震动并线性衰减
            shakeTrigger(player, 5, 3f, 0.7f);
            isCrashed = true;//更新isCrashed并触发销毁逻辑
        }
    }

    /**
     * 击退检测到的实体
     * @param player 技能释放者
     * @param radius 检测半径
     * @param strength 击退力度
     */
    private void knockbackEntities(Player player, double radius, double strength) {
        List<Entity> entitiesToKnockback = this.detectEntity(player, radius);

        if (!entitiesToKnockback.isEmpty()) {
            for(Entity entity : entitiesToKnockback) {
                // 计算从矿车到实体的方向向量
                Vec3 knockbackDirection = entity.position().subtract(this.position()).normalize();

                // 设置击退速度
                Vec3 knockbackVelocity = new Vec3(knockbackDirection.x() * strength, 0.4, knockbackDirection.z() * strength);

                // 应用击退速度
                entity.setDeltaMovement(knockbackVelocity);
            }
        }
    }

    @Override
    public boolean canRiderInteract() {
        return cooldownTicks <= 0; // 冷却期间禁止下车
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
