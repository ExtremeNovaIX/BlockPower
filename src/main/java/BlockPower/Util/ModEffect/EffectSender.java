package BlockPower.Util.ModEffect;

import BlockPower.ModMessages.S2CPacket.HitStopPacket_S2C;
import BlockPower.ModMessages.S2CPacket.ShakePacket_S2C;
import BlockPower.ModMessages.S2CPacket.SneakPacket_S2C;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static BlockPower.ModMessages.ModMessages.sendToPlayer;

public class EffectSender {
    private static final TaskManager taskManager = TaskManager.getInstance(false);

    /**
     * 触发玩家屏幕震动
     *
     * @param duration     震动持续时间
     * @param strength     震动强度
     * @param serverPlayer 目标玩家
     */
    public static void sendScreenShake(int duration, float strength, ServerPlayer serverPlayer) {
        sendToPlayer(new ShakePacket_S2C(duration, strength), serverPlayer);
    }


    /**
     * 向指定区域内的所有玩家广播屏幕震动效果，强度随距离衰减。
     *
     * @param mainEntity  震源实体，效果以此为中心。
     * @param duration    震动持续时间 (tick)。
     * @param maxStrength 在震源中心处的最大震动强度。
     * @param maxRadius   震动能够影响的最大半径（格）。
     * @param minRadius   震动不衰减的距离。
     */
    public static void broadcastScreenShake(Entity mainEntity, int duration, float maxStrength, double maxRadius, double minRadius) {
        if (!(mainEntity.level() instanceof ServerLevel) || minRadius > maxRadius) {
            return;
        }

        Vec3 mainPos = mainEntity.position();

        Commons.detectEntity(mainEntity, maxRadius, null).forEach(entity -> {
            if (entity instanceof ServerPlayer player) {
                // 计算玩家与震源的距离
                double distance = player.position().distanceTo(mainPos);

                if (distance <= minRadius) {
                    sendScreenShake(duration, maxStrength, player);
                } else if (distance <= maxStrength) {
                    // 根据距离计算强度衰减（线性衰减）
                    // 距离越近，(1 - distance / maxRadius) 的值越接近1，强度越高
                    // 距离越远，值越接近0，强度越低
                    float calculatedStrength = (float) (maxStrength * Math.max(0, 1 - (distance / maxRadius)));

                    // 如果计算出的强度大于0，则向该玩家发送数据包
                    if (calculatedStrength > 0) {
                        sendToPlayer(new ShakePacket_S2C(duration, calculatedStrength), player);
                    }
                }
            }
        });
    }

    /**
     * 触发卡帧效果
     *
     * @param duration     卡帧持续时间
     * @param serverPlayer 目标玩家
     * @param skillEntity  触发卡帧的技能实体
     */
    public static void sendHitStop(int duration, ServerPlayer serverPlayer, @Nullable Entity skillEntity) {
        sendToPlayer(new HitStopPacket_S2C(duration), serverPlayer);
        if (skillEntity != null) {
            Vec3 deltaMovement = skillEntity.getDeltaMovement();
            skillEntity.setDeltaMovement(Vec3.ZERO);
            taskManager.runTaskAfterTicks(duration, () -> {
                skillEntity.setDeltaMovement(deltaMovement);
            });
        }
    }

    /**
     * 触发玩家潜行效果
     *
     * @param serverPlayer 目标玩家
     * @param state        潜行状态
     */
    public static void sendPlayerSneak(ServerPlayer serverPlayer, boolean state) {
        sendToPlayer(new SneakPacket_S2C(state), serverPlayer);
    }

}
