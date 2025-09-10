package BlockPower.ModMessages.C2SPacket.SkillPacket;

import BlockPower.Skills.DashSkill;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashSkillPacket_C2S extends AbstractSkillPacket_C2S {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashSkillPacket_C2S.class);
    private static final TaskManager taskManager = TaskManager.getInstance(false);
    private final String keyResult;//玩家在冲刺时按下的键

    public DashSkillPacket_C2S() {
        super(new DashSkill());
        keyResult = null;
    }

    public DashSkillPacket_C2S(FriendlyByteBuf buf) {
        super(new DashSkill());
        keyResult = buf.readUtf();
    }

    public DashSkillPacket_C2S(String keyResult) {
        super(new DashSkill());
        this.keyResult = keyResult;
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(keyResult);
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        if (Commons.isSpectatorOrCreativeMode(player)) return;
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
    }
}
