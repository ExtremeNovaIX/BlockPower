package BlockPower.Entities.DropAnvil;

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

    public DropAnvilRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DropAnvilEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 获取你想要渲染的方块状态，这里是普通铁砧
        BlockState anvilState = Blocks.ANVIL.defaultBlockState();

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        poseStack.pushPose();

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
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}