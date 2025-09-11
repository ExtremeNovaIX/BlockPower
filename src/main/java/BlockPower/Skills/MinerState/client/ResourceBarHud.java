package BlockPower.Skills.MinerState.client;

import BlockPower.Main.Main;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.PlayerResourceData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ResourceBarHud {
    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (mc.gameMode != null && (player == null || mc.options.hideGui || !ClientMinerState.isMinerMode() || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)) {
                return;
            }

            Map<AllResourceType, Double> resourceCounts = ClientResourceData.getResources();
            if (resourceCounts.isEmpty() || resourceCounts.values().stream().allMatch(v -> v <= 0)) {
                return;
            }

            // 绘制资源条逻辑
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            int barTotalWidth = 60;
            int barHeight = 8;
            int barX = (screenWidth - barTotalWidth) / 2 - 58;
            int barY = screenHeight - 51;

            GuiGraphics guiGraphics = event.getGuiGraphics();

            // 绘制外部灰色边框
            guiGraphics.fill(barX - 1, barY - 1, barX + barTotalWidth + 1, barY + barHeight + 1, 0xFF8B8B8B);
            // 绘制黑色内底
            guiGraphics.fill(barX, barY, barX + barTotalWidth, barY + barHeight, 0xFF000000);

            // 资源条填充逻辑
            fillHudResourceBar(resourceCounts, barX, barY, barTotalWidth, barHeight);
            // 绘制特殊资源图标
            drawPreciousResourceIcons(guiGraphics, resourceCounts, barX + barTotalWidth + 4, barY - 2);
        }
    }

    /**
     * 使用累积计算的方式填充资源条，以消除舍入误差。
     */
    private static void fillHudResourceBar(Map<AllResourceType, Double> resourceCounts, int barX, int barY, int barTotalWidth, int barHeight) {
        double barCapacity = PlayerResourceData.getTotalMaxAmount();
        double accumulatedAmount = 0.0; // 用于累积已计算的资源量

        for (AllResourceType type : AllResourceType.getNormalResourceType()) {
            double visualAmount = resourceCounts.getOrDefault(type, 0.0);
            if (visualAmount <= 0) continue;

            // 计算当前区段的起始X坐标
            int startX = barX + (int) Math.round((accumulatedAmount / barCapacity) * barTotalWidth);

            // 累加当前资源量
            accumulatedAmount += visualAmount;

            // 计算当前区段的结束X坐标
            int endX = barX + (int) Math.round((accumulatedAmount / barCapacity) * barTotalWidth);

            // 最终宽度为结束点减去起始点
            int segmentWidth = endX - startX;
            if (segmentWidth <= 0) continue;

            TextureAtlasSprite sprite = getSpriteForResource(type);
            if (sprite != null) {
                // 在计算出的精确位置绘制
                drawTiledTexture(startX, barY, segmentWidth, barHeight, sprite);
            }
        }
    }

    /**
     * 在指定位置绘制特殊资源（金、钻石、下界合金）的图标和数量角标。
     */
    private static void drawPreciousResourceIcons(GuiGraphics guiGraphics, Map<AllResourceType, Double> resourceData, int startX, int startY) {
        Minecraft mc = Minecraft.getInstance();
        int iconSize = 12;
        int spacing = 2;
        float scale = 0.7f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);
        int scaledX = (int) (startX / scale);
        int scaledY = (int) (startY / scale);
        int currentX = scaledX;
        for (AllResourceType type : AllResourceType.getPreciousResourceType()) {
            double amount = resourceData.getOrDefault(type, 0.0);
            if (amount <= 0) continue;
            int count = (int) Math.floor(amount);
            if (count <= 0) continue;
            ItemStack stackToRender = new ItemStack(type.getCorrespondingItem());
            guiGraphics.renderItem(stackToRender, currentX, scaledY);
            if (count > 1) {
                guiGraphics.renderItemDecorations(mc.font, stackToRender, currentX, scaledY, String.valueOf(count));
            }
            currentX += (int) ((iconSize + spacing) / scale);
        }
        guiGraphics.pose().popPose();
    }

    /**
     * 根据资源类型获取对应的渲染材质。
     */
    private static TextureAtlasSprite getSpriteForResource(AllResourceType type) {
        Minecraft mc = Minecraft.getInstance();
        return switch (type) {
            case DIRT ->
                    mc.getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(Items.DIRT)).getParticleIcon(ModelData.EMPTY);
            case STONE ->
                    mc.getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(Items.COBBLESTONE)).getParticleIcon(ModelData.EMPTY);
            case WOOD ->
                    mc.getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(Items.OAK_PLANKS)).getParticleIcon(ModelData.EMPTY);
            case IRON ->
                    mc.getBlockRenderer().getBlockModel(Blocks.RAW_IRON_BLOCK.defaultBlockState()).getParticleIcon(ModelData.EMPTY);
            default -> null; // 对于其他不应在条内渲染的类型返回null
        };
    }

    /**
     * 在指定区域内平铺给定的材质。
     */
    private static void drawTiledTexture(int x, int y, int width, int height, TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int spriteWidth = sprite.contents().width();
        int spriteHeight = sprite.contents().height();
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float uWidth = sprite.getU1() - u0;
        float vHeight = sprite.getV1() - v0;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int drawX = 0; drawX < width; drawX += spriteWidth) {
            for (int drawY = 0; drawY < height; drawY += spriteHeight) {
                int currentWidth = Math.min(spriteWidth, width - drawX);
                int currentHeight = Math.min(spriteHeight, height - drawY);
                float u1 = u0 + (uWidth * ((float) currentWidth / spriteWidth));
                float v1 = v0 + (vHeight * ((float) currentHeight / spriteHeight));
                int x1 = x + drawX;
                int y1 = y + drawY;
                int x2 = x1 + currentWidth;
                int y2 = y1 + currentHeight;
                bufferBuilder.vertex(x1, y2, 0).uv(u0, v1).endVertex();
                bufferBuilder.vertex(x2, y2, 0).uv(u1, v1).endVertex();
                bufferBuilder.vertex(x2, y1, 0).uv(u1, v0).endVertex();
                bufferBuilder.vertex(x1, y1, 0).uv(u0, v0).endVertex();
            }
        }
        tesselator.end();
    }
}