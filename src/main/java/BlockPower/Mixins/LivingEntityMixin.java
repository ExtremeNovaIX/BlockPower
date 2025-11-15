package BlockPower.Mixins;

import BlockPower.Capability.KBPercent.IKBPercentData;
import BlockPower.Capability.IPlayerAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IKBPercentData {

    @Unique
    private static final EntityDataAccessor<Float> BLOCKPOWER_KNOCKBACK_VALUE = SynchedEntityData.defineId(LivingEntityMixin.class, EntityDataSerializers.FLOAT);

    public LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void defineSynchedData(CallbackInfo ci) {
        this.entityData.define(BLOCKPOWER_KNOCKBACK_VALUE, 0.0F);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void onAfterAiStep(CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            IPlayerAccessor accessor = (IPlayerAccessor) player;
            if (accessor.isFlying()) {
                this.setSharedFlag(7, true);
            }
        }
    }

    @Override
    public EntityDataAccessor<Float> getKBPercentDataAccessor() {
        return BLOCKPOWER_KNOCKBACK_VALUE;
    }
}
