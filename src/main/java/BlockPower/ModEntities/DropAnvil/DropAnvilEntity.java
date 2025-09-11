package BlockPower.ModEntities.DropAnvil;

import BlockPower.ModEntities.IStateMachine;
import BlockPower.ModEntities.ModEntities;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.Commons;
import BlockPower.Util.EffectSender;
import BlockPower.Util.SkillLock.SkillLockManager;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static BlockPower.Util.Commons.applyDamage;
import static BlockPower.Util.EffectSender.broadcastScreenShake;

public class DropAnvilEntity extends Entity implements IStateMachine<DropAnvilEntity.AnvilState> {
    private int onGroundLifeTime = 100;

    private int onSkyLifeTime = 600;

    private final ServerPlayer player;

    private final Random r = new Random();

    private final Logger LOGGER = LoggerFactory.getLogger(DropAnvilEntity.class);

    private static final TimerManager timerManager = TimerManager.getInstance(false);

    private static final TaskManager taskManager = TaskManager.getInstance(false);

    private static final SkillLockManager skillLockManager = SkillLockManager.getInstance();

    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(DropAnvilEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<java.util.Optional<java.util.UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(DropAnvilEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private boolean isPlacedBelow = false;

    private boolean isPlayerStandingOnAnvil = false;

    private boolean lastTickIsPlayerStandingOnAnvil = false;

    public enum AnvilState {
        INITIALIZING, //初始化逻辑
        ANIMATING,//动画逻辑
        DROPPING,//正常坠落逻辑
        ENDING//结束逻辑
    }

    @Override
    protected void defineSynchedData() {
        // 在构造时注册DataAccessor并设置默认State
        this.getEntityData().define(DATA_STATE, AnvilState.INITIALIZING.ordinal());
        this.getEntityData().define(DATA_OWNER_UUID, Optional.empty());
    }

    public DropAnvilEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
    }

    public DropAnvilEntity(ServerPlayer player) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.player = player;
        this.getEntityData().set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
    }

    public DropAnvilEntity(ServerPlayer player, double x, double y, double z) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.setPos(x, y, z);
        this.player = player;
        this.getEntityData().set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
    }

    @Override
    public void tick() {
        super.tick();
        handleAnvilDiscard();
        handleAnvilMovement();
        if (!this.level().isClientSide) {
            updateState();
            handlePlayerSneak();
        }
    }

    private void handlePlayerSneak() {
        if (this.player.isShiftKeyDown()) {
            this.isPlayerStandingOnAnvil = false;
        }

        boolean stateChanged = this.lastTickIsPlayerStandingOnAnvil != this.isPlayerStandingOnAnvil;
        if (stateChanged) {
            EffectSender.sendPlayerSneak(this.player, this.isPlayerStandingOnAnvil);
        }

        if (this.isPlayerStandingOnAnvil) {
            taskManager.runOnce(this, "reset_speed", () -> {
                player.setDeltaMovement(Vec3.ZERO);
                player.noPhysics = true;
                player.setNoGravity(true);
            });
            //TODO 解决玩家和铁砧距离越来越远问题
            Vec3 targetPosition = new Vec3(this.getX(), this.getY() + 3, this.getZ());
            // 计算从玩家当前位置指向目标位置的矢量
            Vec3 desiredVelocity = targetPosition.subtract(player.position());
            // 将这个矢量直接设置为玩家的运动矢量
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), desiredVelocity));
        } else {
            taskManager.runOnce(this, "reset_player", () -> {
                skillLockManager.unlock(player);
                player.noPhysics = false;
                player.setNoGravity(false);
            });
        }

        this.lastTickIsPlayerStandingOnAnvil = this.isPlayerStandingOnAnvil;
    }

    private void updateState() {
        AnvilState anvilState = getState();
        if (this.onGround()) {
            setState(AnvilState.ENDING);
        }

        //如果不是初始化状态，且速度大于0.2，进入掉落状态
        if (getState() != AnvilState.INITIALIZING) {
            if (this.getDeltaMovement().length() > 0.2) {
                setState(AnvilState.DROPPING);
            } else {
                if (timerManager.isTimerCyclingDue(this, "ending", 5)) {
                    setState(AnvilState.ENDING);
                }
            }
        }

        switch (anvilState) {
            case INITIALIZING:
                if (!timerManager.isTimerCyclingDue(this, "initializing", 5)) {
                    isPlayerStandingOnAnvil = true;
                } else {
                    setState(AnvilState.DROPPING);
                }
                break;
            case DROPPING:
                hurtEntity();
                break;
            case ENDING:
                break;
        }
    }


    private void hurtEntity() {
        List<Entity> entityList = applyDamage(this, player, 10F, 9, ModSounds.ANVIL_SOUND.get());
        Commons.knockBackEntity(this, entityList, 1.5);
        if (!entityList.isEmpty()) {
            broadcastScreenShake(this, 6, 3f, 15, 7);
            //触发一次卡帧动画以后不再出现卡帧动画效果
//            taskManager.runOnce(this, "hitStop", () -> {
//                sendHitStop(5, player, this);
//            });
        }
    }

    private void handleAnvilDiscard() {
        if (this.position().y < -64) {
            this.discard();
        }

        if (this.onGround()) {
            onSkyLifeTime = 600;
            onGroundLifeTime--;
        } else {
            onGroundLifeTime = 100;
            onSkyLifeTime--;
        }

        if (onSkyLifeTime <= 0 || onGroundLifeTime <= 0) {
            isPlayerStandingOnAnvil = false;
            if (player != null) {
                player.noPhysics = false;
                player.setNoGravity(false);
            }
            this.discard();
        }
    }

    private void handleAnvilMovement() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.08, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));

        if (this.onGround()) {
            taskManager.runOnce(this, "playSound", () -> {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ANVIL_LAND,
                        SoundSource.PLAYERS, 0.5f, r.nextFloat(0.5f) + 0.8f);
            });
        }
    }

    public static void createDropAnvil(ServerPlayer player) {
        DropAnvilEntity dropAnvil = new DropAnvilEntity(player);
        Vec3 spawnPos = player.position();
        if (!player.onGround()) {
            player.swing(InteractionHand.MAIN_HAND, true);
            dropAnvil.setPlacedBelow(true);
            taskManager.runTaskAfterTicks(5, () -> {
                dropAnvil.setPos(spawnPos.x, spawnPos.y - 3, spawnPos.z);
                player.level().addFreshEntity(dropAnvil);
            });
        } else {
//            dropAnvil.setPos(spawnPos.x, spawnPos.y - 1, spawnPos.z);
        }
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
    public boolean canCollideWith(@NotNull Entity entity) {
        return entity instanceof DropAnvilEntity || entity == this.player;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    public boolean isPlacedBelow() {
        return isPlacedBelow;
    }

    public void setPlacedBelow(boolean placedBelow) {
        isPlacedBelow = placedBelow;
    }

    @Override
    public EntityDataAccessor<Integer> getStateDataAccessor() {
        return DATA_STATE;
    }

    @Override
    public AnvilState[] getStateEnumValues() {
        return AnvilState.values();
    }
}
