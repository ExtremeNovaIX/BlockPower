package BlockPower.Util;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * 一个全局管理器，用于处理实体间的命中冷却（无敌帧）。
 * 这可以防止单一来源的攻击在短时间内对同一目标造成多次伤害。
 * 使用 WeakHashMap 来自动清理已销毁的目标实体，防止内存泄漏。
 */
public class HitCooldownManager {

    private static final HitCooldownManager INSTANCE = new HitCooldownManager();

    // 结构: <目标实体, Map<攻击源实体的UUID, 冷却结束的游戏时间 tick>>
    private final Map<Entity, Map<UUID, Long>> cooldowns = new WeakHashMap<>();

    private HitCooldownManager() {}

    public static HitCooldownManager getInstance() {
        return INSTANCE;
    }

    /**
     * 检查一个攻击源是否可以命中一个目标，如果可以，则自动为目标设置冷却。
     * 这是一个有副作用的方法，调用它即代表尝试进行一次攻击。
     *
     * @param source        攻击源实体。
     * @param target        被攻击的目标实体。
     * @param cooldownTicks 命中后需要冷却的 tick 数量。
     * @return 如果可以命中，返回 true；如果目标对于此攻击源仍在冷却中，返回 false。
     */
    public boolean canHit(Entity source, Entity target, int cooldownTicks) {
        if (source == null || target == null || target.level().isClientSide()) {
            return false; // 不在服务端处理或实体无效
        }

        long currentTime = target.level().getGameTime();
        Map<UUID, Long> targetCooldowns = cooldowns.get(target);

        // 检查目标是否对该攻击源有冷却记录
        if (targetCooldowns != null) {
            Long expirationTime = targetCooldowns.get(source.getUUID());
            if (expirationTime != null && currentTime < expirationTime) {
                return false; // 仍在冷却中，不可命中
            }
        }

        // 如果可以命中，则设置新的冷却时间
        cooldowns.computeIfAbsent(target, k -> new HashMap<>())
                 .put(source.getUUID(), currentTime + cooldownTicks);

        return true; // 可以命中
    }
}
