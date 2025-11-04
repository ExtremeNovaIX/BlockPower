package BlockPower.Util.ModParticles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
public class RibbonParticleType extends ParticleType<RibbonParticleData> {

    public RibbonParticleType(boolean overrideLimiter) {
        super(overrideLimiter, RibbonParticleData.DESERIALIZER);
    }

    @Override
    public Codec<RibbonParticleData> codec() {
        return RibbonParticleData.createCodec(this);
    }
}