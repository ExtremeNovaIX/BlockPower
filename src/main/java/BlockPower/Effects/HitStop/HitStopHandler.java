package BlockPower.Effects.HitStop;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * 一个只存在于客户端的处理器，专门用于实现“打击停顿”（Hit Stop）效果。
 * 它通过在极短时间内冻结客户端的渲染更新，来营造强烈的打击感。
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class HitStopHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 通过反射获取到的 Minecraft 类中的 Timer 字段。
     */
    private static final Field timerField;

    /**
     * 通过反射获取到的 Minecraft Timer 类中的 partialTick 字段。
     */
    private static final Field partialTickField;

    /**
     * 静态变量，用于记录卡帧效果的结束系统时间。
     */
    private static long hitStopEndTime = 0;

    static {
        Field tempTimerField = null;
        Field tempPartialTickField = null;
        try {
            //反射获取 Minecraft类中的timer字段
            tempTimerField = Minecraft.class.getDeclaredField("timer");
            tempTimerField.setAccessible(true);

            //反射获取Timer类中的partialTick字段
            tempPartialTickField = Timer.class.getDeclaredField("partialTick");
            tempPartialTickField.setAccessible(true);

        } catch (NoSuchFieldException e) {
            // 如果因为游戏更新等原因字段名改变，这里会抛出异常
            LOGGER.error("Could not find required fields for HitStop effect. It will be disabled.", e);
        }
        timerField = tempTimerField;
        partialTickField = tempPartialTickField;
    }

    /**
     * 这是一个公开的静态方法，作为外部触发卡帧效果的入口。
     *
     * @param durationTicks 卡帧效果需要持续的 tick 数量。
     */
    public static void start(int durationTicks) {
        // 将游戏tick转换为毫秒(1tick=50ms)
        long durationMillis = durationTicks * 50L;
        long newEndTime = System.currentTimeMillis() + durationMillis;

        //为了防止新的卡帧效果覆盖掉正在进行的，总是取更晚的结束时间
        hitStopEndTime = Math.max(hitStopEndTime, newEndTime);
    }

    //监听客户端的渲染Tick事件。
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

            // 检查是否正处于卡帧状态
            if (System.currentTimeMillis() < hitStopEndTime) {
                // 确保成功获取到了所有反射字段
                if (timerField != null && partialTickField != null) {
                    try {
                        // 通过反射从 Minecraft 实例中获取到 Timer 对象
                        Timer timer = (Timer) timerField.get(Minecraft.getInstance());

                        // 通过反射，强行将该 Timer 对象的 partialTick 设置为 0。
                        partialTickField.setFloat(timer, 0.0f);

                    } catch (IllegalAccessException e) {
                        LOGGER.error("Could not access required fields for HitStop effect.", e);
                    }
                }
            }
        }
    }
}
