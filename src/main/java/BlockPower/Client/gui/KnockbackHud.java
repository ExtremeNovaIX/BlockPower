package BlockPower.Client.gui;

import BlockPower.Capability.IKnockbackData;
import BlockPower.Capability.ModCapabilities;
import BlockPower.Skills.MinerState.client.ClientMinerState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

public class KnockbackHud {

    private static final RandomSource random = RandomSource.create();
    private static final float HUD_SHAKE_AMPLITUDE = 2.5f;
    private static final float ENTITY_SHAKE_AMPLITUDE = 4.0f;

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || !ClientMinerState.isMinerMode()) {
            return;
        }

        player.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
            double knockbackValue = cap.getKnockbackValue();
            // 获取整个动画状态对象
            KnockbackAnimationManager.AnimationState animState = KnockbackAnimationManager.getAndUpdatePlayerState((float) knockbackValue);
            float shakeIntensity = animState.getShakeIntensity();
            float colorIntensity = animState.getColorIntensity();

            int baseColor = getKnockbackColor(knockbackValue);
            // 如果在颜色动画中，则在基础色和红色之间插值
            int finalColor = (colorIntensity > 0) ? lerpColor(colorIntensity, baseColor, 0xFF0000) : baseColor;

            String text = String.format("%.1f%%", knockbackValue);
            Font font = minecraft.font;

            int x = 10;
            int y = 10;
            float scale = 1.5f;

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);

            float currentX = x / scale;
            for (char character : text.toCharArray()) {
                Component charComponent = Component.literal(String.valueOf(character))
                        .withStyle(Style.EMPTY.withBold(true));

                float offsetX = 0;
                float offsetY = 0;
                if (shakeIntensity > 0) {
                    offsetX = (random.nextFloat() - 0.5f) * 2 * HUD_SHAKE_AMPLITUDE * shakeIntensity;
                    offsetY = (random.nextFloat() - 0.5f) * 2 * HUD_SHAKE_AMPLITUDE * shakeIntensity;
                }

                guiGraphics.drawString(font, charComponent, (int) (currentX + offsetX), (int) (y / scale + offsetY), finalColor, true);
                currentX += font.width(charComponent);
            }

            poseStack.popPose();
        });
    }

    public static int getKnockbackColor(double value) {
        final int colorWhite = 0xFFFFFF;
        final int colorYellow = 0xFFFF55;
        final int colorOrange = 0xFFAA00;
        final int colorRed = 0xFF0000;

        if (value <= 50) {
            return lerpColor((float) (value / 50.0), colorWhite, colorYellow);
        } else if (value <= 100) {
            return lerpColor((float) ((value - 50) / 50.0), colorYellow, colorOrange);
        } else if (value <= 200) {
            return lerpColor((float) ((value - 100) / 100.0), colorOrange, colorRed);
        } else {
            return colorRed;
        }
    }

    public static int lerpColor(float t, int color1, int color2) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + t * (r2 - r1));
        int g = (int) (g1 + t * (g2 - g1));
        int b = (int) (b1 + t * (b2 - b1));

        return (r << 16) | (g << 8) | b;
    }

    /**
     * 渲染实体的击退值 HUD
     */
    public static void renderEntityKBHud(RenderLivingEvent.Post<?, ?> event, Player player, LivingEntity entity, Minecraft minecraft) {
        // 只有当玩家存在、实体不是玩家自己、且玩家处于MinerMode时才渲染
        if (player == null || entity == player || !ClientMinerState.isMinerMode()) {
            return;
        }

        // 检查实体是否在玩家附近（例如16格内）
        if (player.distanceToSqr(entity) > 16 * 16) {
            return;
        }

        // 通过Mixin接口安全地获取击退值
        if (entity instanceof IKnockbackData data) {
            float knockbackValue = entity.getEntityData().get(data.getKnockbackDataAccessor());

            if (knockbackValue > 0) {
                // 从管理器获取该实体的完整动画状态对象
                KnockbackAnimationManager.AnimationState animState = KnockbackAnimationManager.getAndUpdateEntityState(entity, knockbackValue);
                // 从状态对象中分别获取抖动和颜色的强度
                float shakeIntensity = animState.getShakeIntensity();
                float colorIntensity = animState.getColorIntensity();

                int baseColor = KnockbackHud.getKnockbackColor(knockbackValue);
                // 如果在颜色动画中，则在基础色和红色之间插值
                int finalColor = (colorIntensity > 0) ? KnockbackHud.lerpColor(colorIntensity, baseColor, 0xFF0000) : baseColor;

                String text = String.format("%.1f%%", knockbackValue);
                Font font = minecraft.font;
                PoseStack poseStack = event.getPoseStack();

                poseStack.pushPose();
                poseStack.translate(0, entity.getBbHeight() + 0.75f, 0);
                poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
                float scale = 0.035f;
                poseStack.scale(-scale, -scale, scale);

                float totalWidth = font.width(text);
                float currentX = -totalWidth / 2.0f;

                for (char character : text.toCharArray()) {
                    Component charComponent = Component.literal(String.valueOf(character))
                            .withStyle(Style.EMPTY.withBold(true));

                    float offsetX = 0;
                    float offsetY = 0;
                    if (shakeIntensity > 0) {
                        offsetX = (random.nextFloat() - 0.5f) * 2 * ENTITY_SHAKE_AMPLITUDE * shakeIntensity;
                        offsetY = (random.nextFloat() - 0.5f) * 2 * ENTITY_SHAKE_AMPLITUDE * shakeIntensity;
                    }

                    poseStack.pushPose();
                    poseStack.translate(currentX + offsetX, offsetY, 0);

                    Matrix4f matrix4f = poseStack.last().pose();
                    MultiBufferSource multiBufferSource = event.getMultiBufferSource();
                    int packedLight = LightTexture.pack(15, 15);

                    font.drawInBatch(charComponent, 0, 0, finalColor, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

                    poseStack.popPose();
                    currentX += font.width(charComponent);
                }

                poseStack.popPose();
            }
        }
    }
}
