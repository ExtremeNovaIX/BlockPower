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

    private static void registerItemProperties() {
        //PIXEL_CORE相关
        //注册skill_state属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "skill_state"),
                (stack, level, entity, seed) -> {
                    // 读取NBT中的skill_id
                    return stack.getOrCreateTag().getInt("skill_id");
                });

        //注册tool_type属性
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "tool_type"),
                (stack, level, entity, seed) -> {
                    // 只有在skill_id == 3时才检查这个属性
                    if (stack.getOrCreateTag().getInt("skill_id") == 3) {
                        // (例如: 1=斧, 2=镐, 3=剑)
                        return stack.getOrCreateTag().getInt("tool_type");
                    }
                    return 0f; // 其他模式时返回0
                });

        // 3. 注册 "pixel_core_level" 属性 (新)
        ItemProperties.register(ModItems.PIXEL_CORE.get(),
                new ResourceLocation(Main.MOD_ID, "pixel_core_level"),
                (stack, level, entity, seed) -> {
                    // 只有在skill_id == 3时才检查这个属性
                    if (stack.getOrCreateTag().getInt("skill_id") == 3) {
                        // 1=木, 2=石, 3=铁, 4=金, 5=钻, 6=下界合金
                        return stack.getOrCreateTag().getInt("pixel_core_level");
                    }
                    return 0f; // 其他模式时返回0
                });
    }
}