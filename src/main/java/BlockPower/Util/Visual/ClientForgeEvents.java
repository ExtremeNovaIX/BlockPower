package BlockPower.Util.Visual;

import BlockPower.Main.Main;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientEffectManager.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        //TODO:使用多次渲染大幅度增强亮度

        // 在粒子之后渲染
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        List<VisualEffect> effects = ClientEffectManager.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (VisualEffect effect : effects) {
            // 使用我们最终的、正确的RenderType
            RenderType renderType = ModRenderType.getAdditive(effect.getTexture());
            VertexConsumer buffer = bufferSource.getBuffer(renderType);

            poseStack.pushPose();

            // 平移到特效相对于相机的位置
            poseStack.translate(
                    effect.position.x() - cameraPos.x(),
                    effect.position.y() - cameraPos.y(),
                    effect.position.z() - cameraPos.z()
            );

            // 广告牌效果
            poseStack.mulPose(camera.rotation());

            poseStack.scale(10,10,10);
            poseStack.mulPose(Axis.ZP.rotationDegrees(30));

            // 使用全亮度以获得发光效果
            int fullBright = LightTexture.pack(15, 15);

            // 使用我们已验证的辅助方法绘制四边形
            for (int i = 0; i < 16; i++) {
                RenderUtils.drawTexturedQuad(buffer, poseStack.last().pose(), 1.0f, 0.5f, 0.2f, effect.alpha, fullBright);
            }
            poseStack.popPose();
        }

        // 好的实践是为这个特定的缓冲源实例调用endBatch
        bufferSource.endBatch(ModRenderType.getAdditive(VisualEffect.CROSS_STAR_TEXTURE));
    }
}