package BlockPower.ModEntities;

import BlockPower.ModEffects.EffectManager.EffectSender;
import BlockPower.ModEffects.EffectManager.ModEffectManager;
import BlockPower.ModEffects.ServerEffect.AttractEntityEffect;
import BlockPower.ModItems.PixelCore.PixelCoreSkillState;
import BlockPower.ModParticles.GlowingSparkParticleOptions;
import BlockPower.ModParticles.ModParticles;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 烟花实体，用于跟踪并爆炸
 */
public class FirecrackerEntity extends Entity implements IStateMachine<FirecrackerEntity.FirecrackerState> {
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(FirecrackerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(FirecrackerEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final TaskManager taskManager = TaskManager.getInstance(false);
    private static final int LIFE_TICK = 20;
    private int currLifeTick = 0;

    private static final float MOVE_SPEED = 0.4f;
    private static final float EXPLOSION_RADIUS = 7.0f;
    private static final float EXPLOSION_DAMAGE = 8.0f;

    private final Player player;

    private static final List<Vector3f> EXPLOSION_COLORS = List.of(
            new Vector3f(1.0f, 0.1f, 0.1f),
            new Vector3f(0.1f, 1.0f, 0.1f),
            new Vector3f(0.1f, 0.1f, 1.0f)
    );

    // 白色粒子
    private static final GlowingSparkParticleOptions WHITE_PARTICLE = new GlowingSparkParticleOptions(new Vector3f(1.0f, 1.0f, 1.0f));

    public FirecrackerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
    }

    public FirecrackerEntity(Player player) {
        super(ModEntities.FIRECRACKER_ENTITY.get(), player.level());
        this.player = player;
        this.getEntityData().set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
    }

    @Override
    public void tick() {
        super.tick();
        currLifeTick++;
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        // 生成烟花尾迹
        this.level().addParticle(ParticleTypes.FIREWORK,
                this.getX(), this.getY(), this.getZ(),
                0.0D, 0.0D, 0.0D);
        if (!this.level().isClientSide()) {
            handleStateChange();
            handleStateAction();
        }

    }


    public enum FirecrackerState {
        INIT,
        TRACKING,
        ENDING
    }

    /**
     * 处理状态之间的转换逻辑
     */
    @Override
    public void handleStateChange() {
        if (currLifeTick >= LIFE_TICK) {
            setState(FirecrackerState.ENDING);
            return;
        }

        FirecrackerState state = getState();

        switch (state) {
            case INIT:
                // 像素核心切换为烟花弩
                Commons.changePixelCoreNBT(player, PixelCoreSkillState.CROSSBOW, 2.0f, null);
                // 10tick后切换为普通弩
                taskManager.runTaskAfterTicks(10, () -> Commons.changePixelCoreNBT(player, PixelCoreSkillState.CROSSBOW, 1.0f, null));
                setState(FirecrackerState.TRACKING);
                break;
            case TRACKING:
                // 当发生水平或垂直碰撞时，进入结束状态
                if (this.horizontalCollision || this.verticalCollision) {
                    setState(FirecrackerState.ENDING);
                    return;
                }

                // 当有实体进入近距离检测范围时，进入结束状态
                List<Entity> detectedEntity = Commons.detectEntity(this, 2, player);
                if (!detectedEntity.isEmpty()) {
                    setState(FirecrackerState.ENDING);
                }
                break;
        }
    }

    /**
     * 处理特定状态下每一 tick 的行为
     */
    @Override
    public void handleStateAction() {
        FirecrackerState state = getState();

        switch (state) {
            case TRACKING:
                List<Entity> targetList = Commons.detectEntity(this, 9, player);
                if (!targetList.isEmpty()) {
                    Entity targetEntity = targetList.get(0);
                    ModEffectManager.addEffect(targetEntity, new AttractEntityEffect(2, targetEntity, this));
                    this.setDeltaMovement(this.getDeltaMovement().scale(1.1));
                }
                break;
        }
    }

    /**
     * 处理进入新状态时的瞬时逻辑
     */
    @Override
    public void onStateChange(FirecrackerState newState, FirecrackerState oldState) {
        switch (newState) {
            case TRACKING:
                // 播放引信声音
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1.0f, 1.0f);
                break;
            case ENDING:
                explode();
                EffectSender.broadcastScreenShake(this, 10, 1f, 11.0, 6.0);
                this.discard();
                break;
        }
    }


    private void explode() {
        if (this.level().isClientSide || this.player == null) return;

        ServerLevel serverLevel = (ServerLevel) this.level();
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // 爆炸闪光
        serverLevel.sendParticles(ParticleTypes.FLASH,
                x, y, z, 1, 0.0, 0.0, 0.0, 0.0);

        // 爆炸音效
        serverLevel.playSound(null, x, y, z,
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 2.0f, (1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F) * 0.7F);
        serverLevel.playSound(null, x, y, z,
                SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.NEUTRAL, 1.0f, (1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F) * 0.7F);


        // 球形粒子扩散
        float explosionSpeed = 0.5f;
        int PARTICLE_COUNT = 400;

        // 选择主颜色并创建自定义Options
        Vector3f chosenColor = EXPLOSION_COLORS.get(this.random.nextInt(EXPLOSION_COLORS.size()));
        GlowingSparkParticleOptions chosenColorOptions = new GlowingSparkParticleOptions(chosenColor);

        // 发送 2/3 的主颜色粒子
        int mainParticleCount = PARTICLE_COUNT * 2 / 3;
        serverLevel.sendParticles(chosenColorOptions,
                x, y, z,
                mainParticleCount,
                0.0D,
                0.0D,
                0.0D,
                explosionSpeed);

        // 发送 1/3 的白色粒子
        int whiteParticleCount = PARTICLE_COUNT / 3;
        serverLevel.sendParticles(WHITE_PARTICLE,
                x, y, z,
                whiteParticleCount,
                0.0D,
                0.0D,
                0.0D,
                explosionSpeed);


        // 造成伤害和击退
        List<Entity> entityList = Commons.applyDamage(this, player, EXPLOSION_DAMAGE, EXPLOSION_RADIUS, null);
        Commons.knockBackEntity(this, entityList, 1.5);
    }




    public static void spawnFirecrackerEntity(Player player) {
        FirecrackerEntity firecracker = new FirecrackerEntity(player);
        // 设置初始位置为玩家前方1格
        Vec3 spawnPos = player.getEyePosition().add(player.getLookAngle().scale(1.0));
        firecracker.setPos(spawnPos);
        // 设置初始速度
        firecracker.setDeltaMovement(player.getLookAngle());
        player.level().addFreshEntity(firecracker);
    }

    @Override
    public @NotNull EntityDataAccessor<Integer> getStateDataAccessor() {
        return DATA_STATE;
    }

    @Override
    public @NotNull FirecrackerState[] getStateEnumValues() {
        return FirecrackerState.values();
    }

    @Override
    protected void defineSynchedData() {
        // 在构造时注册DataAccessor并设置默认State
        this.getEntityData().define(DATA_STATE, FirecrackerState.INIT.ordinal());
        this.getEntityData().define(DATA_OWNER_UUID, Optional.empty());
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {
    }
}