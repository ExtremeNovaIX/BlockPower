package BlockPower.Util;

import BlockPower.Util.Timer.TickListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientComboManager {

    public static int trackedEntityId = -1; // -1 表示没有目标
    private static final float SMOOTHING_FACTOR = 0.2f; // 平滑系数 (值越小，转动越慢越平滑；值越大，转动越快)
    private static final int trackingTime = 10;
    private static long nowTick;
    private static final float ANGLE_DEADZONE = 0.5f;//角度死区，角度差小于这个值时，不计算角度差
    private static final double POLE_DEADZONE_THRESHOLD = 0.05;//极轴死区，角度差小于这个值时，不计算角度差

    public static void setCameraTarget(int targetId) {
        trackedEntityId = targetId;
        nowTick = TickListener.getClientTicks();
    }

    public static void stopTracking() {
        trackedEntityId = -1;
    }

    public static void handleCameraTick() {
        if (trackedEntityId == -1) return; // 没有目标，什么都不做

        if (TickListener.getClientTicks() - nowTick > trackingTime) {
            stopTracking();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            stopTracking();
            return;
        }

        Entity target = mc.level.getEntity(trackedEntityId);

        // 如果目标死了、不存在了、或者玩家离目标太远，就停止追踪
        if (target == null || !target.isAlive() || mc.player.distanceToSqr(target) > 49) { // 49 = 7格的平方
            stopTracking();
            return;
        }

        Vec3 playerLookVec = mc.player.getLookAngle();
        // 计算需要朝向的目标角度
        Vec3 playerEyePos = mc.player.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        Vec3 playerToTargetVec = targetPos.subtract(playerEyePos).normalize();

        double dotProduct = playerLookVec.dot(playerToTargetVec);

        // 只追踪前方180度的锥形
        if (dotProduct <= 0) {
            // 目标不在玩家的前方视野(180度锥形)内，停止追踪
            stopTracking();
            return;
        }

        float targetYaw, targetPitch;
        float newYaw, newPitch;
        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        // 计算目标向量的水平部分长度
        double horizontalDist = Math.sqrt(playerToTargetVec.x * playerToTargetVec.x + playerToTargetVec.z * playerToTargetVec.z);

        // 检查目标是否在“极地区域”（正上方或正下方）
        if (horizontalDist < POLE_DEADZONE_THRESHOLD) {
            // 在极地区域内，冻结水平转动（Yaw），只更新垂直转动（Pitch）
            targetYaw = currentYaw; // 目标Yaw = 当前Yaw (即，不转动)
            targetPitch = (float) Mth.wrapDegrees(-Math.toDegrees(Mth.atan2(playerToTargetVec.y, horizontalDist)));

            newYaw = currentYaw; // 强制Yaw不变
            newPitch = Mth.lerp(SMOOTHING_FACTOR, currentPitch, targetPitch);

        } else {
            // 不在极地区域，正常计算所有角度
            targetYaw = (float) Mth.wrapDegrees(Math.toDegrees(Mth.atan2(playerToTargetVec.z, playerToTargetVec.x)) - 90.0F);
            targetPitch = (float) Mth.wrapDegrees(-Math.toDegrees(Mth.atan2(playerToTargetVec.y, horizontalDist)));

            // 正常插值
            newYaw = Mth.rotLerp(SMOOTHING_FACTOR, currentYaw, targetYaw);
            newPitch = Mth.lerp(SMOOTHING_FACTOR, currentPitch, targetPitch);
        }

        // 检查计算出的新角度和当前角度的差距是否已经足够小
        float yawDiff = Math.abs(Mth.wrapDegrees(newYaw - currentYaw));
        float pitchDiff = Math.abs(Mth.wrapDegrees(newPitch - currentPitch));

        if (yawDiff < ANGLE_DEADZONE && pitchDiff < ANGLE_DEADZONE) {
            mc.player.setYRot(targetYaw);
            mc.player.setXRot(targetPitch);
            stopTracking(); // 任务完成，停止追踪
            return;
        }

        //  将计算出的新角度应用给玩家
        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);
    }
}