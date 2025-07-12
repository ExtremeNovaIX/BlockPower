package BlockPower.Util.Timer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Timer {

    private int tick = 0;
    private int tickDuration = 0;
    private boolean isEnd = false;

    public Timer(int tick,int tickDuration){
        this.tick = tick;
        this.tickDuration = tickDuration;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            if(tick == 0)
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
}
