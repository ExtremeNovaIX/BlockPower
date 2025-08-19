package BlockPower.Skills.MinerState.client;

import BlockPower.Main.Main;
import BlockPower.Skills.MinerState.server.MinerStateEvent;
import BlockPower.Skills.MinerState.server.PlayerResourceData;
import BlockPower.Skills.MinerState.server.ResourceType;
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

            if (player == null || mc.options.hideGui || !MinerStateEvent.minerStateMap.getOrDefault(player, false) || (mc.gameMode.getPlayerMode() != GameType.SURVIVAL && mc.gameMode.getPlayerMode() != GameType.ADVENTURE)) {
                return;
            }

            Map<ResourceType, Double> resourceCounts = ClientResourceData.getResources();
            if (resourceCounts.isEmpty() || resourceCounts.values().stream().allMatch(v -> v <= 0)) {
                return; // 如果没有任何资源，则不渲染
            }

            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();

            int barTotalWidth = 90;
            int barHeight = 10;
            int barX = (screenWidth - barTotalWidth) / 2;
            int barY = screenHeight - 51;

            GuiGraphics guiGraphics = event.getGuiGraphics();

            guiGraphics.fill(barX - 1, barY - 1, barX + barTotalWidth + 1, barY + barHeight + 1, 0xFF000000);

            int currentX = barX;

            // 获取固定的压缩阈值作为计算百分比的分母
            double barCapacity = PlayerResourceData.getCompressionThreshold();

            for (ResourceType type : ResourceType.values()) {
                double visualAmount = resourceCounts.getOrDefault(type, 0.0);
                if (visualAmount <= 0) continue;

                // 使用固定的barCapacity作为分母
                int segmentWidth = (int) Math.round((visualAmount / barCapacity) * barTotalWidth);
                if(segmentWidth <= 0) continue;

                TextureAtlasSprite sprite = getSpriteForResource(type);
                if (sprite != null) {
                    drawTiledTexture(currentX, barY, segmentWidth, barHeight, sprite);
                }

                currentX += segmentWidth;
            }
        }
    }

    // getSpriteForResource 方法保持不变...
    private static TextureAtlasSprite getSpriteForResource(ResourceType type) {
        Minecraft mc = Minecraft.getInstance();
        return switch (type) {
            case DIRT -> mc.getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(Items.DIRT)).getParticleIcon();
            case STONE -> mc.getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(Items.COBBLESTONE)).getParticleIcon();
            case WOOD -> mc.getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(Items.OAK_PLANKS)).getParticleIcon();
            case IRON -> mc.getBlockRenderer().getBlockModel(Blocks.RAW_IRON_BLOCK.defaultBlockState()).getParticleIcon();
            case GOLD -> mc.getBlockRenderer().getBlockModel(Blocks.RAW_GOLD_BLOCK.defaultBlockState()).getParticleIcon();
        };
    }

    /**
     * 【已修正】在指定区域内平铺给定的材质。
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