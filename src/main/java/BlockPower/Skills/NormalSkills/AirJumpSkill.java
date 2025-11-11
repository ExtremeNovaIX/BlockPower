package BlockPower.Skills.NormalSkills;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Skills.IPacketSerializable;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AirJumpSkill implements IPacketSerializable, ISkill {
    public static final String SKILL_ID = "AirJump";
    private static final TimerManager timerManager = TimerManager.getInstance(false);

    private String keyResult;

    public AirJumpSkill(String keyResult) {
        this.keyResult = keyResult;
    }

    public AirJumpSkill() {
    }

    @Override
    public @NotNull String getSkillName() {
        return "AirJump";
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
        if (!player.onGround()) {
            player.getCapability(ModCapabilities.PLAYER_AIR_JUMP_DATA).ifPresent(airJumpData -> {
                int serverJumpCount = airJumpData.getJumpCount();

                if (serverJumpCount == 1) {
                    DoubleJump(player);
                } else if (serverJumpCount == 2) {
                    if (SkillLockManager.isLocked(player, SKILL_ID)) {
                        return;
                    }
                    TripleJump(player);
                }
                airJumpData.incrementJumpCount();
            });
        }
        return SkillExecutionResult.success();
    }

    private void TripleJump(ServerPlayer player) {

    }

    private void DoubleJump(ServerPlayer player) {
        Vec3 motion;
        if (keyResult.equals("w")) {
            motion = new Vec3(player.getDeltaMovement().x * 6, 0.8, player.getDeltaMovement().z * 6);
        } else {
            motion = new Vec3(0, 1, 0);
        }
        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), motion));
        timerManager.setTimer(player, "noFallDamage", 100);
    }

    @Override
    public void writeParams(FriendlyByteBuf buf) {
        buf.writeUtf(this.keyResult);
    }

    @Override
    public void readParams(FriendlyByteBuf buf) {
        this.keyResult = buf.readUtf();
    }

    @Override
    public AllResourceType getSkillCostType() {
        return null;
    }

    @Override
    public double getSkillCostAmount() {
        return 0;
    }

    @Override
    public boolean isSkillConsumeResource() {
        return false;
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

    @Override
    public boolean isMustMainHandItemPixelCore() {
        return false;
    }
}
