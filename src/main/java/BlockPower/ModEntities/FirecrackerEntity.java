package BlockPower.ModEntities;

import BlockPower.Skills.ComboSkills.FirecrackerComboSkill;
import BlockPower.Util.KBUtils;
import BlockPower.Util.ModEffects.ClientEffect.ScreenShakeEffect;
import BlockPower.Util.ModEffects.ModEffectManager;
import BlockPower.Util.ModEffects.ServerEffect.AttractEntityEffect;
import BlockPower.ModItems.PixelCore.PixelCoreSkillState;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.FireworkPacket_S2C;
import BlockPower.Util.ModParticles.GlowingSparkParticleOptions;
import BlockPower.Util.Commons;
import BlockPower.Util.StateMachine.IStateMachine;
import BlockPower.Util.TaskManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    private FirecrackerComboSkill skill;

    private Player player;

    private static final List<Pair<Vector3f, Vector3f>> FIREWORK_COLOR_PAIRS = List.of(
            // 火红 - 橙
            Pair.of(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.4f, 0.0f)),
            // 湖蓝 - 紫
            Pair.of(new Vector3f(0.0f, 0.4f, 1.0f), new Vector3f(0.5f, 0.0f, 1.0f)),
            // 天蓝 - 绿
            Pair.of(new Vector3f(0.4f, 0.8f, 1.0f), new Vector3f(0.1f, 0.8f, 0.1f)),
            // 紫 - 朱红
            Pair.of(new Vector3f(0.5f, 0.0f, 1.0f), new Vector3f(0.86f, 0.08f, 0.24f))
    );

    private static final Vector3f WHITE = new Vector3f(1.0f, 1.0f, 1.0f);

    public FirecrackerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public FirecrackerEntity(Player player, FirecrackerComboSkill skill) {
        super(ModEntities.FIRECRACKER_ENTITY.get(), player.level());
        this.player = player;
        this.skill = skill;
        this.getEntityData().set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
    }

    @Override
    public void tick() {
        super.tick();
        currLifeTick++;
        this.move(MoverType.SELF, this.getDeltaMovement());
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
                List<Entity> detectedEntity = Commons.aabbDetectEntity(this, 2, player);
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
                List<Entity> targetList = Commons.aabbDetectEntity(this, 9, player);
                if (!targetList.isEmpty()) {
                    Entity targetEntity = targetList.get(0);
                    ModEffectManager.addEffect(targetEntity, new AttractEntityEffect(2, targetEntity, this));
                    this.setDeltaMovement(this.getDeltaMovement().scale(1.5));
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
                ModEffectManager.addToAllAround(new ScreenShakeEffect(10, 0.8f), this.position(), this.level(), 11.0);
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

        // 随机选择烟花颜色
        int randomIndex = random.nextInt(FIREWORK_COLOR_PAIRS.size());
        Pair<Vector3f, Vector3f> colorPair = FIREWORK_COLOR_PAIRS.get(randomIndex);
        Vector3f color1 = colorPair.getFirst();
        Vector3f color2 = colorPair.getSecond();
        Vec3 pos = new Vec3(x, y, z);

        ModMessages.sendToAllAround(new FireworkPacket_S2C(pos, color1, color2), player.level().dimension(), x, y, z, 64);

        // 球形粒子扩散
        float explosionSpeed = 0.4f;
        int PARTICLE_COUNT = 100;

        Vector3f color1f = new Vector3f(color1.x, color1.y, color1.z);
        Vector3f color2f = new Vector3f(color2.x, color2.y, color2.z);

        // 创建主颜色
        GlowingSparkParticleOptions color1Options = new GlowingSparkParticleOptions(color1f);
        // 创建次要颜色
        GlowingSparkParticleOptions color2Options = new GlowingSparkParticleOptions(color2f);

        // 发送 2/3 的主颜色粒子
        int mainParticleCount = PARTICLE_COUNT * 2 / 3;
        serverLevel.sendParticles(color1Options,
                x, y, z,
                mainParticleCount,
                0.0D,
                0.0D,
                0.0D,
                explosionSpeed);

        // 发送 1/3 的次要颜色粒子
        int secondaryParticleCount = PARTICLE_COUNT - mainParticleCount;
        serverLevel.sendParticles(color2Options,
                x, y, z,
                secondaryParticleCount,
                0.0D,
                0.0D,
                0.0D,
                explosionSpeed);

        // 发送 1/3 的白色粒子
        serverLevel.sendParticles(ParticleTypes.FLASH,
                x, y, z,
                secondaryParticleCount,
                0.0D,
                0.0D,
                0.0D,
                explosionSpeed);


        // 造成伤害和击退
        List<Entity> entityList = Commons.aabbDetectEntity(this, 6, player);
        entityList.forEach(entity -> {
            boolean result = Commons.applyDamage(this, player, entity, 12f);
            if (!result) return;
            KBUtils.applyKB(this, entity, 0.5, 1.5);
        });
    }

    public static void spawnFirecrackerEntity(Player player, FirecrackerComboSkill skill) {
        FirecrackerEntity firecracker = new FirecrackerEntity(player, skill);
        // 设置初始位置为玩家前方1格
        Vec3 spawnPos = player.getEyePosition().add(player.getLookAngle().scale(1.0));
        firecracker.setPos(spawnPos);
        // 设置初始速度
        firecracker.setDeltaMovement(player.getLookAngle().scale(2));
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