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

    public DashSkillPacket_C2S() {
        super(new DashSkill());
    }

    public DashSkillPacket_C2S(FriendlyByteBuf buf) {
        super(new DashSkill());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        if (Commons.isSpectatorOrCreativeMode(player)) return;
        taskManager.runOnceWithCooldown(player, "dashingCoolDown", 10, () -> {
            Vec3 lookAngle = player.getLookAngle().normalize();
            Vec3 newVec = new Vec3(lookAngle.x, 0, lookAngle.z).multiply(1.3, 0, 1.3);
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), newVec));
            player.setSprinting(true);
        });
    }
}
