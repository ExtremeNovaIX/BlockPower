package BlockPower.Util.Timer;

import net.minecraftforge.event.TickEvent;

import static BlockPower.Main.Main.sendDebugMessage;

//@Mod.EventBusSubscriber

/**
 * 这个类是用来演示如何使用TickTimer
 */
public class testTimer {

    //第一步，定义一个TickTimer但是不初始化
    private TickTimer timer;

//    @SubscribeEvent
    //第二步，在tick相关事件或者循环中使用如下结构
    public void onServerTick(TickEvent.PlayerTickEvent event) {
        if(timer == null) {
            timer = new TickTimer();
        }
        if (event.phase == TickEvent.Phase.END) {
            if (timer.waitTicks(timer,100)) {
                //在这里写等待后的逻辑
            }
        }
    }
}
