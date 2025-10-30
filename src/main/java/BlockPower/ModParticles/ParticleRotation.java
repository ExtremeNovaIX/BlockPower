package BlockPower.ModParticles;

import net.minecraft.world.phys.Vec3;

/**
 * 粒子旋转逻辑的抽象基类。
 * 用于定义不同的旋转行为，如面向相机或使用欧拉角。
 * 参考自 Mowzie's Mobs 源码。
 */
public abstract class ParticleRotation {

    /**
     * 将当前 tick 的值保存到 "prev" (上一 tick) 字段中。
     * 用于渲染时的平滑插值。
     */
    public void setPrevValues() {
    }

    /**
     * 旋转模式：始终面向相机。
     * 允许粒子在面向相机的平面上额外旋转一个角度 (billboard rotation)。
     */
    public static class FaceCamera extends ParticleRotation {
        public float faceCameraAngle;
        public float prevFaceCameraAngle;

        public FaceCamera(float faceCameraAngle) {
            this.faceCameraAngle = faceCameraAngle;
        }

        @Override
        public void setPrevValues() {
            prevFaceCameraAngle = faceCameraAngle;
        }
    }

    /**
     * 旋转模式：使用欧拉角 (Yaw, Pitch, Roll)。
     * 用于创建具有固定世界旋转的粒子 (例如：冲击波圆环)。
     */
    public static class EulerAngles extends ParticleRotation {
        public float yaw, pitch, roll;
        public float prevYaw, prevPitch, prevRoll;

        public EulerAngles(float yaw, float pitch, float roll) {
            this.yaw = this.prevYaw = yaw;
            this.pitch = this.prevPitch = pitch;
            this.roll = this.prevRoll = roll;
        }

        @Override
        public void setPrevValues() {
            prevYaw = yaw;
            prevPitch = pitch;
            prevRoll = roll;
        }
    }

    /**
     * 旋转模式：朝向一个向量。
     * 用于创建朝向其运动方向的粒子 (例如：箭矢)。
     */
    public static class OrientVector extends ParticleRotation {
        public Vec3 orientation;
        public Vec3 prevOrientation;

        public OrientVector(Vec3 orientation) {
            this.orientation = this.prevOrientation = orientation;
        }

        @Override
        public void setPrevValues() {
            prevOrientation = orientation;
        }
    }
}