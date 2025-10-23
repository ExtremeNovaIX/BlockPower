package BlockPower.ModEntities.MagmaBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;

public class MagmaEntityRenderer extends EntityRenderer<MagmaEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public MagmaEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    /**
     * 强制返回最大方块光照等级 15，实现实体自发光。
     */
    @Override
    protected int getBlockLightLevel(@NotNull MagmaEntity entity, @NotNull BlockPos pos) {
        return 15;
    }

    /**
     * 渲染岩浆块的模型，并应用最大光照。
     */
    @Override
    public void render(@NotNull MagmaEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {

        BlockState magmaState = Blocks.MAGMA_BLOCK.defaultBlockState();
        Level level = entity.level();
        BlockPos entityPos = entity.blockPosition();

        poseStack.pushPose();

        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // 获取方块光照
        int blockLight = getBlockLightLevel(entity, entityPos);

        // 获取周围环境的天空光照值
        int skyLight = level.getBrightness(LightLayer.SKY, entityPos);

        // 打包光照值
        int finalPackedLight = (skyLight << 20) | (blockLight << 4);

        // 渲染方块模型
        this.blockRenderer.renderSingleBlock(
                magmaState,
                poseStack,
                buffer,
                finalPackedLight, // 传入最终光照值
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 返回实体纹理位置。
     */
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MagmaEntity entity) {
        return new ResourceLocation("minecraft:textures/block/magma.png");
    }
}