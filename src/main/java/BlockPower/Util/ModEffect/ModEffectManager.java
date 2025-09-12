package BlockPower.Util.ModEffect;

import BlockPower.ModEffects.ITickBasedEffect;
import net.minecraft.world.entity.Entity;

import java.util.*;

public class ModEffectManager {

    private static final Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> activeServerEffects = new WeakHashMap<>();
    private static final Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> activeClientEffects = new WeakHashMap<>();

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
     * 为实体添加一个效果。
     * 如果实体已存在同类型效果，则直接覆盖。
     *
     * @param entity 实体
     * @param effect 效果
     */
    public static void addEffect(Entity entity, ITickBasedEffect effect) {
        //获取当前端正确的Map
        Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> mainMap = getEffectMap(effect.isClientSide());

        //获取该实体的效果Map，如果不存在则创建
        Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects =
                mainMap.computeIfAbsent(entity, k -> new HashMap<>());

        //添加或覆盖效果
        entityEffects.put(effect.getClass(), effect);
    }

    /**
     * 移除实体的指定效果。
     *
     * @param entity      实体
     * @param effectClass 效果类
     */
    public static void removeEffect(Entity entity, Class<? extends ITickBasedEffect> effectClass) {
        //获取当前端正确的Map
        Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> mainMap = getEffectMap(entity.level().isClientSide());

        //获取该实体的效果Map
        Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects = mainMap.get(entity);
        if (entityEffects != null) {
            entityEffects.remove(effectClass);
        }
    }

    /**
     * 移除实体的所有效果。
     *
     * @param entity 实体
     */
    public static void removeAllEffects(Entity entity) {
        //获取当前端正确的Map
        Map<Entity, Map<Class<? extends ITickBasedEffect>, ITickBasedEffect>> mainMap = getEffectMap(entity instanceof net.minecraft.client.player.LocalPlayer);

        //获取该实体的效果Map
        Map<Class<? extends ITickBasedEffect>, ITickBasedEffect> entityEffects = mainMap.get(entity);
        if (entityEffects != null) {
            entityEffects.clear();
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
     * 更新指定端的所有效果。
     *
     * @param isClientSide 是否更新客户端效果
     */
    public static void tickAll(boolean isClientSide) {
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
            for (ITickBasedEffect effect : effectMap.values()) {
                if (effect.isFinished()) {
                    //如果效果已结束，安排一个删除该效果的操作
                    modifications.add(() -> effectMap.remove(effect.getClass()));
                } else {
                    //否则，执行效果的 tick 逻辑
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