package BlockPower.Util.ModParticles;

import BlockPower.Client.ModRenderers.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 高级粒子基类 (引擎)。
 * 这是一个可由 "ParticleComponent" 驱动的粒子宿主。
 * 它将 tick 和 render 逻辑委托给组件执行。
 * 参考自 Mowzie's Mobs 源码。
 */
public class AdvancedParticle extends TextureSheetParticle {

    protected final ParticleComponent[] components;
    public boolean emissive;
    public float airDrag;

    public float red, green, blue, alpha;
    public float prevRed, prevGreen, prevBlue, prevAlpha;

    public float scale, prevScale, particleScale;
    public double prevMotionX, prevMotionY, prevMotionZ;

    // 粒子应如何旋转
    protected ParticleRotation rotation;

    protected AdvancedParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ,
                               double r, double g, double b, double a, double scale, double airDrag, int duration,
                               boolean emissive, boolean canCollide, ParticleRotation rotation, ParticleComponent[] components) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);

        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.red = (float) r;
        this.green = (float) g;
        this.blue = (float) b;
        this.alpha = (float) a;

        this.scale = (float) scale;
        this.airDrag = (float) airDrag;
        this.lifetime = duration;
        this.emissive = emissive;
        this.hasPhysics = canCollide;
        this.components = components;
        this.rotation = rotation;

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevRed = this.red;
        this.prevGreen = this.green;
        this.prevBlue = this.blue;
        this.prevAlpha = this.alpha;
        this.prevScale = this.scale;

        this.rotation.setPrevValues();

        for (ParticleComponent component : components) {
            component.init(this);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return emissive ? RenderType.PARTICLE_SHEET_ADDITIVE_NO_DEPTH : RenderType.PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH;
    }

    @Override
    public int getLightColor(float partialTick)
    {
        if (emissive) {
            int i = super.getLightColor(partialTick);
            int k = i >> 16 & 255;
            return 240 | k << 16;
        }
        else {
            return super.getLightColor(partialTick);
        }
    }

    @Override
    public void tick() {
        this.prevRed = this.red;
        this.prevGreen = this.green;
        this.prevBlue = this.blue;
        this.prevAlpha = this.alpha;
        this.prevScale = this.scale;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevMotionX = this.xd;
        this.prevMotionY = this.yd;
        this.prevMotionZ = this.zd;

        this.rotation.setPrevValues();

        for (ParticleComponent component : components) {
            component.preUpdate(this);
        }

        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }

        updatePosition();

        for (ParticleComponent component : components) {
            component.postUpdate(this);
        }
    }

    protected void updatePosition() {
        this.move(this.xd, this.yd, this.zd);

        if (this.onGround && this.hasPhysics)
        {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }

        this.xd *= this.airDrag;
        this.yd *= this.airDrag;
        this.zd *= this.airDrag;
    }

    /**
     * 粒子的主渲染方法。
     * 现在支持自定义旋转逻辑。
     */
    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        for (ParticleComponent component : components) {
            component.preRender(this, partialTicks);
        }

        this.alpha = Mth.lerp(partialTicks, this.prevAlpha, this.alpha);
        if (this.alpha < 0.01f) this.alpha = 0.01f;
        this.rCol = Mth.lerp(partialTicks, this.prevRed, this.red);
        this.gCol = Mth.lerp(partialTicks, this.prevGreen, this.green);
        this.bCol = Mth.lerp(partialTicks, this.prevBlue, this.blue);
        this.particleScale = Mth.lerp(partialTicks, this.prevScale, this.scale);

        Vec3 cameraPos = renderInfo.getPosition();
        float x = (float)(Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float)(Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float)(Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        // 计算旋转
        Quaternionf quaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
        if (rotation instanceof ParticleRotation.FaceCamera) {
            ParticleRotation.FaceCamera faceCameraRot = (ParticleRotation.FaceCamera) rotation;
            quaternion = renderInfo.rotation();
            float angle = Mth.lerp(partialTicks, faceCameraRot.prevFaceCameraAngle, faceCameraRot.faceCameraAngle);
            quaternion.mul(Axis.ZP.rotation(angle));

        } else if (rotation instanceof ParticleRotation.EulerAngles) {
            ParticleRotation.EulerAngles eulerRot = (ParticleRotation.EulerAngles) rotation;
            float rotY = Mth.lerp(partialTicks, eulerRot.prevYaw, eulerRot.yaw);
            float rotX = Mth.lerp(partialTicks, eulerRot.prevPitch, eulerRot.pitch);
            float rotZ = Mth.lerp(partialTicks, eulerRot.prevRoll, eulerRot.roll);

            // 使用 JOML Quaternions (与原版一致) 或 MathUtils
            quaternion.rotationXYZ(rotX, rotY, rotZ);

        } else if (rotation instanceof ParticleRotation.OrientVector) {
            ParticleRotation.OrientVector orientRot = (ParticleRotation.OrientVector) rotation;
            double vecX = Mth.lerp(partialTicks, orientRot.prevOrientation.x, orientRot.orientation.x);
            double vecY = Mth.lerp(partialTicks, orientRot.prevOrientation.y, orientRot.orientation.y);
            double vecZ = Mth.lerp(partialTicks, orientRot.prevOrientation.z, orientRot.orientation.z);

            float pitch = (float) Math.asin(-vecY);
            float yaw = (float) (Mth.atan2(vecX, vecZ));

            quaternion.rotationY(yaw);
            quaternion.mul(Axis.XP.rotation(pitch));
        }

        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F,  1.0F, 0.0F),
                new Vector3f( 1.0F,  1.0F, 0.0F),
                new Vector3f( 1.0F, -1.0F, 0.0F)
        };
        float quadSize = this.particleScale * 0.1f;

        for(int i = 0; i < 4; ++i) {
            Vector3f corner = corners[i];
            corner.rotate(quaternion); // 应用计算出的旋转
            corner.mul(quadSize);
            corner.add(x, y, z);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);

        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(light).endVertex();

        for (ParticleComponent component : components) {
            component.postRender(this, buffer, renderInfo, partialTicks, light);
        }
    }

    public float getAge() {
        return this.age;
    }

    public void setMotion(double x, double y, double z) {
        this.xd = x;
        this.yd = y;
        this.zd = z;
    }

    public Vec3 getPrevPos() {
        return new Vec3(this.xo, this.yo, this.zo);
    }

    public Level getWorld() {
        return this.level;
    }

    /**
     * 粒子工厂。
     * 负责从 ParticleData 中读取参数并实例化 AdvancedParticle。
     * 参考自 Mowzie's Mobs 源码。
     */
    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<ParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(ParticleData typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            // 从 ParticleData 中读取所有自定义参数
            ParticleRotation rotation = typeIn.getRotation();
            double scale = typeIn.getScale();
            double r = typeIn.getRed();
            double g = typeIn.getGreen();
            double b = typeIn.getBlue();
            double a = typeIn.getAlpha();
            double drag = typeIn.getAirDrag();
            int duration = (int)typeIn.getDuration(); // 将 double 转换为 int
            boolean emissive = typeIn.isEmissive();
            boolean canCollide = typeIn.getCanCollide();
            ParticleComponent[] components = typeIn.getComponents();

            // 创建粒子实例
            AdvancedParticle particle = new AdvancedParticle(worldIn, x, y, z,
                    xSpeed, ySpeed, zSpeed,
                    r, g, b, a,
                    scale, drag, duration,
                    emissive, canCollide, rotation, components);

            // 从 SpriteSet 中为粒子选择一个随机纹理
            particle.pickSprite(this.spriteSet);

            return particle;
        }
    }

    public double getXd() {
        return this.xd;
    }

    public double getYd() {
        return this.yd;
    }

    public double getZd() {
        return this.zd;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }


}