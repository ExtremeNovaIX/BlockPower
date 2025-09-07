package BlockPower.ModEntities.RushMinecart;

import BlockPower.ModEntities.IStateMachine;
import BlockPower.ModEntities.ModEntities;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static BlockPower.Util.Commons.applyDamage;
import static BlockPower.Util.Commons.detectEntity;
import static BlockPower.Util.EffectSender.broadcastScreenShake;
import static BlockPower.Util.EffectSender.sendHitStop;

public class RushMinecartEntity extends AbstractMinecart implements IStateMachine<RushMinecartEntity.RushMinecartState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RushMinecartEntity.class);

    /**
     * 定义矿车的所有可能状态
     */
    public enum RushMinecartState {
        INITIALIZING, //初始化逻辑
        RUSHING,      //玩家在车上, 高速冲刺
        HITSTOPPING,   //矿车被卡帧, 停止移动
        SEEKING,      //玩家下车, 寻找新目标
        CAPTURED,     //已捕获新乘客, 冷却中
        CRASHED,      //已撞毁, 准备销毁
        ENDING        //技能结束, 准备销毁
    }

    private static final Integer MAX_SPEED = 2;

    private final ServerPlayer player;

    private Vec3 lastRailPlacementPos = Vec3.ZERO;//记录上一个生成点的位置

    private static final TimerManager timerManager = TimerManager.getInstance(false);//全局计时器管理类

    private static final TaskManager taskManager = TaskManager.getInstance(false);

    private Vec3 minecartSpeed = Vec3.ZERO;

    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(RushMinecartEntity.class, EntityDataSerializers.INT);

    public RushMinecartEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
    }

    public RushMinecartEntity(ServerPlayer player) {
        super(ModEntities.RUSH_MINECART.get(), player.level());
        this.player = player;
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

        RushMinecartState currentState = getState();

        //速度过低时，进入撞毁状态
        if (currentState == RushMinecartState.RUSHING && this.getDeltaMovement().length() < 0.8) {
            setState(RushMinecartState.CRASHED);
        }

        switch (currentState) {
            case INITIALIZING:
                player.startRiding(this);
                setState(RushMinecartState.RUSHING);
                break;

            case RUSHING:
                if (this.getFirstPassenger() != player) {
                    setState(RushMinecartState.SEEKING);
                    break;
                }
                hurtEntity(player);
                break;

            case HITSTOPPING:
                if (timerManager.isTimerCyclingDue(this, "hitStopTimer", 3)) {
                    setState(RushMinecartState.CRASHED);
                }
                break;

            case SEEKING:
                List<Entity> entities = detectEntity(this, 4, player);
                if (!entities.isEmpty()) {
                    entities.get(0).startRiding(this);
                    setState(RushMinecartState.CAPTURED);
                } else {
                    //如果在一定时间内没找到目标，则进入结束状态
                    if (timerManager.isTimerCyclingDue(this, "endTimer", 80)) {
                        setState(RushMinecartState.ENDING);
                    }
                }
                break;

            case CAPTURED:
                //被捕获的生物下车后，进入结束状态
                if (timerManager.isTimerCyclingDue(this, "endTimer", 20)) {
                    setState(RushMinecartState.ENDING);
                }
                break;

            case CRASHED:
                //TODO 修改为按速度大小决定伤害检测范围
                if (this.getFirstPassenger() == player) {
                    player.stopRiding();
                }
                hurtEntity(player);
                if (timerManager.isTimerCyclingDue(this, "crashTimer", 20)) {
                    setState(RushMinecartState.ENDING);
                }
                break;

            case ENDING:
                this.discard();
                break;
        }
    }

    private void handleMinecartMovement() {
        RushMinecartState currentState = getState();
        Vec3 motion = this.getDeltaMovement();

        //冲刺时无重力
        if (currentState == RushMinecartState.RUSHING || currentState == RushMinecartState.INITIALIZING || currentState == RushMinecartState.HITSTOPPING) {
            this.setDeltaMovement(new Vec3(motion.x, 0, motion.z));
        } else {
            this.setDeltaMovement(this.getDeltaMovement()
                    .multiply(0.94, 1.0, 0.94)
                    .add(0.0, -0.1D, 0.0));
        }
        this.move(MoverType.SELF, new Vec3(motion.x, motion.y, motion.z));
    }

    private void handleTrailSpawning() {
        if (getState() == RushMinecartState.RUSHING) {//矿车正常运作中才会生成铁轨
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

    public static void createRushMinecart(ServerPlayer player) {
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
        minecart.setMinecartSpeed(lookAngle.normalize().scale(MAX_SPEED));
        minecart.setDeltaMovement(minecart.getMinecartSpeed());
        //将矿车添加到世界中
        player.level().addFreshEntity(minecart);
        LOGGER.info("生成冲刺矿车");

        minecart.spawnInitialRail();

        //使玩家被矿车吸引
        if (player.onGround()) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.5, 0));
        }
        Vec3 attractionVector = minecart.position().subtract(player.position());
        attractionVector = attractionVector.normalize().scale(1);
        Vec3 newMotion = player.getDeltaMovement().add(attractionVector);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), newMotion));
    }

    private void hurtEntity(@NotNull Player player) {
        List<Entity> entityList = applyDamage(this, player, 15F, 5, ModSounds.MINECART_CRASH_SOUND.get());
        Commons.knockBackEntity(this, entityList, 1.5);
        if (!entityList.isEmpty()) {
            //玩家在车上时触发屏幕震动
            if (getState() == RushMinecartState.RUSHING && this.getFirstPassenger() == player) {
                broadcastScreenShake(this, 6, 2.5f, 5, 3);
                sendHitStop(4, (ServerPlayer) player, this);
            }

            if (getState() == RushMinecartState.RUSHING) {
                setState(RushMinecartState.CRASHED);
            }
        }
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

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        // 在构造时注册DataAccessor并设置默认State
        this.getEntityData().define(DATA_STATE, RushMinecartState.INITIALIZING.ordinal());
    }

    @Override
    public EntityDataAccessor<Integer> getStateDataAccessor() {
        return DATA_STATE;
    }

    @Override
    public RushMinecartState[] getStateEnumValues() {
        return RushMinecartState.values();
    }

    public Vec3 getMinecartSpeed() {
        return minecartSpeed;
    }

    public void setMinecartSpeed(Vec3 minecartSpeed) {
        this.minecartSpeed = minecartSpeed;
    }

    @Override
    public boolean canRiderInteract() {
        return !(getState() == RushMinecartState.RUSHING);
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        if (getState() == RushMinecartState.CAPTURED) {
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
    public boolean canCollideWith(Entity p_20303_) {
        return false;
    }
}
