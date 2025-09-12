package BlockPower.ModBlocks;

import BlockPower.Main.Main;
import BlockPower.ModBlocks.DestroyingBlocks.DestroyingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityBlocks {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MOD_ID);

    public static final RegistryObject<BlockEntityType<DestroyingBlockEntity>> DESTROYING_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("destroying_block_entity", () ->
                    BlockEntityType.Builder.of(
                            DestroyingBlockEntity::new,
                            ModBlocks.DECAYING_STONE.get(),
                            ModBlocks.DECAYING_DIRT.get()
                    ).build(null));
}
