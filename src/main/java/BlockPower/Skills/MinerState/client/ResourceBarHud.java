package BlockPower.Skills.MinerState.client;

import BlockPower.Main.Main;
import BlockPower.Skills.MinerState.server.MinerStateEvent;
import BlockPower.Skills.MinerState.server.PlayerResourceData;
import BlockPower.Skills.MinerState.server.AllResourceType;
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

            if (player == null || mc.options.hideGui || !MinerStateEvent.minerStateMap.getOrDefault(player, false) || mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                return;
            }

            Map<AllResourceType, Double> resourceCounts = ClientResourceData.getResources();
            if (resourceCounts.isEmpty() || resourceCounts.values().stream().allMatch(v -> v <= 0)) {
                return; // 如果没有任何资源，则不渲染
            }

            //绘制资源条逻辑
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            int barTotalWidth = 60;
            int barHeight = 8;
            int barX = (screenWidth - barTotalWidth) / 2 - 58;
            int barY = screenHeight - 51;

            GuiGraphics guiGraphics = event.getGuiGraphics();

            guiGraphics.fill(barX - 2, barY - 2, barX + barTotalWidth + 2, barY + barHeight + 2, 0xFF000000);

            //资源条填充逻辑
            fillHudResourceBar(barX, resourceCounts, barTotalWidth, barY, barHeight);
            //绘制特殊资源图标
            drawPreciousResourceIcons(guiGraphics, resourceCounts, barX + barTotalWidth + 4, barY - 2);
        }
    }

    /**
     * 在指定位置绘制特殊资源（金、钻石、下界合金）的图标和数量角标。
     *
     * @param guiGraphics  渲染上下文
     * @param resourceData 包含所有资源的Map
     * @param startX       第一个图标的起始X坐标
     * @param startY       第一个图标的起始Y坐标
     */
    private static void drawPreciousResourceIcons(GuiGraphics guiGraphics, Map<AllResourceType, Double> resourceData, int startX, int startY) {
        Minecraft mc = Minecraft.getInstance();

        // 调整后的尺寸参数
        int iconSize = 12; // 缩小图标尺寸 (原16)
        int spacing = 2;
        float scale = 0.7f; // 缩放比例

        // 保存当前变换状态
        guiGraphics.pose().pushPose();
        // 应用缩放变换
        guiGraphics.pose().scale(scale, scale, 1.0f);
        // 调整坐标以补偿缩放
        int scaledX = (int) (startX / scale);
        int scaledY = (int) (startY / scale);

        int currentX = scaledX;

        for (AllResourceType type : AllResourceType.getPreciousResourceType()) {
            double amount = resourceData.getOrDefault(type, 0.0);
            if (amount <= 0) continue;

            int count = (int) Math.floor(amount);
            if (count <= 0) continue;

            ItemStack stackToRender = new ItemStack(type.getCorrespondingItem());
            // 渲染时使用缩放后的坐标
            guiGraphics.renderItem(stackToRender, currentX, scaledY);
            if (count > 1) {
                guiGraphics.renderItemDecorations(mc.font, stackToRender, currentX, scaledY, String.valueOf(count));
            }

            currentX += (int) ((iconSize + spacing) / scale);
        }

        // 恢复变换状态
        guiGraphics.pose().popPose();
    }

    private static void fillHudResourceBar(int barX, Map<AllResourceType, Double> resourceCounts, int barTotalWidth, int barY, int barHeight) {
        int currentX = barX;
        double barCapacity = PlayerResourceData.getCompressionThreshold();// 获取固定的压缩阈值作为计算百分比的分母
        //获取每一种普通资源的数量
        for (AllResourceType type : AllResourceType.getNormalResourceType()) {

            double visualAmount = resourceCounts.getOrDefault(type, 0.0);
            if (visualAmount <= 0) continue;// 如果资源数量小于等于0，则跳过

            // 使用固定的barCapacity作为分母
            int segmentWidth = (int) Math.round((visualAmount / barCapacity) * barTotalWidth);
            if (segmentWidth <= 0) continue;// 如果计算出的宽度小于等于0，则跳过

            TextureAtlasSprite sprite = getSpriteForResource(type);
            if (sprite != null) {
                drawTiledTexture(currentX, barY, segmentWidth, barHeight, sprite);
            }

            currentX += segmentWidth;
        }
    }

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
            case GOLD -> null;
            case DIAMOND -> null;
            case NETHERITE -> null;
        };
    }

    /**
     * 在指定区域内平铺给定的材质。
     */
    private static void drawTiledTexture(int x, int y, int width, int height, TextureAtlasSprite sprite) {
        // 绑定材质图集
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // 保证颜色正常，防止被其他UI元素染色
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