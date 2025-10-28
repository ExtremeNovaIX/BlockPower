package BlockPower.ModEntities.DropAnvil;

import BlockPower.ModEffects.ClientEffect.PlayerSneakEffect;
import BlockPower.ModEntities.IStateMachine;
import BlockPower.ModEntities.ModEntities;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Skills.SkillLock.LockPriority;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffect.EffectSender;
import BlockPower.Util.ModEffect.ModEffectManager;
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

import java.util.*;

import static BlockPower.Util.Commons.applyDamage;
import static BlockPower.Util.ModEffect.EffectSender.broadcastScreenShake;

public class DropAnvilEntity extends Entity implements IStateMachine<DropAnvilEntity.AnvilState> {
    private static final int LIFE_TICK = 100;
    private int currLifeTick = 0;

    private final ServerPlayer player;

    private String lockID;

    private final Random r = new Random();

    private static final TimerManager timerManager = TimerManager.getInstance(false);

    private static final TaskManager taskManager = TaskManager.getInstance(false);

    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(DropAnvilEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(DropAnvilEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private boolean isPlacedBelow = false;

    private boolean isPlayerStandingOnAnvil = true;

    public enum AnvilState {
        INIT,
        ANIMATING,//动画逻辑
        DROPPING,//坠落逻辑
        ON_GROUND,//在地面时的逻辑
        ENDING//结束逻辑
    }

    public DropAnvilEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
    }

    public DropAnvilEntity(ServerPlayer player) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.player = player;
        this.getEntityData().set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
        this.lockID = player.getName().getString() + "_AnvilLock:" + this.getUUID();
    }

    public DropAnvilEntity(ServerPlayer player, double x, double y, double z) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.setPos(x, y, z);
        this.player = player;
        this.getEntityData().set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
        this.lockID = player.getName().getString() + "_AnvilLock:" + this.getUUID();
    }

    @Override
    public void tick() {
        super.tick();
        currLifeTick++;
        handleAnvilMovement();
        if (!this.level().isClientSide) {
            handleStateChange();
            handlePlayerReset();
            handleStateAction();
        }
    }

    /**
     * 如果玩家脱离铁砧，则重置状态
     */
    private void handlePlayerReset() {
        if (!isPlayerStandingOnAnvil) return;

        //如果玩家按下shift，则设置骑乘状态为false
        if (this.player.isShiftKeyDown()) {
            this.isPlayerStandingOnAnvil = false;
            EffectSender.sendPlayerSneak(player, false);

            SkillLockManager.unlock(player, lockID);
            player.noPhysics = false;
            player.setNoGravity(false);
        }
    }

    @Override
    public void handleStateAction() {
        if (this.isPlayerStandingOnAnvil) {
            taskManager.runOnce(this, "reset_speed", () -> {
                player.setDeltaMovement(Vec3.ZERO);
                player.noPhysics = true;
                player.setNoGravity(true);
            });
            Vec3 targetPosition = new Vec3(this.getX(), this.getY() + 3, this.getZ());
            // 计算从玩家当前位置指向目标位置的矢量
            Vec3 desiredVelocity = targetPosition.subtract(player.position());
            // 将这个矢量直接设置为玩家的运动矢量
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), desiredVelocity));
        }

        if (getState() == AnvilState.DROPPING) {
            hurtEntity();
        }

    }

    @Override
    public void handleStateChange() {
        //如果铁砧超出世界范围，则切换到ENDING状态
        if (this.position().y < -64) {
            setState(AnvilState.ENDING);
            return;
        }

        //如果铁砧生命周期结束，则切换到ENDING状态
        if (currLifeTick >= LIFE_TICK) {
            setState(AnvilState.ENDING);
            return;
        }

        //在地面时切换到ON_GROUND状态
        if (this.onGround()) {
            setState(AnvilState.ON_GROUND);
            return;
        }

        //如果不是初始化状态或者动画状态，且速度大于0.1，进入掉落状态
        if (getState() != AnvilState.INIT && getState() != AnvilState.ANIMATING) {
            if (this.getDeltaMovement().length() > 0.1) {
                setState(AnvilState.DROPPING);
                return;
            }
        }

        AnvilState anvilState = getState();
        switch (anvilState) {
            case INIT:
                setState(AnvilState.ANIMATING);
                break;
            case ANIMATING:
                //5tick后切换到掉落状态
                if (timerManager.isTimerCyclingDue(this, "anvil_animating", 5)) {
                    setState(AnvilState.DROPPING);
                }
                break;

            case DROPPING:
                if (this.onGround()) {
                    setState(AnvilState.ON_GROUND);
                }
                break;
        }
    }

    @Override
    public void onStateChange(AnvilState newState, AnvilState oldState) {
        switch (newState) {
            case ANIMATING:
                //添加技能锁
                SkillLockManager.lock(player, lockID, LockPriority.LOWEST);
                //设置玩家站立在铁砧上
                isPlayerStandingOnAnvil = true;
                EffectSender.sendPlayerSneak(player, true);
                //切换像素核心材质为铁砧
                Commons.changePixelCoreNBT(player, 1.0F, null, null);
                break;
            case ON_GROUND:
                // 落地时播放落地音效
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ANVIL_LAND,
                        SoundSource.PLAYERS, 0.5f, r.nextFloat(0.5f) + 0.8f);
                break;
            case ENDING:
                // 重置状态逻辑
                isPlayerStandingOnAnvil = false;
                EffectSender.sendPlayerSneak(player, false);
                SkillLockManager.unlock(player, lockID);
                player.noPhysics = false;
                player.setNoGravity(false);

                this.discard();
                break;
        }
    }

    private void hurtEntity() {
        List<Entity> entityList = applyDamage(this, player, 10F, 9, ModSounds.ANVIL_SOUND.get());
        Commons.knockBackEntity(this, entityList, 1.5);
        if (!entityList.isEmpty()) {
            broadcastScreenShake(this, 6, 2f, 15, 7);
        }
    }

    private void handleAnvilMovement() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.2, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.99));
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
    protected void defineSynchedData() {
        // 在构造时注册DataAccessor并设置默认State
        this.getEntityData().define(DATA_STATE, AnvilState.INIT.ordinal());
        this.getEntityData().define(DATA_OWNER_UUID, Optional.empty());
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
    public @NotNull EntityDataAccessor<Integer> getStateDataAccessor() {
        return DATA_STATE;
    }

    @Override
    public AnvilState[] getStateEnumValues() {
        return AnvilState.values();
    }
}
