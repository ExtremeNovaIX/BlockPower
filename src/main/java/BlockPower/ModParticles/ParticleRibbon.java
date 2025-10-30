package BlockPower.ModParticles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import BlockPower.ModParticles.ParticleComponent.PropertyOverLength;
import BlockPower.ModParticles.ParticleComponent.PropertyOverLength.EnumRibbonProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * 拖尾 (Ribbon) 粒子。
 * 通过连接历史位置点数组来渲染一个 "缎带" 网格。
 * 参考自 Mowzie's Mobs 源码。
 */
public class ParticleRibbon extends AdvancedParticle {

    // 存储拖尾的位置历史
    public Vec3[] positions;
    public Vec3[] prevPositions;

    // 用于纹理平移 (UV scrolling)
    public float texPanOffset;

    protected ParticleRibbon(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
                             double motionX, double motionY, double motionZ,
                             ParticleRotation rotation, double scale, double r, double g, double b, double a,
                             double drag, double duration, boolean emissive, int length, ParticleComponent[] components) {

        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motionX, motionY, motionZ, r, g, b, a, scale, drag, (int)duration, emissive, false, rotation, components);

        this.positions = new Vec3[length];
        this.prevPositions = new Vec3[length];

        // 初始化数组的第一个点 (即粒子的当前位置)
        if (this.positions.length >= 1) {
            this.positions[0] = new Vec3(this.x, this.y, this.z);
        }
        if (this.prevPositions.length >= 1) {
            this.prevPositions[0] = new Vec3(this.xo, this.yo, this.zo);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        alpha = prevAlpha + (alpha - prevAlpha) * partialTicks;
        if (alpha < 0.01) alpha = 0.01f;
        rCol = prevRed + (red - prevRed) * partialTicks;
        gCol = prevGreen + (green - prevGreen) * partialTicks;
        bCol = prevBlue + (blue - prevBlue) * partialTicks;
        particleScale = prevScale + (scale - prevScale) * partialTicks;

        for (ParticleComponent component : components) {
            component.preRender(this, partialTicks);
        }

        int j = this.getLightColor(partialTicks);

        float r =  rCol;
        float g = gCol;
        float b = bCol;
        float a = alpha;
        float scale = particleScale;
        float prevR = r;
        float prevG = g;
        float prevB = b;
        float prevA = a;
        float prevScale = scale;

        for (ParticleComponent component : components) {
            if (component instanceof PropertyOverLength) {
                PropertyOverLength pOverLength = (PropertyOverLength) component;
                float value = pOverLength.evaluate(0);
                if (pOverLength.getProperty() == EnumRibbonProperty.SCALE) {
                    prevScale *= value;
                }
                else if (pOverLength.getProperty() == EnumRibbonProperty.RED) {
                    prevR *= value;
                }
                else if (pOverLength.getProperty() == EnumRibbonProperty.GREEN) {
                    prevG *= value;
                }
                else if (pOverLength.getProperty() == EnumRibbonProperty.BLUE) {
                    prevB *= value;
                }
                else if (pOverLength.getProperty() == EnumRibbonProperty.ALPHA) {
                    prevA *= value;
                }
            }
        }

        Vec3 offsetDir = new Vec3(0, 0, 0);
        for (int index = 0; index < positions.length - 1; index++) {
            if (positions[index] == null || positions[index + 1] == null) continue;

            r = rCol;
            g = gCol;
            b = bCol;
            scale = particleScale;
            float t = ((float)index + 1) / ((float)positions.length - 1);
            float tPrev = ((float)index) / ((float)positions.length - 1);

            for (ParticleComponent component : components) {
                if (component instanceof PropertyOverLength) {
                    PropertyOverLength pOverLength = (PropertyOverLength) component;
                    float value = pOverLength.evaluate(t);
                    if (pOverLength.getProperty() == EnumRibbonProperty.SCALE) {
                        scale *= value;
                    }
                    else if (pOverLength.getProperty() == EnumRibbonProperty.RED) {
                        r *= value;
                    }
                    else if (pOverLength.getProperty() == EnumRibbonProperty.GREEN) {
                        g *= value;
                    }
                    else if (pOverLength.getProperty() == EnumRibbonProperty.BLUE) {
                        b *= value;
                    }
                    else if (pOverLength.getProperty() == EnumRibbonProperty.ALPHA) {
                        a *= value;
                    }
                }
            }

            Vec3 Vector3d = renderInfo.getPosition();
            Vec3 p1 = prevPositions[index].add(positions[index].subtract(prevPositions[index]).scale(partialTicks)).subtract(Vector3d);
            Vec3 p2 = prevPositions[index + 1].add(positions[index + 1].subtract(prevPositions[index + 1]).scale(partialTicks)).subtract(Vector3d);

            if (index == 0) {
                Vec3 moveDir = p2.subtract(p1).normalize();
                if (rotation instanceof ParticleRotation.FaceCamera) {
                    Vec3 viewVec = new Vec3(renderInfo.getLookVector());
                    offsetDir = moveDir.cross(viewVec).normalize();
                } else {
                    offsetDir = moveDir.cross(new Vec3(0, 1, 0)).normalize();
                }
                offsetDir = offsetDir.scale(prevScale);
            }

            Vec3[] aVector3d2 = new Vec3[] {offsetDir.scale(-1), offsetDir, null, null};
            Vec3 moveDir = p2.subtract(p1).normalize();
            if (rotation instanceof ParticleRotation.FaceCamera) {
                Vec3 viewVec = new Vec3(renderInfo.getLookVector());
                offsetDir = moveDir.cross(viewVec).normalize();
            }
            else {
                offsetDir = moveDir.cross(new Vec3(0, 1, 0)).normalize();
            }
            offsetDir = offsetDir.scale(scale);
            aVector3d2[2] = offsetDir;
            aVector3d2[3] = offsetDir.scale(-1);

            Vector4f[] vertices2 = new Vector4f[] {
                    new Vector4f((float)aVector3d2[0].x, (float)aVector3d2[0].y,  (float)aVector3d2[0].z, 1f),
                    new Vector4f((float)aVector3d2[1].x, (float)aVector3d2[1].y,  (float)aVector3d2[1].z, 1f),
                    new Vector4f((float)aVector3d2[2].x,  (float)aVector3d2[2].y,  (float)aVector3d2[2].z, 1f),
                    new Vector4f((float)aVector3d2[3].x,  (float)aVector3d2[3].y,  (float)aVector3d2[3].z, 1f)
            };
            Matrix4f boxTranslate = (new Matrix4f()).translate((float)p1.x, (float)p1.y, (float)p1.z);
            vertices2[0].mul(boxTranslate);
            vertices2[1].mul(boxTranslate);
            boxTranslate = (new Matrix4f()).translate((float)p2.x, (float)p2.y, (float)p2.z);
            vertices2[2].mul(boxTranslate);
            vertices2[3].mul(boxTranslate);

            float vMin = this.getV0();
            float vMax = this.getV1();

            // [修复] 计算 U 轴的中心点
            float u_center = (this.getU0() + this.getU1()) / 2.0f;

            // [修复] 强制 p1 和 p2 都使用纹理的中心 U 坐标
            float u_p1 = u_center + texPanOffset;
            float u_p2 = u_center + texPanOffset;

            // 顶点顺序 0 -> 1 -> 2 -> 3
            buffer.vertex(vertices2[0].x(), vertices2[0].y(), vertices2[0].z()).uv(u_p1, vMax).color(prevR, prevG, prevB, prevA).uv2(j).endVertex();
            buffer.vertex(vertices2[1].x(), vertices2[1].y(), vertices2[1].z()).uv(u_p1, vMin).color(prevR, prevG, prevB, prevA).uv2(j).endVertex();
            buffer.vertex(vertices2[2].x(), vertices2[2].y(), vertices2[2].z()).uv(u_p2, vMin).color(r, g, b, a).uv2(j).endVertex();
            buffer.vertex(vertices2[3].x(), vertices2[3].y(), vertices2[3].z()).uv(u_p2, vMax).color(r, g, b, a).uv2(j).endVertex();

            prevR = r;
            prevG = g;
            prevB = b;
            prevA = a;
        }

        for (ParticleComponent component : components) {
            component.postRender(this, buffer, renderInfo, partialTicks, j);
        }
    }

