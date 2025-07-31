package BlockPower.Util.Visual;
import BlockPower.Main.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class VisualEffect {
        public static final ResourceLocation CROSS_STAR_TEXTURE = new ResourceLocation(Main.MOD_ID, "textures/effect/cross_star.png");
    public static final ResourceLocation FIRE_RING_TEXTURE = new ResourceLocation(Main.MOD_ID, "textures/effect/fire_ring.png");

    public enum EffectType {
        CROSS_STAR,
        FIRE_RING
    }

    public final EffectType type;
    public final Vec3 position;
    public final int maxAge;

    public int age;
    public float scale;
    public float alpha;
    public float rotation;

    public VisualEffect(EffectType type, Vec3 position, int maxAge) {
        this.type = type;
        this.position = position;
        this.maxAge = maxAge;
        this.age = 0;
        this.scale = 1.0f;
        this.alpha = 1.0f;
        this.rotation = 0.0f;
    }

    // 判断特效是否存活
    public boolean isAlive() {
        return this.age < this.maxAge;
    }

    // 更新特效状态，在每个tick调用
    public void tick() {
        this.age++;
        // 示例：简单的线性淡出和放大动画
        this.alpha = 1.0f - ((float)this.age / (float)this.maxAge);
        this.scale = 1.0f + ((float)this.age / (float)this.maxAge) * 0.5f; // 从1倍放大到1.5倍
    }

    public ResourceLocation getTexture() {
        return switch (this.type) {
            case CROSS_STAR -> CROSS_STAR_TEXTURE;
            case FIRE_RING -> FIRE_RING_TEXTURE; // 即使还没用，也先加上
            // 如果有更多类型，在这里添加
            default -> CROSS_STAR_TEXTURE; // 提供一个默认值
        };
    }
}