package BlockPower.ModEntities.MagmaBlock;

import BlockPower.ModEffects.CloudTrailEffect;
import BlockPower.ModEffects.SpringAttractionEffect;
import BlockPower.ModEffects.UnBalanceEffect;
import BlockPower.ModEntities.IStateMachine;
import BlockPower.ModEntities.ModEntities;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffect.ModEffectManager;
import BlockPower.Util.TaskManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class MagmaEntity extends Entity implements IStateMachine<MagmaEntity.MagmaEntityState> {
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(MagmaEntity.class, EntityDataSerializers.INT);
    private static final TaskManager taskManager = TaskManager.getInstance(false);
    private final Player player;
    private int lifeTick;
    private int currentTick = 0;

    public enum MagmaEntityState {
        INIT,
        ACTIVE,
        END
    }

    @Override
    public void tick() {
        super.tick();
        if (getState() == MagmaEntityState.INIT) {
            setState(MagmaEntityState.ACTIVE);
        }
        handleStateChange();
        handleStateAction();
        currentTick++;
    }

    private void handleStateAction() {
        MagmaEntityState currState = getState();
        switch (currState) {
            case INIT:
                break;
            case ACTIVE:
                List<Entity> entities = Commons.detectEntity(this, 7, player);
                entities.forEach(entity -> {
                    SpringAttractionEffect springAttractionEffect = new SpringAttractionEffect(player, entity);
                    springAttractionEffect.setCOMBO_DURATION_TICKS(2);
                    ModEffectManager.addEffect(entity, springAttractionEffect);
                    ModEffectManager.addEffect(entity, new UnBalanceEffect(entity, 2));
                });
                break;
            case END:
                // 结束时，给实体添加云迹和不平衡效果
                List<Entity> e = Commons.detectEntity(this, 4, player);
                e.forEach(entity -> {
                    ModEffectManager.addEffect(entity, new CloudTrailEffect(entity, 20));
                    ModEffectManager.addEffect(entity, new UnBalanceEffect(entity, 10));
                });
                // 击退实体
                Commons.knockBackEntityUp(this, e, 0.7);

                //TODO 播放声音

                this.discard();
                //TODO 生成岩浆块破坏粒子
                break;
        }
    }

    private void handleStateChange() {
        if (currentTick >= lifeTick) {
            setState(MagmaEntityState.END);
        }
    }

    public MagmaEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        player = null;
    }


    public MagmaEntity(Player player, int lifeTick) {
        super(ModEntities.MAGMA_ENTITY.get(), player.level());
        this.player = player;
        this.lifeTick = lifeTick;
    }

    public static void spawnMagmaEntity(Player player, int lifetime) {
        MagmaEntity magmaEntity = new MagmaEntity(player, lifetime);
        Vec3 pos = player.position();
        magmaEntity.setPos(pos.x, pos.y + 2, pos.z);
        player.level().addFreshEntity(magmaEntity);
    }

    @Override
    public EntityDataAccessor<Integer> getStateDataAccessor() {
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
