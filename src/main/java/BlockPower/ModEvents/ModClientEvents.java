package BlockPower.ModEvents;

import BlockPower.Main.Main;
import BlockPower.ModItems.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerItemProperties();
        });
    }

    // ModClientEvents.java

    private static void registerItemProperties() {
        // PIXEL_CORE相关
        // 注册skill_state属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "skill_state"),
                (stack, level, entity, seed) -> {
                    return stack.getOrCreateTag().getInt("skill_state");
                });

        // 注册tool_type属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "tool_type"),
                (stack, level, entity, seed) -> {
                    if (stack.getOrCreateTag().getInt("skill_state") == 3) {
                        return stack.getOrCreateTag().getInt("tool_type");
                    }
                    return 0f;
                });

        // 注册 "pixel_core_level" 属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "pixel_core_level"),
                (stack, level, entity, seed) -> {
                    if (stack.getOrCreateTag().getInt("skill_state") == 3) {
                        return stack.getOrCreateTag().getInt("pixel_core_level");
                    }
                    return 0f;
                });
    }
}