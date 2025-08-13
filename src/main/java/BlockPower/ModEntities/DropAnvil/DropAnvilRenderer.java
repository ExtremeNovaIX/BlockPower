package BlockPower.ModEntities.DropAnvil;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DropAnvilRenderer extends EntityRenderer<DropAnvilEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public DropAnvilRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(DropAnvilEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState anvilState = Blocks.ANVIL.defaultBlockState();

        poseStack.pushPose();
        poseStack.scale(3F, 3F, 3F);
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        blockRenderer.renderSingleBlock(
                anvilState,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DropAnvilEntity entity) {
        return Blocks.ANVIL.getLootTable();
    }
}