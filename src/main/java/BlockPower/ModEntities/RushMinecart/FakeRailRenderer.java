// 文件名: FakeRailRenderer.java
package BlockPower.ModEntities.RushMinecart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FakeRailRenderer extends EntityRenderer<FakeRailEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public FakeRailRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(FakeRailEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState railState = Blocks.POWERED_RAIL.defaultBlockState().setValue(BlockStateProperties.POWERED, true);

        //从实体获取同步过来的Y轴旋转角度
        float yRot = entity.getYRot();

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot + 90));
        poseStack.translate(-0.5D, 0, -0.5D);

        this.blockRenderer.renderSingleBlock(railState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FakeRailEntity entity) {
        return null;
    }
}