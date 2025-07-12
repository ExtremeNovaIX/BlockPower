package BlockPower.Util.Timer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Timer {

    private int tick = 0;
    private int tickDuration = 0;
    private boolean isEnd = false;

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    private static Timer instance;

    public Timer(int tick, int tickDuration) {
        this.tick = tick;
        this.tickDuration = tickDuration;
        instance = this;
    }

    public static boolean timer(int tick, int tickDuration){
        Timer timer = new Timer(tick, tickDuration);
        return timer.isEnd();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Timer timer = Timer.getInstance();
            if (timer != null) {
                int newTick = timer.getTickDuration();
                if (!(newTick <= 0) && timer.getTick() == 0) {
                    timer.setTick(newTick);
                }
                int nowTick = timer.getTick();
                if (nowTick > 0) {
                    timer.setTick(nowTick - 1);
                }else{
                    timer.setEnd(true);
                    event.setCanceled(true);
                }
            }
        }
    }


    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public int getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
    }

    public static Timer getInstance() {
        return instance;
    }
}
