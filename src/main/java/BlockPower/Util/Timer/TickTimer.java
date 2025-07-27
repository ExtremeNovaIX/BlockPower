package BlockPower.Util.Timer;

import org.jetbrains.annotations.NotNull;

public class TickTimer {
    private int startTick;

    private int nowTick;

    private int tickDuration;

    public int getNowTick() {
        return nowTick;
    }

    public void setNowTick(int nowTick) {
        this.nowTick = nowTick;
    }

    public TickTimer(int tickDuration) {
        this.startTick = ServerTickListener.getTicks();
        this.tickDuration = tickDuration;
    }

    /**
     * 等待一段时间，具体用法请查看testTimer
     * @param tickTimer 计时器对象
     * @return true: 等待时间已到
     *         false: 等待时间未到
     */
    public boolean updateTimer(@NotNull TickTimer tickTimer) {
        tickTimer.setNowTick(ServerTickListener.getTicks() - tickTimer.getStartTick());
        return tickTimer.getNowTick()  >= tickDuration;
    }

    public int getStartTick() {
        return startTick;
    }

    public void setStartTick(int startTick) {
        this.startTick = startTick;
    }

    public int getTickDuration() {
        return tickDuration;
    }
}
