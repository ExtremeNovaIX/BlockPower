package BlockPower.Util.Visual;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModRenderType extends RenderType {
    // 我们只需要这一个构造函数
    private ModRenderType(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2) {
        super(s, v, m, i, b, b2, r, r2);
        throw new IllegalStateException("This class is not meant to be instantiated!");
    }

    public static RenderType getAdditive(ResourceLocation texture) {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false)) // 无模糊，无mip-mapping
                .setTransparencyState(ADDITIVE_TRANSPARENCY) // 加亮混合模式
                .setCullState(NO_CULL) // 不剔除背面
                .setLightmapState(LIGHTMAP) // 使用世界光照
                .setWriteMaskState(COLOR_WRITE) // 不写入深度缓冲区
                .createCompositeState(true);

        return create("additive_effect",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, // 我们RenderUtils所提供的顶点格式
                VertexFormat.Mode.QUADS,
                256,
                true,
                false, // 加亮混合不需要排序
                state);
    }
}