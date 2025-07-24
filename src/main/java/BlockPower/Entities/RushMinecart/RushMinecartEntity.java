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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static BlockPower.Main.Main.sendDebugMessage;
import static BlockPower.ModMessages.ModMessages.sendToPlayer;
import static BlockPower.Util.ScreenShake.ScreenShakeHandler.shakeTrigger;

public class RushMinecartEntity extends AbstractMinecart {

    private static final Logger LOGGER = LoggerFactory.getLogger("Rush_Minecart");

    /**
     * 定义矿车的所有可能状态
     */
    public enum State {
        INITIALIZING, //初始延迟, 等待玩家骑乘
        RUSHING,      //玩家在车上, 高速冲刺
        SEEKING,      //玩家下车, 寻找新目标
        CAPTURED,     //已捕获新乘客, 冷却中
        CRASHED,      //已撞毁, 准备销毁
        ENDING        //技能结束, 准备销毁
    }

    private Player player;

    private Vec3 lastRailPlacementPos = Vec3.ZERO;//记录上一个生成点的位置

    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(RushMinecartEntity.class, EntityDataSerializers.INT);

    private TickTimer rideDelayTimer;
    private TickTimer endTimer;
    private TickTimer crashTimer;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, State.INITIALIZING.ordinal());
    }

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
        normalMinecraftLogic();
        handleMinecartMovement();

        //服务端逻辑
        if (!this.level().isClientSide) {
            updateState();
            handleTrailSpawning();
        }
    }

    /**
     * 核心逻辑, 根据当前状态执行对应逻辑并切换状态
     */
    private void updateState() {
        if (player == null) {
            return;
        }

        State currentState = getState();

        switch (currentState) {
            case INITIALIZING:
                if (rideDelayTimer == null) rideDelayTimer = new TickTimer();
                if (rideDelayTimer.waitTicks(rideDelayTimer, 2)) {
                    player.startRiding(this);
                    setState(State.RUSHING);
                }
                break;

            case RUSHING:
                if (this.getFirstPassenger() != player) {
                    setState(State.SEEKING);
                    break;
                }
                hurtEntity(player);
                break;

            case SEEKING:
                List<Entity> entities = detectEntity(player, 4);
                if (!entities.isEmpty()) {
                    entities.get(0).startRiding(this);
                    setState(State.CAPTURED);
                } else {
                    //如果在一定时间内没找到目标，则进入结束状态
                    if (endTimer == null) endTimer = new TickTimer();
                    if (endTimer.waitTicks(endTimer, 80)) {
                        setState(State.ENDING);
                    }
                }
                break;

            case CAPTURED:
                //被捕获的生物下车后，进入结束状态
                if (endTimer == null) endTimer = new TickTimer();
                if (endTimer.waitTicks(endTimer, 40)) {
                    setState(State.ENDING);
                }
                break;

            case CRASHED:
                hurtEntity(player);
                if (crashTimer == null) crashTimer = new TickTimer();
                if (crashTimer.waitTicks(crashTimer, 40)) {
                    this.discard();
                }
                break;

            case ENDING:
                this.discard();
                break;
        }
    }

    private void handleMinecartMovement() {
        State currentState = getState();

        //冲刺时无重力
        if (currentState == State.RUSHING || currentState == State.INITIALIZING) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(new Vec3(motion.x, 0, motion.z));
        } else {
            this.setDeltaMovement(this.getDeltaMovement()
                    .multiply(0.97, 1.0, 0.97)
                    .add(0.0, -0.1D, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private void handleTrailSpawning() {
        if (getState() == State.RUSHING) {//矿车正常运作中才会生成铁轨
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
    }

    private void spawnRailAt(Vec3 pos) {
        FakeRailEntity fakeRail = new FakeRailEntity(this.level(), pos.x(), pos.y(), pos.z(), this.getYRot());
        this.level().addFreshEntity(fakeRail);
    }


    public void spawnInitialRail() {
        Vec3 initialPos = this.position();
        this.spawnRailAt(initialPos);
        this.lastRailPlacementPos = initialPos;
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

        minecart.spawnInitialRail();

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
        List<Entity> entities = detectEntity(player, 4);
        if (!entities.isEmpty()) {
            entities.forEach(entity -> {
                entity.hurt(this.level().damageSources().mobAttack(player), 15F);
                if (entity instanceof ServerPlayer) {
                    sendToPlayer(new PlayerActionPacket_S2C(ServerAction.SHAKE), (ServerPlayer) entity);
                }
                knockBackEntity(entity, 0.6);
            });
            //触发屏幕震动
            if (getState() == State.RUSHING) {
                sendToPlayer(new PlayerActionPacket_S2C(ServerAction.SHAKE), (ServerPlayer) player);
            }
            setState(State.CRASHED);
        }
    }

    private void knockBackEntity(Entity entity, double strength) {
        Vec3 knockbackVector = entity.position().subtract(this.position()).normalize();
        entity.setDeltaMovement(entity.getDeltaMovement().add(
                knockbackVector.x * strength,
                1 * strength,
                knockbackVector.z * strength
        ));
    }

    private void normalMinecraftLogic() {
        this.checkBelowWorld(); // 检查是否掉出世界
        this.checkInsideBlocks();
        if (this.isVehicle()) {
            for (Entity entity : this.getPassengers()) {
                entity.setPos(this.getX(), this.getY() + this.getPassengersRidingOffset() + entity.getMyRidingOffset(), this.getZ());
            }
        }
    }

    private State getState() {
        return State.values()[this.entityData.get(DATA_STATE)];
    }

    private void setState(State state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    @Override
    public boolean canRiderInteract() {
        return !(getState() == State.RUSHING);
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        if (getState() == State.CAPTURED) {
            return;
        }
        super.removePassenger(passenger);
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
