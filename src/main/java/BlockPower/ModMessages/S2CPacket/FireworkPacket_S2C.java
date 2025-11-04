package BlockPower.ModMessages.S2CPacket;

import BlockPower.Util.ModParticles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class FireworkPacket_S2C extends AbstractS2CPacket {
    private final double x;
    private final double y;
    private final double z;

    private final float r1;
    private final float g1;
    private final float b1;

    private final float r2;
    private final float g2;
    private final float b2;

    public FireworkPacket_S2C(Vec3 pos, Vector3f color1, Vector3f color2) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.r1 = color1.x;
        this.g1 = color1.y;
        this.b1 = color1.z;
        this.r2 = color2.x;
        this.g2 = color2.y;
        this.b2 = color2.z;
    }

    public FireworkPacket_S2C(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.r1 = buf.readFloat();
        this.g1 = buf.readFloat();
        this.b1 = buf.readFloat();
        this.r2 = buf.readFloat();
        this.g2 = buf.readFloat();
        this.b2 = buf.readFloat();
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(r1);
        buf.writeFloat(g1);
        buf.writeFloat(b1);
        buf.writeFloat(r2);
        buf.writeFloat(g2);
        buf.writeFloat(b2);
    }

    @Override
    protected void handleClientSide() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Vec3 pos = new Vec3(x, y, z);
        Vector3f color1 = new Vector3f(r1, g1, b1);
        Vector3f color2 = new Vector3f(r2, g2, b2);
        spawnFirework(player, pos, color1, color2);
    }

    public static void spawnFirework(LocalPlayer player, Vec3 pos, Vector3f color1, Vector3f color2) {
        Level level = player.level();
        // 拖尾组件
        ParticleComponent[] ribbonComponents = new ParticleComponent[]{
                new ParticleComponent.PropertyOverLength(
                        ParticleComponent.PropertyOverLength.EnumRibbonProperty.ALPHA,
                        ParticleComponent.KeyTrack.startAndEnd(1.0f, 0.0f)
                ),

                new ParticleComponent.PropertyOverLength(
                        ParticleComponent.PropertyOverLength.EnumRibbonProperty.SCALE,
                        ParticleComponent.KeyTrack.startAndEnd(1.0f, 0.0f)
                )
        };

        int particleCount = 150;
        for (int i = 0; i < particleCount; i++) {
            // 随机化
            double xd = level.random.nextGaussian();
            double yd = level.random.nextGaussian();
            double zd = level.random.nextGaussian();
            double length = Mth.sqrt((float) (xd * xd + yd * yd + zd * zd));
            if (length == 0) length = 1;

            float speed = 0.8f;
            xd = (xd / length) * speed;
            yd = (yd / length) * speed;
            zd = (zd / length) * speed;

            float r, g, b;
            if (level.random.nextBoolean()) {
                r = color1.x;
                g = color1.y;
                b = color1.z;
            } else {
                r = color2.x;
                g = color2.y;
                b = color2.z;
            }


            // 头部粒子
            ParticleComponent[] headComponents = new ParticleComponent[]{
                    new ParticleComponent.PropertyControl(
                            ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA,
                            ParticleComponent.KeyTrack.startAndEnd(0.0f, 0.0f),
                            false
                    ),
                    new RibbonComponent(
                            ModParticles.RIBBON_PARTICLE.get(),
                            15,

                            0, 0, 0,
                            0.2,
                            r, g, b,
                            1.0,
                            true,
                            true,
                            ribbonComponents
                    )
            };

            // 创建粒子Data
            ParticleData headParticleData = new ParticleData(
                    ModParticles.ADVANCED_PARTICLE.get(),
                    new ParticleRotation.FaceCamera(0),
                    0.0f,
                    1.0f, 1.0f, 1.0f, 0.0f,
                    0.98f,
                    20,
                    true,
                    false,
                    headComponents
            );
            level.addParticle(headParticleData, pos.x(), pos.y(), pos.z(), xd, yd, zd);
        }
    }
}
