package BlockPower.ModParticles;

import BlockPower.Util.MathUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

/**
 * 粒子行为组件的抽象基类。
 * 定义了粒子生命周期中可以注入自定义逻辑的各个阶段。
 * 参考自 Mowzie's Mobs 源码。
 */
public abstract class ParticleComponent {

    /**
     * 当粒子被创建时调用一次。
     * @param particle 拥有此组件的粒子实例
     */
    public void init(AdvancedParticle particle) {
    }

    /**
     * 在粒子每 tick 更新位置之前调用。
     * @param particle 拥有此组件的粒子实例
     */
    public void preUpdate(AdvancedParticle particle) {
    }

    /**
     * 在粒子每 tick 更新位置之后调用。
     * @param particle 拥有此组件的粒子实例
     */
    public void postUpdate(AdvancedParticle particle) {
    }

    /**
     * 在粒子每帧渲染之前调用。
     * 用于根据时间更新粒子的颜色、大小等渲染属性。
     * @param particle 拥有此组件的粒子实例
     * @param partialTicks 渲染的部分 tick，用于平滑动画
     */
    public void preRender(AdvancedParticle particle, float partialTicks) {
    }

    /**
     * 在粒子每帧渲染之后调用。
     * 可用于绘制额外的几何图形或效果。
     * @param particle 拥有此组件的粒子实例
     * @param buffer 顶点缓冲区
     * @param renderInfo 渲染信息
     * @param partialTicks 渲染的部分 tick
     * @param lightmap 光照贴图值
     */
    public void postRender(AdvancedParticle particle, VertexConsumer buffer, Camera renderInfo, float partialTicks, int lightmap) {
    }

    /**
     * 动画数据的抽象基类。
     * 用于评估粒子在特定时间点 (t) 的属性值。
     * t 通常是粒子的生命周期百分比 (0.0f 到 1.0f)。
     * 参考自 Mowzie's Mobs 源码。
     */
    public abstract static class AnimData {
        public float evaluate(float t) {
            return 0;
        }
    }

    /**
     * 关键帧轨道。
     * 允许在特定时间点 (times) 定义特定值 (values)，并在它们之间进行线性插值。
     */
    public static class KeyTrack extends AnimData {
        float[] values;
        float[] times;

        public KeyTrack(float[] values, float[] times) {
            this.values = values;
            this.times = times;
        }

        @Override
        public float evaluate(float t) {
            if (values.length != times.length) return 0;
            // 遍历关键帧
            for (int i = 0; i < times.length; i++) {
                float time = times[i];
                if (t == time) return values[i];
                else if (t < time) {
                    if (i == 0) return values[0]; // 在第一个关键帧之前
                    // 在两个关键帧之间进行线性插值
                    float a = (t - times[i - 1]) / (time - times[i - 1]);
                    return values[i - 1] * (1 - a) + values[i] * a;
                }
                else {
                    if (i == values.length - 1) return values[i]; // 在最后一个关键帧之后
                }
            }
            return 0;
        }

        /**
         * 辅助方法：创建一个从 0.0 到 1.0 的简单线性插值。
         */
        public static KeyTrack startAndEnd(float startValue, float endValue) {
            return new KeyTrack(new float[] {startValue, endValue}, new float[] {0, 1});
        }

        /**
         * 辅助方法：使用 easeInCubic 曲线创建轨道。
         * (需要 MathUtils.java)
         */
        public static KeyTrack easeInCubic(float startValue, float endValue) {
            float value = endValue - startValue;
            float[] f1=new float[11],f2=new float[]{0,0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f,1};
            for(int i = 0;i<11;i++){
                // (假设 MathUtils.easeInCubic 存在，如果不存在，则此方法无效)
                // f1[i] = MathUtils.easeInCubic(f2[i])*value + startValue;
                // 使用 easeInQuint 作为替代
                f1[i] = MathUtils.easeInQuint(f2[i])*value + startValue;
            }
            return new KeyTrack(f1, f2);
        }

        /**
         * 辅助方法：使用 easeOutCirc 曲线创建轨道。
         * (需要 MathUtils.java)
         */
        public static KeyTrack easeOutCirc(float startValue, float endValue) {
            float value = endValue - startValue;
            float[] f1=new float[11],f2=new float[]{0,0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f,1};
            for(int i = 0;i<11;i++){
                f1[i] = MathUtils.easeOutCirc(f2[i])*value + startValue;
            }
            return new KeyTrack(f1, f2);
        }
    }

