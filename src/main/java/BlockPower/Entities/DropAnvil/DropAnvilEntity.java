package BlockPower.Entities.DropAnvil;

import BlockPower.Effects.FakeItemInHandEffect;
import BlockPower.Entities.ModEntities;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.StateMachine.StateMachine;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    private final ServerPlayer player;

    private final Random r = new Random();

    private final Logger LOGGER = LoggerFactory.getLogger("DropAnvilEntity");

    private static final TimerManager timerManager = TimerManager.getInstance();

    private final StateMachine<State> stateMachine;

    private boolean isPlacedBelow = false;

    private boolean canHitStop = true;

    private boolean canPlaySound = true;

    private boolean isAnimationPlayed = false;

    private enum State {
        INITIALIZING, //初始化逻辑
        ANIMATING,//动画逻辑
        DROPPING,//正常坠落逻辑
        ENDING//结束逻辑
    }

    public DropAnvilEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.player = null;
        this.stateMachine = new StateMachine<>(this, DropAnvilEntity.class, State.class, State.INITIALIZING);
    }

    public DropAnvilEntity(ServerPlayer player) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.player = player;
        this.stateMachine = new StateMachine<>(this, DropAnvilEntity.class, State.class, State.INITIALIZING);
    }

    public DropAnvilEntity(ServerPlayer player, double x, double y, double z) {
        super(ModEntities.DROP_ANVIL.get(), player.level());
        this.setPos(x, y, z);
        this.player = player;
        this.stateMachine = new StateMachine<>(this, DropAnvilEntity.class, State.class, State.INITIALIZING);
    }

    @Override
    public void tick() {
        super.tick();
        handleAnvilDiscard();
        handleAnvilMovement();
        if (!this.level().isClientSide) {
            updateState();
        }
    }

    private void updateState() {
        State state = getState();
        if (this.onGround()) {
            setState(State.ENDING);
        }

        //如果不是初始化状态，且速度大于0.5，进入掉落状态
        if (getState() != State.INITIALIZING) {
            if (this.getDeltaMovement().length() > 0.2) {
                setState(State.DROPPING);
            } else {
                if (timerManager.isTimerCyclingDue(this, "ending", 5)) {
                    setState(State.ENDING);
                }
            }
        }

        switch (state) {
            case INITIALIZING:
                if (!timerManager.isTimerCyclingDue(this, "initializing", 5)) {
                    //TODO 改成坐标同步式的，不要骑乘
                    player.startRiding(this);
                }else{
                    setState(State.DROPPING);
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
        List<Entity> entityList = applyDamage(this, player, 1.5, 10F, 9, ModSounds.ANVIL_SOUND.get());
        if (!entityList.isEmpty()) {
            broadcastScreenShake(this, 4, 2f, 9, 5);
            //触发一次卡帧动画以后不再出现卡帧动画效果
            if (canHitStop) {
                sendHitStop(3, player, this);
                canHitStop = false;
                canPlaySound = false;
            }
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

        if (this.canPlaySound && this.onGround()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ANVIL_LAND,
                    SoundSource.PLAYERS, 5f, r.nextFloat(0.5f) + 0.8f);
            this.canPlaySound = false;
        }
    }

    public static void createDropAnvil(ServerPlayer player) {
        DropAnvilEntity dropAnvil = new DropAnvilEntity(player);
        Vec3 spawnPos = player.position();
        if (!player.onGround()) {
            //TODO 改成延迟生成铁砧
            FakeItemInHandEffect.playItemAnimation(player, new ItemStack(Items.ANVIL), 5);
            player.swing(InteractionHand.MAIN_HAND, true);
            dropAnvil.setPlacedBelow(true);
            dropAnvil.setPos(spawnPos.x, spawnPos.y - 3, spawnPos.z);
        } else {
//            dropAnvil.setPos(spawnPos.x, spawnPos.y - 1, spawnPos.z);
        }
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

    public boolean isPlacedBelow() {
        return isPlacedBelow;
    }

    public void setPlacedBelow(boolean placedBelow) {
        isPlacedBelow = placedBelow;
    }

    private DropAnvilEntity.State getState() {
        return this.stateMachine.getState();
    }

    private void setState(DropAnvilEntity.State state) {
        this.stateMachine.setState(state);
    }
}
