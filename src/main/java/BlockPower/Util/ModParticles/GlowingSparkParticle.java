package BlockPower.Util.ModParticles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class GlowingSparkParticle extends SimpleAnimatedParticle {
    private final int fadeStartTick;

    protected GlowingSparkParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSpriteSet, Vector3f color) {
        super(pLevel, pX, pY, pZ, pSpriteSet, 0.0F);

        // 从贴图表中随机选择一个起始贴图
        this.pickSprite(pSpriteSet);

        // 应用颜色
        this.setColor(color.x(), color.y(), color.z());

        // 设置速度
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;

        this.fadeStartTick = 20;
        // 设置生命周期
        this.lifetime = 30 + this.random.nextInt(4);

        // 设置摩擦力
        this.friction = 0.9F;

        // 无碰撞
        this.hasPhysics = false;
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        super.tick();
        // 淡出效果
        if (this.age < this.fadeStartTick) {
            this.alpha = 1.0F;
        } else {
            // 20 tick 之后，开始计算淡出
            int ticksInFadePhase = this.age - this.fadeStartTick;
            int totalFadeDuration = this.lifetime - this.fadeStartTick;

            if (totalFadeDuration <= 0) {
                this.alpha = 0.0F;
            } else {
                float fadePercent = (float) ticksInFadePhase / (float) totalFadeDuration;
                this.alpha = 1.0F - fadePercent;
            }
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<GlowingSparkParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        public Particle createParticle(GlowingSparkParticleOptions pOptions, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            // 从pOptions中获取颜色
            Vector3f color = pOptions.getColor();

            // 返回自定义粒子，并传入颜色
            return new GlowingSparkParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprites, color);
        }
    }
}

