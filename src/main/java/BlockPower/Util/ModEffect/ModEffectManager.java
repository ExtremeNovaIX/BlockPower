package BlockPower.Util.ModEffect;

import BlockPower.ModEffects.ITickBasedEffect;
import net.minecraft.world.entity.Entity;

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
        // 根据效果的运行端选择正确的队列
        Map<Entity, List<ITickBasedEffect>> pendingMap = getPendingAdditions(effect.isClientSide());
        // 将效果加入对应实体的待添加列表
        pendingMap.computeIfAbsent(entity, k -> new ArrayList<>()).add(effect);
    }

    /**
     * 将移除实体的指定效果操作推入队列，在下一 Tick 移除。
     *
     * @param entity      实体
     * @param effectClass 效果类
     */
    public static void removeEffect(Entity entity, Class<? extends ITickBasedEffect> effectClass) {
        boolean isClientSide = entity.level().isClientSide();
        Map<Entity, Set<Class<? extends ITickBasedEffect>>> pendingMap = getPendingRemovals(isClientSide);

        // 将待移除的效果类加入对应实体的Set中
        pendingMap.computeIfAbsent(entity, k -> new HashSet<>()).add(effectClass);
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
        // 遍历所有待移除的效果
        pendingRemovals.forEach((entity, effectClasses) -> {
            Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects = mainMap.get(entity);
            if (entityEffects != null) {
                for (Class<? extends ITickBasedEffect> effectClass : effectClasses) {
                    entityEffects.remove(effectClass);
                }
            }
        });
        pendingRemovals.clear();

        // 遍历所有待添加的实体和效果
        Map<Entity, List<ITickBasedEffect>> pendingMap = getPendingAdditions(isClientSide);
        pendingMap.forEach((entity, effects) -> {
            // 获取该实体的效果Map，如果不存在则创建
            Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects =
                    mainMap.computeIfAbsent(entity, k -> new HashMap<>());

            // 将所有待添加的效果放入实体Map
            for (ITickBasedEffect effect : effects) {
                // 这里执行原来的 addEffect 核心逻辑：添加或覆盖
                entityEffects.put(effect.getClass(), effect);
            }
        });

        // 清空列表，等待下一 Tick
        pendingMap.clear();
    }

    /**
     * 更新指定端的所有效果。
     *
     * @param isClientSide 是否更新客户端效果
     */
    public static void tickAll(boolean isClientSide) {
        // 处理所有的修改操作
        processPendingModifications(isClientSide);

        // 获取当前端正确的Map
        Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> mainMap = getEffectMap(isClientSide);

        //用于缓存所有需要执行的修改操作（如删除）
        List<Runnable> modifications = new ArrayList<>();

        //这个循环只调用 tick() 和 isFinished()，不进行任何删除或添加操作。将需要执行的删除操作存入modifications列表
        for (Map.Entry<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> entityEntry : mainMap.entrySet()) {
            Entity entity = entityEntry.getKey();
            Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> effectMap = entityEntry.getValue();

            //如果实体本身已失效，安排清空其所有效果
            if (entity.isRemoved()) {
                modifications.add(effectMap::clear);
                continue; //继续检查下一个实体
            }

            //遍历实体身上的每一种效果
            Iterator<ITickBasedEffect> effectIterator = effectMap.values().iterator();
            while (effectIterator.hasNext()) {
                ITickBasedEffect effect = effectIterator.next();

                if (effect.isFinished()) {
                    // 安全移除内部 Map 中的元素
                    effectIterator.remove();
                    effect.onEnd();
                } else {
                    // 执行 tick 逻辑
                    effect.tick();
                }
            }
        }

        //执行所有已安排的修改
        if (!modifications.isEmpty()) {
            for (Runnable modification : modifications) {
                modification.run();
            }
        }

        //清理空条目
        mainMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}