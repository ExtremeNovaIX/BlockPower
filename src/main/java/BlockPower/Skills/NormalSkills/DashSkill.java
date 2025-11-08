package BlockPower.Skills.NormalSkills;

import BlockPower.Skills.ComboSkills.ComboManager.Server.PlayerComboManager;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DashSkill implements IPacketSerializableSkill {
    private static final TaskManager taskManager = TaskManager.getInstance(false);

    private String keyResult;

    public DashSkill() {
    }

    public DashSkill(String keyResult) {
        this.keyResult = keyResult;
    }

    @Override
    public @NotNull String getSkillName() {
        return "Dash";
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
        if (Commons.isSpectatorOrCreativeMode(player)) return SkillExecutionResult.fail("Player is Spectator or Creative Mode");
        taskManager.runOnceWithCooldown(player, "dashingCoolDown", 10, () -> {
            Vec3 lookAngle = player.getLookAngle().normalize();
            //根据玩家的最后方向输入决定冲刺方向
            Vec3 newVec = switch (keyResult) {
                case "a" -> new Vec3(lookAngle.z, 0, -lookAngle.x);
                case "s" -> new Vec3(-lookAngle.x, 0, -lookAngle.z);
                case "d" -> new Vec3(-lookAngle.z, 0, lookAngle.x);
                default -> new Vec3(lookAngle.x, 0, lookAngle.z);//默认向前
            };
            Vec3 finalVec = new Vec3(newVec.x, 0, newVec.z).multiply(1.3, 0, 1.3);
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), finalVec));
            player.setSprinting(true);
        });
        return SkillExecutionResult.success();
    }



    public String getKeyResult() {
        return keyResult;
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
