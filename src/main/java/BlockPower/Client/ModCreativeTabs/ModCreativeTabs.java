package BlockPower.Client.ModCreativeTabs;

import BlockPower.Main.Main;
import BlockPower.ModItems.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 注册模组的创造模式物品栏。
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MOD_ID);

    public static final RegistryObject<CreativeModeTab> BLOCKPOWER_TAB = CREATIVE_MODE_TABS.register("blockpower_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.blockpower_tab"))
                    .icon(() -> new ItemStack(ModItems.PIXEL_CORE.get()))
                    .displayItems((displayParams, output) -> {
                        output.accept(ModItems.DEBUG_ITEM.get());
                        output.accept(ModItems.DEBUG_ITEM2.get());
                        output.accept(ModItems.PIXEL_CORE.get());
                        output.accept(ModItems.RESOURCE_FILL_DEBUG_ITEM.get());
                    })
                    .build()
    );
}