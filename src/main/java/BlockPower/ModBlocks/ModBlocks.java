package BlockPower.ModBlocks;

import BlockPower.Main.Main;
import BlockPower.ModBlocks.DestroyingBlocks.DestroyingBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MOD_ID);

    public static final RegistryObject<Block> DECAYING_STONE = BLOCKS.register("decaying_stone",
            () -> new DestroyingBlock(BlockBehaviour.Properties.copy(Blocks.STONE),100));

    public static final RegistryObject<Block> DECAYING_DIRT = BLOCKS.register("decaying_dirt",
            () -> new DestroyingBlock(BlockBehaviour.Properties.copy(Blocks.DIRT),50));

}
