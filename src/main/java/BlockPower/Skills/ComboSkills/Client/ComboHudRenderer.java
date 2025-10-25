package BlockPower.Skills.ComboSkills.Client;

import BlockPower.Main.Main;
import BlockPower.Skills.ComboSkills.Client.ClientComboData;
import BlockPower.Skills.ComboSkills.ComboSkill;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 负责在客户端 HUD 上渲染连携技图标及其所有相关动画。
 * 包括：图标、计时器、登场动画（缩放、扩散环）和触发动画（缩放、扩散环）。
 */
public class ComboHudRenderer implements IGuiOverlay {
    private static final ResourceLocation COMBO_TIMER_MASK = new ResourceLocation(Main.MOD_ID, "textures/gui/combo_skills/combo_timer_mask.png");

    // 布局配置
    private static final int LOGICAL_ICON_SIZE = 16;
    private static final int TEXTURE_ICON_SIZE = 64;
    private static final int ICON_PADDING = 4;

    // 连携技动画配置
    private static final float FADE_IN_TICKS = 3.0f;
    private static final float POP_IN_DURATION_TICKS = 3.0f;
    private static final float POP_IN_START_SCALE = 1.8f;

    // 连携技动画: "登场" 扩散环
    private static final float RING_DIFFUSE_DURATION_TICKS = 4.0f;
    private static final float RING_START_RADIUS_MULT = 0.9f;
    private static final float RING_END_RADIUS_MULT = 1.3f;
    private static final float RING_THICKNESS = 1.5f;
    private static final float RING_START_ALPHA = 0.9f;