    /**
     * 拖尾粒子需要一个自定义的、能包裹所有历史位置的包围盒。
     */
    @Override
    public AABB getBoundingBox() {
        if (positions == null || positions.length <= 0 || positions[0] == null) return super.getBoundingBox();

        AABB box = new AABB(positions[0], positions[0]);
        for (Vec3 pos : positions) {
            if (pos == null) continue;
            box = box.minmax(new AABB(pos, pos));
        }
        return box.inflate(0.1); // 膨胀一点
    }

    // 辅助方法，供纹理平移 (PanTexture) 组件使用
    public float getMinUPublic() { return getU0(); }
    public float getMaxUPublic() { return getU1(); }

    /**
     * 拖尾粒子专用的工厂。
     * 读取 RibbonParticleData 来创建 ParticleRibbon 实例。
     */
    @OnlyIn(Dist.CLIENT)
    public static final class Factory implements ParticleProvider<RibbonParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(RibbonParticleData typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleRibbon particle = new ParticleRibbon(worldIn, x, y, z,
                    xSpeed, ySpeed, zSpeed,
                    typeIn.getRotation(), typeIn.getScale(),
                    typeIn.getRed(), typeIn.getGreen(), typeIn.getBlue(), typeIn.getAlpha(),
                    typeIn.getAirDrag(), typeIn.getDuration(),
                    typeIn.isEmissive(), typeIn.getLength(), typeIn.getComponents());

            particle.pickSprite(spriteSet);
            return particle;
        }
    }
}