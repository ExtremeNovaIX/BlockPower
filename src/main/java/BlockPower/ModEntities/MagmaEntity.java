package BlockPower.ModEntities;

import BlockPower.Util.ModEffects.ClientEffect.ScreenShakeEffect;
import BlockPower.Util.ModEffects.ServerEffect.AttractEntityEffect;
import BlockPower.Util.ModEffects.ServerEffect.CloudTrailEffect;
import BlockPower.Util.ModEffects.ServerEffect.UnBalanceEffect;
import BlockPower.ModSounds.ModSounds;
import net.minecraft.sounds.SoundEvents;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffects.ModEffectManager;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagmaEntity extends Entity implements IStateMachine<MagmaEntity.MagmaEntityState> {
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(MagmaEntity.class, EntityDataSerializers.INT);

    private final Player player;
    private int MAX_LIFE_TICK = 25;
    private int MAX_ACTIVE_TICK = 25;
    private int activeTick = 0;
    private int currentTick = 0;

    public enum MagmaEntityState {
        INIT,
        SEARCHING,
        ACTIVE,
        END
    }

    @Override
    public void tick() {
        super.tick();
        currentTick++;
        if (!this.level().isClientSide) {
            handleStateChange();
            handleStateAction();
        }
    }

    @Override
    public void handleStateAction() {
        MagmaEntityState currState = getState();
        switch (currState) {
            case INIT:
                break;
            case SEARCHING, ACTIVE:
                // 检测实体并添加吸引和失衡效果
                List<Entity> entities = Commons.detectEntity(this, 5, player);
                entities.forEach(entity -> {
                    ModEffectManager.addEffect(entity, new AttractEntityEffect(6, this, entity));
                    ModEffectManager.addEffect(entity, new UnBalanceEffect(entity, 6));
                });
                if (currState == MagmaEntityState.ACTIVE) {
                    activeTick++;
                    entities.forEach(entity -> {
                        if (entity instanceof LivingEntity livingEntity) {
                            // 对实体造成火焰伤害并设置着火时间为5秒
                            livingEntity.hurt(livingEntity.level().damageSources().onFire(), 2F);
                            livingEntity.setSecondsOnFire(5);
                        }
                    });
                    spawnParticles();
                }
                break;

            case END:
                // 结束时，给实体添加云迹和失衡效果
                List<Entity> e = Commons.detectEntity(this, 3, player);
                e.forEach(entity -> {
                    ModEffectManager.addEffect(entity, new CloudTrailEffect(entity, 20));
                    ModEffectManager.addEffect(entity, new UnBalanceEffect(entity, 10));
                });
                // 应用伤害
                Commons.applyDamage(this, player, 10, 3, null);
                Commons.knockBackEntity(this, e, 1.8F);

                this.discard();

                // 生成岩浆块破坏粒子
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                            this.getX(), this.getY(), this.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.15D);
                }
                break;
        }
    }

    private void spawnParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                // 随机偏移量
                double offsetX = (Math.random() - 0.5) * 2.0;
                double offsetY = (Math.random() - 0.5) * 2.0;
                double offsetZ = (Math.random() - 0.5) * 2.0;

                // 生成火焰粒子
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        3, // 每个位置生成3个粒子
                        0.1, 0.1, 0.1, // 速度偏移
                        0.02); // 速度

                // 生成烟雾粒子
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        2, // 每个位置生成2个粒子
                        0.05, 0.05, 0.05, // 速度偏移
                        0.01); // 速度
            }
        }
    }

    @Override
    public void handleStateChange() {
        if (currentTick >= MAX_LIFE_TICK) {
            setState(MagmaEntityState.END);
            return;
        }

        switch (getState()) {
            case INIT:
                setState(MagmaEntityState.SEARCHING);
                break;
            case SEARCHING:
                // 在近距离搜索实体如果检测到实体，切换到ACTIVE状态
                List<Entity> searchList = Commons.detectEntity(this, 1.5, player);
                if (!searchList.isEmpty()) setState(MagmaEntityState.ACTIVE);
                break;
            case ACTIVE:
                if (activeTick >= MAX_ACTIVE_TICK) {
                    setState(MagmaEntityState.END);
                }
                break;
            case END:
                break;
        }
    }

    @Override
    public void onStateChange(MagmaEntityState newState, MagmaEntityState oldState) {
        switch (newState) {
            case INIT:
                break;
            case SEARCHING, END:
                player.swing(InteractionHand.MAIN_HAND, true);
                // 播放放置音效
                player.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                break;
            case ACTIVE:
                // 播放岩浆块音效
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.MAGMA_BLOCK_SOUND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                ModEffectManager.addToAllAround(new ScreenShakeEffect(20, 1.2f), this.position(), this.level(), 8.0D);
                break;
        }
    }

    public MagmaEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        player = null;
    }


    public MagmaEntity(Player player) {
        super(ModEntities.MAGMA_ENTITY.get(), player.level());
        this.player = player;
    }

    public static void spawnMagmaEntity(Player player) {
        MagmaEntity magmaEntity = new MagmaEntity(player);
        Vec3 pos = player.position();
        magmaEntity.setPos(pos.x, pos.y + 2.5, pos.z);
        player.level().addFreshEntity(magmaEntity);
    }

    @Override
    public @NotNull EntityDataAccessor<Integer> getStateDataAccessor() {
        return DATA_STATE;
    }

    @Override
    public MagmaEntityState[] getStateEnumValues() {
        return MagmaEntityState.values();
    }

    @Override
    protected void defineSynchedData() {
        // 在构造时注册DataAccessor并设置默认State
        this.getEntityData().define(DATA_STATE, MagmaEntityState.INIT.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
}
