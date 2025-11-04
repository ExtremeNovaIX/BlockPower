package BlockPower.Client.ModRenderers;

import BlockPower.ModEntities.FirecrackerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class FirecrackerEntityRenderer extends EntityRenderer<FirecrackerEntity> {

    private final ItemRenderer itemRenderer;
    private final ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);

    public FirecrackerEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FirecrackerEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 使烟花朝向其飞行方向
        // 计算水平旋转 (Y轴)
        float yaw = (float) (Mth.atan2(entity.getDeltaMovement().z, entity.getDeltaMovement().x) * (double) (180F / (float) Math.PI)) - 90.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        // 计算垂直旋转 (X轴)
        float pitch = (float) (Mth.atan2(entity.getDeltaMovement().horizontalDistance(), entity.getDeltaMovement().y) * (double) (180F / (float) Math.PI)) - 90.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));


        // 渲染烟花物品模型
        this.itemRenderer.renderStatic(
                fireworkStack,
                ItemDisplayContext.NONE,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull FirecrackerEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}