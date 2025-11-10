package BlockPower.Util.ModEffects;

import BlockPower.Util.ModEffects.ClientEffect.IClientTickBasedEffect;
import BlockPower.ModException.EffectException;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.EffectAddSyncPacket_S2C;
import BlockPower.ModMessages.S2CPacket.EffectRemoveSyncPacket_S2C;
import BlockPower.Util.Commons;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ModEffectManager {


    private static final Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> activeServerEffects = new WeakHashMap<>();
    private static final Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> activeClientEffects = new WeakHashMap<>();

    // 用于暂存下一Tick需要添加的效果。键为实体，值为该实体待添加的效果列表
    private static final Map<Entity, List<ITickBasedEffect>> pendingServerAdditions = new WeakHashMap<>();
    private static final Map<Entity, List<ITickBasedEffect>> pendingClientAdditions = new WeakHashMap<>();

    // 用于暂存下一Tick需要移除的效果。键为实体，值为该实体待移除的效果类集合
    private static final Map<Entity, Set<Class<? extends ITickBasedEffect>>> pendingServerRemovals = new WeakHashMap<>();
    private static final Map<Entity, Set<Class<? extends ITickBasedEffect>>> pendingClientRemovals = new WeakHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ModEffectManager.class);


    /**
     * 根据运行端获取对应的待移除 Map。
     */
    private static Map<Entity, Set<Class<? extends ITickBasedEffect>>> getPendingRemovals(boolean isClientSide) {
        return isClientSide ? pendingClientRemovals : pendingServerRemovals;
    }

    /**
     * 根据运行端获取对应的待添加队列Map。
     */
    private static Map<Entity, List<ITickBasedEffect>> getPendingAdditions(boolean isClientSide) {
        return isClientSide ? pendingClientAdditions : pendingServerAdditions;
    }

    /**
     * 根据运行端获取对应的效果Map。
     *
     * @param isClientSide 是否为客户端
     * @return 对应的效果Map
     */
    private static Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> getEffectMap(boolean isClientSide) {
        return isClientSide ? activeClientEffects : activeServerEffects;
    }

    /**
     * 将添加效果操作推入队列，在下一次 tickAll 迭代开始前安全添加。
     */
    public static void addEffect(Entity entity, ITickBasedEffect effect) {
        // 安全检查：如果实体已移除，则不执行任何操作
        if (entity == null || entity.isRemoved()) {
            return;
        }
        // 根据效果的运行端选择正确的队列
        Map<Entity, List<ITickBasedEffect>> pendingMap = getPendingAdditions(effect.isClientSide());
        // 将效果加入对应实体的待添加列表
        pendingMap.computeIfAbsent(entity, k -> new ArrayList<>()).add(effect);

        // 对于客户端效果，发送添加包进行同步
        if (effect instanceof IClientTickBasedEffect cEffect && entity instanceof ServerPlayer player) {
            if (cEffect.getType() == null) throw new EffectException(cEffect);
            ModMessages.sendToPlayer(new EffectAddSyncPacket_S2C(cEffect), player);
            log.info("Auto send EffectAddSyncPacket_S2C: {} to {}", cEffect, player.getGameProfile().getName());
        }
    }

    public static void addToAllAround(ITickBasedEffect effect, Vec3 pos, Level level, double radius) {
        List<Entity> entityList = Commons.aabbDetectEntity(pos, level, radius, null);

        entityList.forEach(entity -> {
            // 安全检查：如果实体已移除，则跳过
            if (entity.isRemoved()) {
                return;
            }
            // 根据效果的运行端选择正确的队列
            Map<Entity, List<ITickBasedEffect>> pendingMap = getPendingAdditions(effect.isClientSide());
            // 将效果加入对应实体的待添加列表
            pendingMap.computeIfAbsent(entity, k -> new ArrayList<>()).add(effect);
        });

        // 对于客户端效果，发送添加包进行同步
        if (effect instanceof IClientTickBasedEffect cEffect) {
            if (cEffect.getType() == null) throw new EffectException(cEffect);
            ModMessages.sendToAllAround(new EffectAddSyncPacket_S2C(cEffect), level.dimension(), pos.x(), pos.y(), pos.z(), radius);
            log.info("Auto send EffectAddSyncPacket_S2C: {} to all around {}", cEffect, pos);
        }
    }

    /**
     * 将移除实体的指定效果操作推入队列，在下一 Tick 移除。
     *
     * @param entity      实体
     * @param effectClass 效果类
     */
    public static void removeEffect(Entity entity, Class<? extends ITickBasedEffect> effectClass) {
        // 安全检查：如果实体已移除，则不执行任何操作
        if (entity == null || entity.isRemoved()) {
            return;
        }
        Optional<? extends ITickBasedEffect> effect = ModEffectManager.getEntityEffect(entity, effectClass);
        if (effect.isEmpty()) return;
        boolean isClientSide = effect.get().isClientSide();
        // 根据效果的运行端选择正确的队列
        Map<Entity, Set<Class<? extends ITickBasedEffect>>> pendingMap = getPendingRemovals(isClientSide);
        // 将待移除的效果类加入对应实体的Set中
        pendingMap.computeIfAbsent(entity, k -> new HashSet<>()).add(effectClass);

        // 对于客户端效果，还需要发送移除包进行同步
        if (isClientSide && entity instanceof ServerPlayer player) {
            // 检查当前effectClass是否为IClientTickBasedEffect的子类
            if (IClientTickBasedEffect.class.isAssignableFrom(effectClass)) {
                ModMessages.sendToPlayer(new EffectRemoveSyncPacket_S2C(effectClass.asSubclass(IClientTickBasedEffect.class)), player);
                log.info("Auto send EffectRemoveSyncPacket_S2C: {} to {}", effectClass.asSubclass(IClientTickBasedEffect.class), player.getGameProfile().getName());
            }
        }
    }

    /**
     * 获取实体的指定效果。
     *
     * @param entity      实体
     * @param effectClass 效果类
     * @param <T>         效果类型
     * @return 效果的Optional包装
     */
    public static <T extends ITickBasedEffect> Optional<T> getEntityEffect(Entity entity, Class<T> effectClass) {
        if (entity == null || effectClass == null) {
            return Optional.empty();
        }
        // 根据实体所在的世界判断是客户端还是服务端
        boolean isClient = entity.level().isClientSide();
        Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects = getEffectMap(isClient).get(entity);

        if (entityEffects == null) {
            return Optional.empty();
        }
        //确保即使 effect 不存在也不会抛异常
        return Optional.ofNullable(effectClass.cast(entityEffects.get(effectClass)));
    }

    /**
     * 在开始遍历前，处理所有添加和移除操作。
     */
    private static void processPendingModifications(boolean isClientSide) {
        Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> mainMap = getEffectMap(isClientSide);
        Map<Entity, Set<Class<? extends ITickBasedEffect>>> pendingRemovals = getPendingRemovals(isClientSide);
        Map<Entity, List<ITickBasedEffect>> pendingAdds = getPendingAdditions(isClientSide);

        // 创建副本以进行安全迭代
        Map<Entity, Set<Class<? extends ITickBasedEffect>>> removalsCopy = new HashMap<>(pendingRemovals);
        Map<Entity, List<ITickBasedEffect>> addsCopy = new HashMap<>(pendingAdds);

        // 清空原始暂存区，以便在处理期间可以接收新的请求
        pendingRemovals.clear();
        pendingAdds.clear();

        // 遍历副本进行处理
        removalsCopy.forEach((entity, classes) -> {
            Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects = mainMap.get(entity);
            if (entityEffects != null) {
                classes.forEach(entityEffects::remove);
            }
        });

        addsCopy.forEach((entity, effects) -> {
            Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects = mainMap.computeIfAbsent(entity, k -> new HashMap<>());
            for (ITickBasedEffect effect : effects) {
                entityEffects.put(effect.getClass(), effect);
            }
        });
    }

    /**
     * 更新指定端的所有效果。
     *
     * @param isClientSide 是否更新客户端效果
     */
    public static void tickAll(boolean isClientSide) {
        // 处理所有待定修改
        processPendingModifications(isClientSide);

        Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> mainMap = getEffectMap(isClientSide);

        // 创建主效果Map的条目副本进行安全迭代
        Set<Map.Entry<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>>> entries = new HashSet<>(mainMap.entrySet());

        for (Map.Entry<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> entityEntry : entries) {
            Entity entity = entityEntry.getKey();
            Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> effectMap = entityEntry.getValue();

            if (entity == null || entity.isRemoved()) {
                // 如果实体已失效，直接在主Map中移除该条目
                mainMap.remove(entity);
                continue;
            }

            effectMap.values().removeIf(effect -> {
                if (effect.isFinished()) {
                    effect.onEnd();
                    return true;
                } else {
                    effect.tick();
                    return false;
                }
            });
        }

        // 清理所有内部效果Map为空的实体条目
        mainMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
