package BlockPower.ModParticles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

/**
 * 粒子选项的数据容器，用于通过网络或命令生成粒子。
 * 存储生成 AdvancedParticle 所需的所有参数。
 * 参考自 Mowzie's Mobs 源码。
 */
public class ParticleData implements ParticleOptions {

    private final ParticleType<? extends ParticleData> type;
    private final float airDrag;
    private final float red, green, blue, alpha;
    private final ParticleRotation rotation;
    private final float scale;
    private final boolean emissive;
    private final float duration;
    private final boolean canCollide;
    private final ParticleComponent[] components;

    public ParticleData(ParticleType<? extends ParticleData> type, ParticleRotation rotation, double scale, double r, double g, double b, double a, double drag, double duration, boolean emissive, boolean canCollide, ParticleComponent[] components) {
        this.type = type;
        this.rotation = rotation;
        this.scale = (float) scale;
        this.red = (float) r;
        this.green = (float) g;
        this.blue = (float) b;
        this.alpha = (float) a;
        this.airDrag = (float) drag;
        this.duration = (float) duration;
        this.emissive = emissive;
        this.canCollide = canCollide;
        this.components = components; // 组件仅在代码中定义，不通过网络传输
    }

    @Override
    public ParticleType<? extends ParticleData> getType() {
        return type;
    }

    /**
     * 将粒子数据写入网络缓冲区 (FriendlyByteBuf) 以进行同步。
     */
    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        // 将旋转对象序列化为字符串和浮点数
        String rotationMode;
        float faceCameraAngle = 0;
        float yaw = 0;
        float pitch = 0;
        float roll = 0;
        if (rotation instanceof ParticleRotation.FaceCamera) {
            rotationMode = "face_camera";
            faceCameraAngle = ((ParticleRotation.FaceCamera) rotation).faceCameraAngle;
        }
        else if (rotation instanceof ParticleRotation.EulerAngles) {
            rotationMode = "euler";
            yaw = ((ParticleRotation.EulerAngles) rotation).yaw;
            pitch = ((ParticleRotation.EulerAngles) rotation).pitch;
            roll = ((ParticleRotation.EulerAngles) rotation).roll;
        }
        else {
            rotationMode = "orient";
            Vec3 vec = ((ParticleRotation.OrientVector)rotation).orientation;
            yaw = (float) vec.x;
            pitch = (float) vec.y;
            roll = (float) vec.z;
        }