    /**
     * 振荡器。
     * 使用余弦函数在两个值之间来回振荡。
     */
    public static class Oscillator extends AnimData {
        float value1, value2;
        float frequency;
        float phaseShift;

        public Oscillator(float value1, float value2, float frequency, float phaseShift) {
            this.value1 = value1;
            this.value2 = value2;
            this.frequency = frequency;
            this.phaseShift = phaseShift;
        }

        @Override
        public float evaluate(float t) {
            float a = (value2 - value1) / 2f;
            return (float) (value1 + a + a * Math.cos(t * frequency + phaseShift));
        }
    }

    /**
     * 恒定值。
     * 无论 t 为何值，始终返回同一个值。
     */
    public static class Constant extends AnimData {
        float value;

        public Constant(float value) {
            this.value = value;
        }

        @Override
        public float evaluate(float t) {
            return value;
        }
    }

    /**
     * 辅助方法：创建 Constant 实例。
     */
    public static Constant constant(float value) {
        return new Constant(value);
    }

    /**
     * 属性控制组件。
     * 随时间 (t) 评估 AnimData，并将结果应用于粒子的特定属性。
     * 参考自 Mowzie's Mobs 源码。
     */
    public static class PropertyControl extends ParticleComponent {
        /**
         * 定义了可以被此组件控制的粒子属性。
         */
        public enum EnumParticleProperty {
            POS_X, POS_Y, POS_Z,
            MOTION_X, MOTION_Y, MOTION_Z,
            RED, GREEN, BLUE, ALPHA,
            SCALE,
            YAW, PITCH, ROLL, // 适用于 EulerAngles
            PARTICLE_ANGLE, // 适用于 FaceCamera
            AIR_DRAG
        }

        private final AnimData animData;
        private final EnumParticleProperty property;
        private final boolean additive; // 值是叠加的还是覆盖的

        /**
         * @param property 要控制的属性
         * @param animData 用于评估值的动画数据
         * @param additive 如果为 true，值将添加到现有值上；如果为 false，将直接设置(覆盖)值
         */
        public PropertyControl(EnumParticleProperty property, AnimData animData, boolean additive) {
            this.property = property;
            this.animData = animData;
            this.additive = additive;
        }

        /**
         * 粒子初始化时，应用 t=0 时的值。
         */
        @Override
        public void init(AdvancedParticle particle) {
            float value = animData.evaluate(0);
            applyUpdate(particle, value);
            applyRender(particle, value);
        }

        /**
         * 渲染前，根据渲染插值时间 (t) 更新渲染属性 (如颜色, 缩放)。
         */
        @Override
        public void preRender(AdvancedParticle particle, float partialTicks) {
            float ageFrac = (particle.getAge() + partialTicks) / particle.getLifetime();
            float value = animData.evaluate(ageFrac);
            applyRender(particle, value);
        }

        /**
         * Tick 更新前，根据 tick 时间 (t) 更新物理属性 (如运动, 位置)。
         */
        @Override
        public void preUpdate(AdvancedParticle particle) {
            float ageFrac = particle.getAge() / particle.getLifetime();
            float value = animData.evaluate(ageFrac);
            applyUpdate(particle, value);
        }

        /**
         * 将值应用于物理相关的属性。
         */
        private void applyUpdate(AdvancedParticle particle, float value) {
            switch (property) {
                case POS_X:
                    // 修正：直接访问 public 字段 x, y, z
                    if (additive) particle.setPos(particle.getX() + value, particle.getY(), particle.getZ());
                    else particle.setPos(value, particle.getY(), particle.getZ());
                    break;
                case POS_Y:
                    if (additive) particle.setPos(particle.getX(), particle.getY() + value, particle.getZ());
                    else particle.setPos(particle.getX(), value, particle.getZ());
                    break;
                case POS_Z:
                    if (additive) particle.setPos(particle.getX(), particle.getY(), particle.getZ() + value);
                    else particle.setPos(particle.getX(), particle.getY(), value);
                    break;
                case MOTION_X:
                    if (additive) particle.setMotion(particle.getXd() + value, particle.getYd(), particle.getZd());
                    else particle.setMotion(value, particle.getYd(), particle.getZd());
                    break;
                case MOTION_Y:
                    if (additive) particle.setMotion(particle.getXd(), particle.getYd() + value, particle.getZd());
                    else particle.setMotion(particle.getXd(), value, particle.getZd());
                    break;
                case MOTION_Z:
                    if (additive) particle.setMotion(particle.getXd(), particle.getYd(), particle.getZd() + value);
                    else particle.setMotion(particle.getXd(), particle.getYd(), value);
                    break;
                case AIR_DRAG:
                    if (additive) particle.airDrag += value;
                    else particle.airDrag = value;
                    break;
                default:
                    break;
            }
        }

