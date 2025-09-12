package BlockPower.ModEffects;

import BlockPower.Util.Timer.TickTimer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * 一个只存在于客户端的处理器，专门用于实现“打击停顿”（Hit Stop）效果。
 * 它通过在极短时间内冻结客户端的渲染更新，来营造强烈的打击感。
 */
@OnlyIn(Dist.CLIENT)
public class HitStopEffect implements ITickBasedEffect {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 通过反射获取到的 Minecraft 类中的 Timer 字段。
     */
    private static final Field timerField;

    /**
     * 通过反射获取到的 Minecraft Timer 类中的 partialTick 字段。
     */
    private static final Field partialTickField;

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

    private final TickTimer tickTimer;

    public HitStopEffect(int duration) {
        tickTimer = new TickTimer(duration, false);
    }

    @Override
    public void tick() {
        // 检查是否正处于卡帧状态
        if (tickTimer.isFinished()) {
            return;
        }
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

    @Override
    public boolean isFinished() {
        return tickTimer.isFinished();
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
