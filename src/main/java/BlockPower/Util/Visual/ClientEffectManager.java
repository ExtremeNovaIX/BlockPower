package BlockPower.Util.Visual;// ClientEffectManager.java
import BlockPower.Util.Visual.VisualEffect;
import com.google.common.collect.Lists;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientEffectManager {
    // 使用CopyOnWriteArrayList以避免在迭代时修改列表导致的ConcurrentModificationException
    private static final List<VisualEffect> activeEffects = new CopyOnWriteArrayList<>();

    public static void spawnEffect(VisualEffect.EffectType type, Vec3 position, int durationTicks) {
        activeEffects.add(new VisualEffect(type, position, durationTicks));
    }

    public static void tick() {
        // 迭代并更新所有特效，移除生命周期结束的特效
        activeEffects.removeIf(effect -> {
            effect.tick();
            return!effect.isAlive();
        });
    }

    public static List<VisualEffect> getActiveEffects() {
        return activeEffects;
    }
}