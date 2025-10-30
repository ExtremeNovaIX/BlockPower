package BlockPower.ModParticles;

import BlockPower.ModParticles.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;

/**
 * 包含用于创建和管理拖尾 (Ribbon) 粒子的特定组件。
 * 参考自 Mowzie's Mobs 源码。
 */
public class RibbonComponent extends ParticleComponent {

    int length;
    ParticleType<? extends RibbonParticleData> ribbonType;
    double yaw, pitch, roll, scale, r, g, b, a;
    boolean faceCamera;
    boolean emissive;
    ParticleComponent[] components;

    /**
     * @param particle 粒子类型
     * @param length 拖尾的长度 (段数)
     * @param components 应用于拖尾粒子上的额外组件
     */
    public RibbonComponent(ParticleType<? extends RibbonParticleData> particle, int length, double yaw, double pitch, double roll, double scale, double r, double g, double b, double a, boolean faceCamera, boolean emissive, ParticleComponent[] components) {
        this.length = length;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.scale = scale;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.emissive = emissive;
        this.faceCamera = faceCamera;
        this.components = components;
        this.ribbonType = particle;
    }

    /**
     * 当宿主粒子初始化时，生成一个关联的 ParticleRibbon 实体。
     */
    @Override
    public void init(AdvancedParticle particle) {
        super.init(particle);
        if (particle != null) {

            // 复制组件数组，并额外添加 AttachToParticle 和 Trail 组件
            ParticleComponent[] newComponents = new ParticleComponent[components.length + 2];
            System.arraycopy(components, 0, newComponents, 0, components.length);
            newComponents[components.length] = new AttachToParticle(particle); // 'particle' 是 "头部" 粒子
            newComponents[components.length + 1] = new Trail();

            // 生成 ParticleRibbon 实体
            ParticleRotation rotation = faceCamera ? new ParticleRotation.FaceCamera((float) 0) : new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
            particle.getWorld().addParticle(new RibbonParticleData(ribbonType, rotation, scale, r, g, b, a, 0, particle.getLifetime() + length, emissive, length, newComponents),
                    particle.getX(), particle.getY(), particle.getZ(), 0, 0, 0);
        }
    }

    /**
     * 将 ParticleRibbon 实例附加回创建它的 AdvancedParticle。
     * [修改] 这个组件现在负责在每一 tick 将 "拖尾" 粒子的位置同步到 "头部" 粒子。
     */
    private static class AttachToParticle extends ParticleComponent {
        AdvancedParticle attachedParticle; // 这是 "头部" 粒子

        public AttachToParticle(AdvancedParticle attachedParticle) {
            this.attachedParticle = attachedParticle;
        }

        @Override
        public void init(AdvancedParticle particle) { // 'particle' 是 "拖尾" 粒子
            super.init(particle);
            // (Mowzie's Mobs 在这里有一个 ribbon 字段，我们暂时不需要)
            // attachedParticle.ribbon = (ParticleRibbon) particle;
        }

        /**
         * [新增] 在 "拖尾" 粒子更新位置之前被调用
         */
        @Override
        public void preUpdate(AdvancedParticle particle) { // 'particle' 是 "拖尾" 粒子
            if (this.attachedParticle != null && this.attachedParticle.isAlive()) {
                // 强制 "拖尾" 粒子的位置与 "头部" 粒子的位置完全相同
                particle.setPos(this.attachedParticle.getX(), this.attachedParticle.getY(), this.attachedParticle.getZ());
            } else {
                // (如果 "头部" 粒子消失了，"拖尾" 粒子将停在原地，直到它自己消失)
            }
        }
    }
}