        /**
         * 将值应用于渲染相关的属性。
         */
        private void applyRender(AdvancedParticle particle, float value) {
            switch (property) {
                case RED:
                    if (additive) particle.red += value;
                    else particle.red = value;
                    break;
                case GREEN:
                    if (additive) particle.green += value;
                    else particle.green = value;
                    break;
                case BLUE:
                    if (additive) particle.blue += value;
                    else particle.blue = value;
                    break;
                case ALPHA:
                    if (additive) particle.alpha += value;
                    else particle.alpha = value;
                    break;
                case SCALE:
                    if (additive) particle.scale += value;
                    else particle.scale = value;
                    break;
                case YAW:
                    if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                        ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) particle.rotation;
                        if (additive) eulerRot.yaw += value;
                        else eulerRot.yaw = value;
                    }
                    break;
                case PITCH:
                    if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                        ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) particle.rotation;
                        if (additive) eulerRot.pitch += value;
                        else eulerRot.pitch = value;
                    }
                    break;
                case ROLL:
                    if (particle.rotation instanceof ParticleRotation.EulerAngles) {
                        ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) particle.rotation;
                        if (additive) eulerRot.roll += value;
                        else eulerRot.roll = value;
                    }
                    break;
                case PARTICLE_ANGLE:
                    if (particle.rotation instanceof ParticleRotation.FaceCamera) {
                        ParticleRotation.FaceCamera faceCameraRot = (ParticleRotation.FaceCamera) particle.rotation;
                        if (additive) faceCameraRot.faceCameraAngle += value;
                        else faceCameraRot.faceCameraAngle = value;
                    }
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * 属性随拖尾长度变化的组件。
     * 用于控制拖尾的淡出、缩放等。
     */
    public static class PropertyOverLength extends ParticleComponent {
        /**
         * 定义可以随拖尾长度变化的属性。
         */
        public enum EnumRibbonProperty {
            RED, GREEN, BLUE, ALPHA,
            SCALE
        }
        private final AnimData animData;
        private final EnumRibbonProperty property;

        public PropertyOverLength(EnumRibbonProperty property, AnimData animData) {
            this.animData = animData;
            this.property = property;
        }

        /**
         * @param t 沿拖尾的百分比 (0.0 = 头部, 1.0 = 尾部)
         */
        public float evaluate(float t) {
            return animData.evaluate(t);
        }

        public EnumRibbonProperty getProperty() {
            return property;
        }
    }

    /**
     * 拖尾组件。
     * 负责在每 tick 更新拖尾的位置历史数组。
     */
    public static class Trail extends ParticleComponent {
        @Override
        public void postUpdate(AdvancedParticle particle) {
            if (particle instanceof ParticleRibbon) {
                ParticleRibbon ribbon = (ParticleRibbon) particle;
                // 将所有点向后移动一位
                for (int i = ribbon.positions.length - 1; i > 0; i--) {
                    ribbon.positions[i] = ribbon.positions[i - 1];
                    ribbon.prevPositions[i] = ribbon.prevPositions[i - 1];
                }
                // 将头部 (0) 设置为粒子当前位置
                ribbon.positions[0] = new Vec3(ribbon.getX(), ribbon.getY(), ribbon.getZ());
                ribbon.prevPositions[0] = ribbon.getPrevPos();
            }
        }
    }

    /**
     * 纹理平移 (UV Scrolling) 组件。
     */
    public static class PanTexture extends ParticleComponent {
        float startOffset = 0;
        float speed = 1;

        public PanTexture(float startOffset, float speed) {
            this.startOffset = startOffset;
            this.speed = speed;
        }

        @Override
        public void preRender(AdvancedParticle particle, float partialTicks) {
            if (particle instanceof ParticleRibbon) {
                ParticleRibbon ribbon = (ParticleRibbon) particle;
                float time = (ribbon.getAge() - 1 + partialTicks) / (ribbon.getLifetime());
                float t = (startOffset + time * speed) % 1.0f;

                float uMin = ribbon.getMinUPublic();
                float uMax = ribbon.getMaxUPublic();
                float uMid = (uMax - uMin) / 2f + uMin;

                ribbon.texPanOffset = (uMid - uMin) * t;
            }
        }
    }
}