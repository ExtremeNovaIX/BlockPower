package BlockPower.Skills.NormalSkills;

import BlockPower.Capability.IPlayerAccessor;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import BlockPower.Skills.SkillLock.LockPriority;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FlySkill implements ISkill {
    public static final String SKILL_ID = "FlySkill";
    private static final TimerManager timerManager = TimerManager.getInstance(false);

    @Override
    public @NotNull String getSkillName() {
        return "FlySkill";
    }

    @Override
    public String getSkillDescription() {
        return "";
    }

    @Override
    public int getSkillLevel() {
        return 0;
    }

    @Override
    public SkillExecutionResult triggerSkill(ServerPlayer player) {
        IPlayerAccessor accessor = (IPlayerAccessor) player;
        accessor.setFlying(true);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1.0f, 1.0f);

        Vec3 lookAngle = player.getLookAngle();
        double forwardSpeed = 1.5;
        double downwardSpeed = -0.2;
        Vec3 initialVelocity = new Vec3(lookAngle.x * forwardSpeed, downwardSpeed, lookAngle.z * forwardSpeed);
        player.setDeltaMovement(initialVelocity);
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), initialVelocity));

        timerManager.setTimer(player, SKILL_ID, 15);
        player.startFallFlying();
        SkillLockManager.lock(player, SKILL_ID, LockPriority.LOWEST);

        return SkillExecutionResult.success();
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.DIAMOND;
    }

    @Override
    public double getSkillCostAmount() {
        return 1;
    }

    @Override
    public boolean isSkillConsumeResource() {
        return true;
    }

    @Override
    public void recordCombo(ServerPlayer player) {

    }

    @Override
    public double getSkillKBPercent() {
        return 0;
    }

    @Override
    public double getSkillDamage() {
        return 0;
    }
}
