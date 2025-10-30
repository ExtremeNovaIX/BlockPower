package BlockPower.ModRenderers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 包含模组自定义的渲染类型。
 * 部分代码参考自 Mowzie's Mobs 源码。
 */
@OnlyIn(Dist.CLIENT)
public abstract class RenderType extends net.minecraft.client.renderer.RenderType {

    public RenderType(String nameIn, VertexFormat formatIn, VertexFormat.Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    /**
     * 用于渲染半透明粒子且不写入深度缓冲区的渲染类型。
     * 这允许粒子在其他物体之后渲染时不会被错误遮挡。
     */
    public static ParticleRenderType PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(false); // 不写入深度缓冲区
            RenderSystem.disableCull(); // 禁用背面剔除，确保双面可见
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES); // 绑定粒子纹理图集
            RenderSystem.enableBlend(); // 启用混合以支持透明度
            // 设置混合函数为标准的 Alpha 混合
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            // 开始绘制四边形 (QUADS) 模式的粒子顶点
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        @Override
        public String toString() {
            return "BLOCKPOWER_PARTICLE_SHEET_TRANSLUCENT_NO_DEPTH";
        }
    };

    // 用于渲染加法混合粒子且不写入深度缓冲区的渲染类型。(发光)
    // 这允许粒子在其他物体之后渲染时不会被错误遮挡。
    public static ParticleRenderType PARTICLE_SHEET_ADDITIVE_NO_DEPTH = new ParticleRenderType() {
        public void begin(BufferBuilder p_217600_1_, TextureManager p_217600_2_) {
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();

            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

            p_217600_1_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator p_217599_1_) {
            p_217599_1_.end();

            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }

        public String toString() {
            return "PARTICLE_SHEET_ADDITIVE_NO_DEPTH";
        }
    };
}