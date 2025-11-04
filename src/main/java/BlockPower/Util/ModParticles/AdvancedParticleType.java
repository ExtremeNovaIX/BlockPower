package BlockPower.Util.ModParticles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;


/**
 * 粒子类型的基础类，用于简化注册。
 * 它自动链接到 ParticleData 的序列化器和编解码器。
 * 参考自 Mowzie's Mobs 源码。
 */
public class AdvancedParticleType extends ParticleType<ParticleData> {

    /**
     * 构造函数。
     * @param overrideLimiter 是否应覆盖粒子距离限制
     */
    public AdvancedParticleType(boolean overrideLimiter) {
        super(overrideLimiter, ParticleData.DESERIALIZER);
    }

    /**
     * 返回用于数据包 (Data Pack) 加载的编解码器 (Codec)。
     */
    @Override
    public Codec<ParticleData> codec() {
        return ParticleData.CODEC(this);
    }
}