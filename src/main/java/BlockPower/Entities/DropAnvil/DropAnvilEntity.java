package BlockPower.Entities.DropAnvil;

import BlockPower.Entities.ModEntities;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.Timer.ServerTickListener;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import static BlockPower.Util.Commons.applyDamage;
import static BlockPower.Util.PacketSender.broadcastScreenShake;
import static BlockPower.Util.PacketSender.sendHitStop;

public class DropAnvilEntity extends Entity {
    private int onGroundLifeTime = 100;
    private int onSkyLifeTime = 600;
    private ServerPlayer player;
    private final Random r = new Random();
    private final Logger LOGGER = LoggerFactory.getLogger("DropAnvilEntity");
    private boolean isActive = false;
    private boolean isPlacedBelow = false;
    private static final TimerManager timerManager = TimerManager.getInstance();
    private boolean needAnimation = true;

    public DropAnvilEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
    }

    public DropAnvilEntity(ServerPlayer player) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.player = player;
    }

    public DropAnvilEntity(ServerPlayer player, double x, double y, double z) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.setPos(x, y, z);
        this.player = player;
    }

    @Override
    public void tick() {
        super.tick();
        handleAnvilDiscard();
        handleAnvilMovement();
        if (!this.level().isClientSide) {
            if (this.onGround()) {
                needAnimation = false;
                isActive = false;
            }
            if (!this.onGround() && ServerTickListener.getTicks() % 4 == 0) {
                hurtEntity();
            }

            //TODO 完成动画
            if (!timerManager.isTimerCyclingDue(this, "needAnimation", 20)) {
                player.setPose(Pose.CROUCHING);
                player.setPos(this.getX(), this.getY() + 3, this.getZ());
            } else {
                player.setPose(Pose.STANDING);
            }

            //TODO 修复落地重复播放问题
            if (this.isActive && this.onGround()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ANVIL_LAND,
                        SoundSource.PLAYERS, 5f, r.nextFloat(0.5f) + 0.8f);
            }
        }
    }

    private void hurtEntity() {
        List<Entity> entityList = applyDamage(this, player, 1.5, 10F, 9, ModSounds.ANVIL_SOUND.get());
        if (isActive && !entityList.isEmpty()) {
            setActive(false);
            broadcastScreenShake(this, 4, 2f, 9, 5);
            sendHitStop(3, player, this);
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
            this.discard();
        }
    }

    private void handleAnvilMovement() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.08, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    public static void createDropAnvil(ServerPlayer player) {
        DropAnvilEntity dropAnvil = new DropAnvilEntity(player);
        Vec3 spawnPos = player.position();
        if (!player.onGround()) {
            dropAnvil.setPlacedBelow(true);
            dropAnvil.setPos(spawnPos.x, spawnPos.y - 3, spawnPos.z);
        } else {
//            dropAnvil.setPos(spawnPos.x, spawnPos.y - 1, spawnPos.z);
        }
        dropAnvil.setActive(true);
        player.level().addFreshEntity(dropAnvil);
    }

    @Override
    protected void defineSynchedData() {

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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isPlacedBelow() {
        return isPlacedBelow;
    }

    public void setPlacedBelow(boolean placedBelow) {
        isPlacedBelow = placedBelow;
    }
}
