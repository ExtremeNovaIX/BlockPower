package BlockPower.Mixins;

import BlockPower.Capability.IPlayerAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IPlayerAccessor {

    @Unique
    private static final EntityDataAccessor<Boolean> IS_TRIPLE_JUMPING =
            SynchedEntityData.defineId(PlayerMixin.class, EntityDataSerializers.BOOLEAN);

    protected PlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineCustomData(CallbackInfo ci) {
        this.entityData.define(IS_TRIPLE_JUMPING, false);
    }

    @Inject(method = "startFallFlying", at = @At("HEAD"), cancellable = true)
    private void onStartFallFlying(CallbackInfo ci) {
        if (this.isFlying()) {
            this.setSharedFlag(7, true);
            ci.cancel();
        }
    }

    @Override
    public boolean isFlying() {
        return this.entityData.get(IS_TRIPLE_JUMPING);
    }

    @Override
    public void setFlying(boolean isFlying) {
        this.entityData.set(IS_TRIPLE_JUMPING, isFlying);
    }
}
