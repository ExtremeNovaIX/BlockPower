package BlockPower.ModParticles;

import BlockPower.Main.Main;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Main.MOD_ID);

    // 注册新的粒子类型
    public static final RegistryObject<ParticleType<GlowingSparkParticleOptions>> GLOWING_SPARK =
            PARTICLE_TYPES.register("glowing_spark",
                    () -> new ParticleType<>(true, GlowingSparkParticleOptions.DESERIALIZER) {
                        @Override
                        public @NotNull Codec<GlowingSparkParticleOptions> codec() {
                            // 返回我们自定义的 CODEC
                            return GlowingSparkParticleOptions.CODEC;
                        }
                    });

}