    // 连携技动画: "触发" 扩散环
    private static final float TRIGGER_RING_START_RADIUS_MULT = 1.0f;
    private static final float TRIGGER_RING_END_RADIUS_MULT = 2.5f;
    private static final float TRIGGER_RING_THICKNESS = 1.0f;
    private static final float TRIGGER_RING_START_ALPHA = 0.8f;


    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {

        List<ClientComboData.ActiveSkillData> activeSkills = ClientComboData.getActiveComboSkills();
        if (activeSkills.isEmpty()) {
            return;
        }

        // 设置通用渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 重新计算绘制起始位置 (基于准星)
        int skillsToDraw = Math.min(activeSkills.size(), 3);
        int crosshairX = screenWidth / 2;
        int crosshairY = screenHeight / 2;
        int startX = crosshairX + 20;
        int startY = crosshairY - 30;

        // 检查连携技是否可用(暂未实现)
        boolean isAvailable = ClientComboData.isComboSkillAvailable();

        for (int i = 0; i < skillsToDraw; i++) {
            ClientComboData.ActiveSkillData skillData = activeSkills.get(i);
            ComboSkill skill = skillData.getType().getSkill();
            if (skill == null) continue;

            // 根据连携技在本地缓存中的索引计算当前图标绘制位置
            int currentX = startX + (i * (LOGICAL_ICON_SIZE + ICON_PADDING));

            drawSkillIcon(guiGraphics, skillData, skill, currentX, startY, partialTick, isAvailable);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    /**
     * 绘制技能图标及其所有效果
     */
    private void drawSkillIcon(GuiGraphics guiGraphics, ClientComboData.ActiveSkillData skillData, ComboSkill skill,
                               int x, int y, float partialTick, boolean isAvailable) {

        // 获取基础数据
        int totalDurationTicks = skill.getComboWindowTick();
        float percent = skillData.getRemainingPercent();
        int remainingTicks = (int) (percent * totalDurationTicks);
        int ticksAlive = totalDurationTicks - remainingTicks;
        float totalTicksAlive = ticksAlive + partialTick;

        float fadeInAlpha = Math.min(1.0f, totalTicksAlive / FADE_IN_TICKS);
        // 根据连携技是否可用设置颜色 (可用时正常，不可用时灰色)
        float color = isAvailable ? 1.0f : 0.5f;

        // 计算图标当前缩放比例
        float scale = calculateIconScale(skillData, totalTicksAlive, partialTick);

        // 应用图标变换
        guiGraphics.pose().pushPose();
        float centerX = x + LOGICAL_ICON_SIZE / 2.0f;
        float centerY = y + LOGICAL_ICON_SIZE / 2.0f;
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);

        // 绘制登场或者触发动画的扩散环
        drawAllEffects(guiGraphics, skillData, totalTicksAlive, partialTick, fadeInAlpha);

        // 绘制技能图标和计时器环
        RenderSystem.setShaderColor(color, color, color, fadeInAlpha);
        drawTimerMask(guiGraphics, percent);
        drawIconTexture(guiGraphics, skill);

        guiGraphics.pose().popPose();
    }

    /**
     * 计算图标的当前缩放比例。
     * 优先处理“登场”缩放，完成后再处理“按下”缩放。
     *
     * @param skillData       技能数据
     * @param totalTicksAlive 技能已激活的总 tick (含插值)
     * @param partialTick     渲染插值
     * @return 缩放比例 (e.g., 1.0f)
     */
    private float calculateIconScale(ClientComboData.ActiveSkillData skillData, float totalTicksAlive, float partialTick) {
        float popInProgress = Math.min(1.0f, totalTicksAlive / POP_IN_DURATION_TICKS);
        boolean isTriggered = skillData.isTriggered();

        if (popInProgress < 1.0f) {
            // 登场缩放
            float easeOutProgress = 1.0f - (1.0f - popInProgress) * (1.0f - popInProgress);
            return POP_IN_START_SCALE + (1.0f - POP_IN_START_SCALE) * easeOutProgress;
        } else if (isTriggered) {
            // 触发（按下）缩放
            float animPercent = skillData.getTriggerAnimPercent(partialTick);
            return 1.0f - (animPercent * 0.2f); // 1.0 -> 0.8
        } else {
            return 1.0f;
        }
    }

    /**
     * 绘制所有非纹理的视觉效果（例如扩散环）。
     *
     * @param guiGraphics     GuiGraphics 实例
     * @param skillData       技能数据
     * @param totalTicksAlive 技能已激活的总 tick (含插值)
     * @param partialTick     渲染插值
     * @param fadeInAlpha     图标的整体淡入透明度
     */
    private void drawAllEffects(GuiGraphics guiGraphics, ClientComboData.ActiveSkillData skillData, float totalTicksAlive, float partialTick, float fadeInAlpha) {
        boolean isTriggered = skillData.isTriggered();

        // 连携技完成缩放后，绘制扩散环
        if (totalTicksAlive > POP_IN_DURATION_TICKS) {
            float ringTicksAlive = totalTicksAlive - POP_IN_DURATION_TICKS;
            float ringProgress = Math.min(1.0f, ringTicksAlive / RING_DIFFUSE_DURATION_TICKS);
            if (ringProgress < 1.0f) {
                drawExpandingRing(guiGraphics, ringProgress, fadeInAlpha,
                        RING_START_RADIUS_MULT, RING_END_RADIUS_MULT,
                        RING_THICKNESS, RING_START_ALPHA);
            }
        }

        // 触发（按下）缩放完成后，绘制扩散环
        if (isTriggered) {
            float triggerProgress = skillData.getTriggerAnimPercent(partialTick);
            if (triggerProgress <= 1.0f && triggerProgress >= 0.0f) {
                drawExpandingRing(guiGraphics, triggerProgress, fadeInAlpha,
                        TRIGGER_RING_START_RADIUS_MULT, TRIGGER_RING_END_RADIUS_MULT,
                        TRIGGER_RING_THICKNESS, TRIGGER_RING_START_ALPHA);
            }
        }
    }

    /**
     * 绘制一个从内到外扩散并淡出的圆环。
     * 假设 PoseStack 已经被平移到图标中心。
     *
     * @param guiGraphics GuiGraphics 实例
     * @param progress    动画进度 (0.0 -> 1.0)
     * @param globalAlpha 图标的整体淡入透明度
     * @param startRadius 圆环起始半径（占图标半径的倍数）
     * @param endRadius   圆环结束半径（占图标半径的倍数）
     * @param thickness   圆环的像素粗细
     * @param startAlpha  圆环的初始本地透明度
     */
    private void drawExpandingRing(GuiGraphics guiGraphics, float progress, float globalAlpha,
                                   float startRadius, float endRadius, float thickness, float startAlpha) {

        if (globalAlpha <= 0.0f || progress >= 1.0f) return;

        // 计算半径，扩散速度先快后慢
        float easeOutRadiusProgress = 1.0f - (1.0f - progress) * (1.0f - progress);
        float baseRadius = LOGICAL_ICON_SIZE / 2.0f;
        float currentRadius = baseRadius * (startRadius + (endRadius - startRadius) * easeOutRadiusProgress);

        float innerRadius = currentRadius - (thickness / 2.0f);
        float outerRadius = currentRadius + (thickness / 2.0f);

        // 计算透明度，淡出速度先快后慢
        float easeInAlphaProgress = progress * progress;
        float localAlpha = startAlpha * (1.0f - easeInAlphaProgress);
        float finalAlpha = localAlpha * globalAlpha;

        if (finalAlpha <= 0.0f) return;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f matrix = guiGraphics.pose().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        int segments = 40;
        for (int i = 0; i <= segments; i++) {
            float percent = i / (float) segments;
            float angle = percent * 2.0f * (float) Math.PI;

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            bufferBuilder.vertex(matrix, cos * outerRadius, sin * outerRadius, 0)
                    .color(1.0f, 1.0f, 1.0f, finalAlpha).endVertex();
            bufferBuilder.vertex(matrix, cos * innerRadius, sin * innerRadius, 0)
                    .color(1.0f, 1.0f, 1.0f, finalAlpha).endVertex();
        }
        tesselator.end();
    }

    /**
     * 绘制连携技的计时环。
     * 假设 PoseStack 已经被平移到图标中心。
     *
     * @param guiGraphics GuiGraphics 实例
     * @param percent     剩余时间百分比 (0.0 -> 1.0)
     */
    private void drawTimerMask(GuiGraphics guiGraphics, float percent) {
        if (percent <= 0) return;

        float radius = LOGICAL_ICON_SIZE / 2.0f;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f matrix = guiGraphics.pose().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, COMBO_TIMER_MASK);

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);

