package BlockPower.Util.ModParticles;

import BlockPower.Main.Main;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Main.MOD_ID);

    public static final RegistryObject<ParticleType<GlowingSparkParticleOptions>> GLOWING_SPARK =
            PARTICLE_TYPES.register("glowing_spark",
                    () -> new ParticleType<>(true, GlowingSparkParticleOptions.DESERIALIZER) {
                        @Override
                        public @NotNull Codec<GlowingSparkParticleOptions> codec() {
                            // 返回自定义CODEC
                            return GlowingSparkParticleOptions.CODEC;
                        }
                    });
    public static final RegistryObject<ParticleType<ParticleData>> ADVANCED_PARTICLE =
            PARTICLE_TYPES.register("advanced_particle",
                    () -> new AdvancedParticleType(true)
            );
    public static final RegistryObject<ParticleType<RibbonParticleData>> RIBBON_PARTICLE =
            PARTICLE_TYPES.register("ribbon_particle",
                    () -> new RibbonParticleType(true)
            );

}