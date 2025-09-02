package BlockPower.ModMessages.C2SPacket.SkillPacket;

import BlockPower.ModEvents.PlayerEvents.PlayerServerEvents;
import BlockPower.Skills.AirJumpSkill;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirJumpPacket_C2S extends AbstractSkillPacket_C2S {
    private static final TaskManager taskManager = TaskManager.getInstance();

    private static final TimerManager timerManager = TimerManager.getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(AirJumpPacket_C2S.class);

    public AirJumpPacket_C2S() {
        super(new AirJumpSkill());
    }

    public AirJumpPacket_C2S(FriendlyByteBuf buf) {
        super(new AirJumpSkill());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        if (!player.onGround() && PlayerServerEvents.getPlayerAirTicks(player) >= 3) {
            taskManager.runOnce(player, "airJump", () -> {
                LOGGER.info(" {} airJump", player.getGameProfile().getName());
                Vec3 motion = new Vec3(player.getDeltaMovement().x * 6, 0.8, player.getDeltaMovement().z * 6);
                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), motion));
                timerManager.setTimer(player, "noFallDamage", 100);
            });
        }
    }


//未启用
    //    @Override
//    protected void handleServerSide(ServerPlayer player) {
//        if (!player.onGround() && PlayerServerEvents.getPlayerAirTicks(player) >= 3) {
//            taskManager.runOnce(player, "airJump", () -> {
//                LOGGER.info(" {} airJump", player.getGameProfile().getName());
//                Vec3 playerDeltaMovement = player.getDeltaMovement();
//
//                // 计算水平速度（x和z方向的合成速度）
//                double horizontalSpeed = Math.sqrt(playerDeltaMovement.x * playerDeltaMovement.x + playerDeltaMovement.z * playerDeltaMovement.z);
//
//                // 速度阈值设置
//                final double DIRECTION_THRESHOLD = 0.1;
//                final double WALKING_SPEED = 4.3;
//                final double MIN_SPEED_THRESHOLD = WALKING_SPEED * 0.8;
//                final double MAX_SPEED_THRESHOLD = WALKING_SPEED * 1.2;
//
//                Vec3 jumpDirection;
//                double jumpStrength = 0.9;  // 基础跳跃强度
//
//                if (horizontalSpeed < DIRECTION_THRESHOLD) {
//                    // 玩家基本静止，只进行垂直跳跃，不添加水平移动
//                    jumpDirection = Vec3.ZERO;
//                    jumpStrength = 0;  // 水平方向不施加力
//                } else if (horizontalSpeed < 0.4) {
//                    // 速度过低，使用玩家视线方向
//                    Vec3 lookAngle = player.getLookAngle();
//                    jumpDirection = new Vec3(lookAngle.x, 0, lookAngle.z).normalize();
//                } else {
//                    // 使用玩家当前移动方向
//                    jumpDirection = new Vec3(playerDeltaMovement.x, 0, playerDeltaMovement.z).normalize();
//                }
//
//                // 速度增强逻辑
//                double speedMultiplier = getSpeedMultiplier(horizontalSpeed, MIN_SPEED_THRESHOLD, MAX_SPEED_THRESHOLD);
//
//                // 计算最终跳跃向量
//                Vec3 motion = new Vec3(
//                        jumpDirection.x * jumpStrength * speedMultiplier,
//                        0.8,
//                        jumpDirection.z * jumpStrength * speedMultiplier
//                );
//
//                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), motion));
//                timerManager.setTimer(player, "noFallDamage", 100);
//            });
//        }
//    }
//
//    private static double getSpeedMultiplier(double horizontalSpeed, double MIN_SPEED_THRESHOLD, double MAX_SPEED_THRESHOLD) {
//        double speedMultiplier = 1.0;
//
//        if (horizontalSpeed > MIN_SPEED_THRESHOLD) {
//            if (horizontalSpeed <= MAX_SPEED_THRESHOLD) {
//                // 线性插值：从1.2倍到1.8倍增强
//                double progress = (horizontalSpeed - MIN_SPEED_THRESHOLD) / (MAX_SPEED_THRESHOLD - MIN_SPEED_THRESHOLD);
//                speedMultiplier = 1.2 + (0.6 * progress);
//            } else {
//                // 超过最大阈值，给予最大增强
//                speedMultiplier = 1.8;
//            }
//        }
//        return speedMultiplier;
//    }
}