        // 中心点
        bufferBuilder.vertex(matrix, 0, 0, 0).uv(0.5f, 0.5f).endVertex();

        // 逆时针绘制圆弧
        int segments = 40;
        int segmentsToDraw = (int) Math.ceil(segments * percent);
        float startAngle = (float) (-Math.PI / 2.0);

        for (int j = segmentsToDraw; j >= 0; j--) {
            float currentFillPercent = j / (float) segments;
            float angle = startAngle + (currentFillPercent * 2.0f * (float) Math.PI);

            float x = radius * (float) Math.cos(angle);
            float y = radius * (float) Math.sin(angle);
            float u = 0.5f + 0.5f * (float) Math.cos(angle);
            float v = 0.5f + 0.5f * (float) Math.sin(angle);

            bufferBuilder.vertex(matrix, x, y, 0).uv(u, v).endVertex();
        }
        tesselator.end();
    }

    /**
     * 绘制缩放的技能图标。
     * 假设 PoseStack 已经被平移到图标中心。
     *
     * @param guiGraphics GuiGraphics 实例
     * @param skill       技能实例
     */
    private void drawIconTexture(GuiGraphics guiGraphics, ComboSkill skill) {
        String iconPath = skill.getTextureLocation();
        if (iconPath != null) {
            ResourceLocation iconTexture = new ResourceLocation(Main.MOD_ID, iconPath);

            int renderX = (int) (-LOGICAL_ICON_SIZE / 2.0f);
            int renderY = (int) (-LOGICAL_ICON_SIZE / 2.0f);

            // 绘制高分辨率纹理到逻辑尺寸
            guiGraphics.blit(
                    iconTexture,
                    renderX, renderY,
                    LOGICAL_ICON_SIZE, LOGICAL_ICON_SIZE,
                    0.0f, 0.0f,
                    TEXTURE_ICON_SIZE, TEXTURE_ICON_SIZE,
                    TEXTURE_ICON_SIZE, TEXTURE_ICON_SIZE
            );
        } else {
            // 备用绘制 (Debug)
            int fillRadius = (int) (LOGICAL_ICON_SIZE / 2.0f) - 2;
            guiGraphics.fill(-fillRadius, -fillRadius, fillRadius, fillRadius, 0xFFFF00FF);
        }
    }
}