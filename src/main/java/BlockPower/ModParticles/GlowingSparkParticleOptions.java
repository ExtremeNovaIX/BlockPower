package BlockPower.ModParticles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

import java.util.Locale;

/**
 * 自定义的 ParticleOptions, 用于携带颜色数据
 */
public class GlowingSparkParticleOptions implements ParticleOptions {
    // 序列化和反序列化
    public static final Codec<GlowingSparkParticleOptions> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(GlowingSparkParticleOptions::getColor)
            ).apply(instance, GlowingSparkParticleOptions::new)
    );

    // 定义网络传输
    public static final ParticleOptions.Deserializer<GlowingSparkParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        // 从 /particle 命令读取
        public GlowingSparkParticleOptions fromCommand(ParticleType<GlowingSparkParticleOptions> pParticleType, StringReader pReader) throws CommandSyntaxException {
            pReader.expect(' ');
            float r = (float)pReader.readDouble();
            pReader.expect(' ');
            float g = (float)pReader.readDouble();
            pReader.expect(' ');
            float b = (float)pReader.readDouble();
            return new GlowingSparkParticleOptions(new Vector3f(r, g, b));
        }

        // 从网络包读取
        public GlowingSparkParticleOptions fromNetwork(ParticleType<GlowingSparkParticleOptions> pParticleType, FriendlyByteBuf pBuffer) {
            return new GlowingSparkParticleOptions(pBuffer.readVector3f());
        }
    };

    private final Vector3f color;

    public GlowingSparkParticleOptions(Vector3f color) {
        this.color = color;
    }

    public Vector3f getColor() {
        return this.color;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.GLOWING_SPARK.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf pBuffer) {
        pBuffer.writeVector3f(this.color);
    }

    // 转换为/particle 命令字符串
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f",
                ModParticles.GLOWING_SPARK.getId(), this.color.x(), this.color.y(), this.color.z());
    }
}
