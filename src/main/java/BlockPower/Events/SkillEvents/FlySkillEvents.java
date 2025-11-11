package BlockPower.Events.SkillEvents;

import BlockPower.Capability.IPlayerAccessor;
import BlockPower.Main.Main;
import BlockPower.Skills.NormalSkills.AirJumpSkill;
import BlockPower.Skills.NormalSkills.FlySkill;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlySkillEvents {
    private static final TimerManager timerManager = TimerManager.getInstance(false);

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide() || event.phase != TickEvent.Phase.END) {
            return;
        }
        ServerPlayer player = (ServerPlayer) event.player;
        IPlayerAccessor accessor = (IPlayerAccessor) player;

        if (player.onGround()) {
            if (accessor.isFlying()) {
                accessor.setFlying(false);
                timerManager.removeTimer(player, FlySkill.SKILL_ID);
            }
        }

        if (accessor.isFlying()) {
            handleFlight(player);
        }

        if (timerManager.isTimerActive(player, FlySkill.SKILL_ID) && timerManager.isFinished(player, FlySkill.SKILL_ID, true)) {
            accessor.setFlying(false);
        }
    }

    private static void handleFlight(ServerPlayer player) {
        float lookXRot = player.getXRot(); // 垂直视角
        Vec3 newMotion = player.getDeltaMovement();
        Vec3 lookAngle = player.getLookAngle().normalize();

        // 俯冲
        if (lookXRot > 30.0F) {
            newMotion = newMotion.add(lookAngle.x * 0.15, lookAngle.y * 0.15, lookAngle.z * 0.15);
        }
        // 抬升
        else if (lookXRot < -30.0F) {
            newMotion = newMotion.add(lookAngle.x * 1.4, lookAngle.y * 1.2, lookAngle.z * 1.4);
        }

        if (newMotion.lengthSqr() > 25.0) {
            newMotion = newMotion.normalize().scale(5.0);
        }

        player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), newMotion));
    }
}
