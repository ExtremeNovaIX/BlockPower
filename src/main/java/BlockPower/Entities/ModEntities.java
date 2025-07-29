package BlockPower.Entities;

import BlockPower.Entities.DropAnvil.DropAnvilEntity;
import BlockPower.Entities.RushMinecart.FakeRailEntity;
import BlockPower.Entities.RushMinecart.RushMinecartEntity;
import BlockPower.Main.Main;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Main.MOD_ID);

    public static final RegistryObject<EntityType<FakeRailEntity>> FAKE_RAIL_ENTITY =
            ENTITY_TYPES.register("fake_rail",
                    () -> EntityType.Builder.<FakeRailEntity>of(FakeRailEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(128)
                            .noSave()
                            .fireImmune()
                            .build("fake_rail"));

    public static final RegistryObject<EntityType<RushMinecartEntity>> RUSH_MINECART =
            ENTITY_TYPES.register("rush_minecart",
                    () -> EntityType.Builder.<RushMinecartEntity>of(RushMinecartEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(128) // 客户端追踪范围
                            .updateInterval(1) // 更新间隔
                            .noSave()
                            .noSummon()
                            .build("rush_minecart"));

    public static final RegistryObject<EntityType<DropAnvilEntity>> DROP_ANVIL =
            ENTITY_TYPES.register("drop_anvil",
                    () -> EntityType.Builder.<DropAnvilEntity>of(DropAnvilEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .noSave()
                            .noSummon()
                            .build("drop_anvil"));
}