        buffer.writeFloat(this.airDrag);
        buffer.writeFloat(this.red);
        buffer.writeFloat(this.green);
        buffer.writeFloat(this.blue);
        buffer.writeFloat(this.alpha);
        buffer.writeUtf(rotationMode);
        buffer.writeFloat(this.scale);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeFloat(roll);
        buffer.writeBoolean(this.emissive);
        buffer.writeFloat(this.duration);
        buffer.writeFloat(faceCameraAngle);
        buffer.writeBoolean(canCollide);
    }

    /**
     * 将粒子数据序列化为命令字符串。
     */
    @SuppressWarnings("deprecation")
    @Override
    public String writeToString() {
        // 将旋转对象序列化为字符串和浮点数
        String rotationMode;
        float faceCameraAngle = 0;
        float yaw = 0;
        float pitch = 0;
        float roll = 0;
        if (rotation instanceof ParticleRotation.FaceCamera) {
            rotationMode = "face_camera";
            faceCameraAngle = ((ParticleRotation.FaceCamera) rotation).faceCameraAngle;
        }
        else if (rotation instanceof ParticleRotation.EulerAngles) {
            rotationMode = "euler";
            yaw = ((ParticleRotation.EulerAngles) rotation).yaw;
            pitch = ((ParticleRotation.EulerAngles) rotation).pitch;
            roll = ((ParticleRotation.EulerAngles) rotation).roll;
        }
        else {
            rotationMode = "orient";
            Vec3 vec = ((ParticleRotation.OrientVector)rotation).orientation;
            yaw = (float) vec.x;
            pitch = (float) vec.y;
            roll = (float) vec.z;
        }

        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %s %.2f %.2f %.2f %.2f %b %.2f %.2f %b", ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()),
                this.airDrag, this.red, this.green, this.blue, this.alpha, rotationMode, this.scale, yaw, pitch, roll, this.emissive, this.duration, faceCameraAngle, canCollide);
    }

    @OnlyIn(Dist.CLIENT)
    public double getRed() { return this.red; }
    @OnlyIn(Dist.CLIENT)
    public double getGreen() { return this.green; }
    @OnlyIn(Dist.CLIENT)
    public double getBlue() { return this.blue; }
    @OnlyIn(Dist.CLIENT)
    public double getAlpha() { return this.alpha; }
    @OnlyIn(Dist.CLIENT)
    public double getAirDrag() { return airDrag; }
    @OnlyIn(Dist.CLIENT)
    public ParticleRotation getRotation() { return rotation; }
    @OnlyIn(Dist.CLIENT)
    public double getScale() { return scale; }
    @OnlyIn(Dist.CLIENT)
    public boolean isEmissive() { return emissive; }
    @OnlyIn(Dist.CLIENT)
    public double getDuration() { return duration; }
    @OnlyIn(Dist.CLIENT)
    public boolean getCanCollide() { return canCollide; }
    @OnlyIn(Dist.CLIENT)
    public ParticleComponent[] getComponents() { return components; }

    /**
     * 用于从命令或数据包反序列化粒子数据的 Deserializer。
     */
    public static final Deserializer<ParticleData> DESERIALIZER = new Deserializer<ParticleData>() {
        /**
         * 从命令字符串中解析粒子数据。
         */
        public ParticleData fromCommand(ParticleType<ParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double airDrag = reader.readDouble();
            reader.expect(' ');
            double red = reader.readDouble();
            reader.expect(' ');
            double green = reader.readDouble();
            reader.expect(' ');
            double blue = reader.readDouble();
            reader.expect(' ');
            double alpha = reader.readDouble();
            reader.expect(' ');
            String rotationMode = reader.readString();
            reader.expect(' ');
            double scale = reader.readDouble();
            reader.expect(' ');
            double yaw = reader.readDouble();
            reader.expect(' ');
            double pitch = reader.readDouble();
            reader.expect(' ');
            double roll = reader.readDouble();
            reader.expect(' ');
            boolean emissive = reader.readBoolean();
            reader.expect(' ');
            double duration = reader.readDouble();
            reader.expect(' ');
            double faceCameraAngle = reader.readDouble();
            reader.expect(' ');
            boolean canCollide = reader.readBoolean();

            ParticleRotation rotation;
            if (rotationMode.equals("face_camera")) rotation = new ParticleRotation.FaceCamera((float) faceCameraAngle);
            else if (rotationMode.equals("euler")) rotation = new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
            else rotation = new ParticleRotation.OrientVector(new Vec3(yaw, pitch, roll));

            // 组件(components)在此处为空，因为它们不能通过命令传输
            return new ParticleData(particleTypeIn, rotation, scale, red, green, blue, alpha, airDrag, duration, emissive, canCollide, new ParticleComponent[]{});
        }

        /**
         * 从网络缓冲区 (FriendlyByteBuf) 中解析粒子数据。
         */
        public ParticleData fromNetwork(ParticleType<ParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            double airDrag = buffer.readFloat();
            double red = buffer.readFloat();
            double green = buffer.readFloat();
            double blue = buffer.readFloat();
            double alpha = buffer.readFloat();
            String rotationMode = buffer.readUtf();
            double scale = buffer.readFloat();
            double yaw = buffer.readFloat();
            double pitch = buffer.readFloat();
            double roll = buffer.readFloat();
            boolean emissive = buffer.readBoolean();
            double duration = buffer.readFloat();
            double faceCameraAngle = buffer.readFloat();
            boolean canCollide = buffer.readBoolean();

            ParticleRotation rotation;
            if (rotationMode.equals("face_camera")) rotation = new ParticleRotation.FaceCamera((float) faceCameraAngle);
            else if (rotationMode.equals("euler")) rotation = new ParticleRotation.EulerAngles((float)yaw, (float)pitch, (float)roll);
            else rotation = new ParticleRotation.OrientVector(new Vec3(yaw, pitch, roll));

            // 组件(components)在此处为空，因为它们不能通过网络传输
            return new ParticleData(particleTypeIn, rotation, scale, red, green, blue, alpha, airDrag, duration, emissive, canCollide, new ParticleComponent[]{});
        }
    };

    /**
     * 用于数据驱动加载 (例如 JSON 文件) 的 Codec。
     * 这是一个简化版本，不包括旋转或组件。
     */
    public static Codec<ParticleData> CODEC(ParticleType<ParticleData> particleType) {
        return RecordCodecBuilder.create((codecBuilder) -> codecBuilder.group(
                        Codec.DOUBLE.fieldOf("scale").forGetter(ParticleData::getScale),
                        Codec.DOUBLE.fieldOf("r").forGetter(ParticleData::getRed),
                        Codec.DOUBLE.fieldOf("g").forGetter(ParticleData::getGreen),
                        Codec.DOUBLE.fieldOf("b").forGetter(ParticleData::getBlue),
                        Codec.DOUBLE.fieldOf("a").forGetter(ParticleData::getAlpha),
                        Codec.DOUBLE.fieldOf("drag").forGetter(ParticleData::getAirDrag),
                        Codec.DOUBLE.fieldOf("duration").forGetter(ParticleData::getDuration),
                        Codec.BOOL.fieldOf("emissive").forGetter(ParticleData::isEmissive),
                        Codec.BOOL.fieldOf("canCollide").forGetter(ParticleData::getCanCollide)
                ).apply(codecBuilder, (scale, r, g, b, a, drag, duration, emissive, canCollide) ->
                        new ParticleData(particleType, new ParticleRotation.FaceCamera(0), scale, r, g, b, a, drag, duration, emissive, canCollide, new ParticleComponent[]{}))
        );
    }
}