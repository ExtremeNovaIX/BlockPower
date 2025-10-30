package BlockPower.ModEvents;

import BlockPower.Main.Main;
import BlockPower.ModEntities.ModEntities;
import BlockPower.ModItems.ModItems;
import BlockPower.ModParticles.AdvancedParticle;
import BlockPower.ModParticles.GlowingSparkParticle;
import BlockPower.ModParticles.ModParticles;
import BlockPower.ModParticles.ParticleRibbon;
import BlockPower.ModRenderers.*;
import BlockPower.Skills.ComboSkills.ComboManager.Client.ComboHudRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    public static final ResourceLocation COMBO_HUD_OVERLAY = new ResourceLocation(Main.MOD_ID, "combo_hud");

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        // 在快捷栏上方绘制
        event.registerAbove(
                new ResourceLocation("minecraft", "hotbar"),
                COMBO_HUD_OVERLAY.getPath(),
                new ComboHudRenderer()
        );
    }

    @SubscribeEvent
    public static void onRenderRegister(final EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntities.FAKE_RAIL_ENTITY.get(), FakeRailRenderer::new);
        EntityRenderers.register(ModEntities.RUSH_MINECART.get(), RushMinecartRenderer::new);
        EntityRenderers.register(ModEntities.DROP_ANVIL.get(), DropAnvilRenderer::new);
        EntityRenderers.register(ModEntities.MAGMA_ENTITY.get(), MagmaEntityRenderer::new);
        EntityRenderers.register(ModEntities.FIRECRACKER_ENTITY.get(), FirecrackerEntityRenderer::new);

        event.registerEntityRenderer(ModEntities.FAKE_ITEM.get(), FakeItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.GLOWING_SPARK.get(), GlowingSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.ADVANCED_PARTICLE.get(), AdvancedParticle.Factory::new);
        event.registerSpriteSet(ModParticles.RIBBON_PARTICLE.get(), ParticleRibbon.Factory::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerItemProperties();
        });
    }

    private static void registerItemProperties() {
        // PIXEL_CORE相关
        // 注册skill_state属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "skill_state"),
                (stack, level, entity, seed) -> {
                    return stack.getOrCreateTag().getInt("skill_state");
                }
        );

        // 注册tool_type属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "tool_type"),
                (stack, level, entity, seed) -> {
                    return stack.getOrCreateTag().getInt("tool_type");
                }
        );

        // 注册 "pixel_core_level" 属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "pixel_core_level"),
                (stack, level, entity, seed) -> {
                    return stack.getOrCreateTag().getInt("pixel_core_level");
                }
        );
    }
}
