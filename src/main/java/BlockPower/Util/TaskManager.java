package BlockPower.Util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.Entity;

import java.util.*;

@Mod.EventBusSubscriber
public class TaskManager {
    private static final TaskManager INSTANCE = new TaskManager();
    public static final List<Map.Entry<Runnable, Integer>> scheduledTasks = new ArrayList<>();
    public static final Map<Entity, Map<String, Integer>> methodExecutionCounter = new WeakHashMap<>();
    private static int d1 = 0;

    private TaskManager() {
    }

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    /**
     * 添加任务到自更新timer池内，在tickDuration后执行。
     * 没有重复检查，最好不要在循环内执行。
     * @param tickDuration 在多少tick后执行。
     * @param runnable 执行的方法或者语句。
     */
    public void runTaskAfterTicks(@NotNull Integer tickDuration, @NotNull Runnable runnable) {
        scheduledTasks.add(new AbstractMap.SimpleEntry<>(runnable, tickDuration));
    }

    public void runOnce(@NotNull Entity entity, @NotNull String methodName, @NotNull Runnable runnable) {
        runTimes(entity, methodName, runnable, 1);
    }

    public void runTimes(@NotNull Entity entity, @NotNull String methodName, @NotNull Runnable runnable, @NotNull Integer times) {
        if (entity.isRemoved()) {
            methodExecutionCounter.remove(entity);
            return;
        }
        // 获取该实体的内部Map，如果不存在则创建一个
        Map<String, Integer> innerMap = methodExecutionCounter.computeIfAbsent(entity, k -> new HashMap<>());

        // 使用getOrDefault 获取当前剩余次数。如果不存在，则使用传入的 times 作为初始值
        int remainTimes = innerMap.getOrDefault(methodName, times);

        if (remainTimes > 0) {
            runnable.run();
            innerMap.put(methodName, remainTimes - 1);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            updateScheduledTasksList();
//            updateMethodExecutionCounter();
        }
    }

    private static void updateScheduledTasksList() {
        scheduledTasks.removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            if (entry.getValue() <= 0) {
                entry.getKey().run();
                return true;
            }
            return false;
        });
    }

    private static void updateMethodExecutionCounter() {
        d1++;
        if (!(d1 % 36000 == 0)) {
            return;
        }
        d1 = 0;
        methodExecutionCounter.forEach((entity, map) -> {
            if (entity.isRemoved()) {
                methodExecutionCounter.remove(entity);
            }
        });
    }
}
