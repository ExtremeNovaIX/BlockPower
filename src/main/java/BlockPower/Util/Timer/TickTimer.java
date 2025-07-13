package BlockPower.Util.Timer;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TickTimer {
    private int startTick;

    private int nowTick;

    public int getNowTick() {
        return nowTick;
    }

    public void setNowTick(int nowTick) {
        this.nowTick = nowTick;
    }

    public TickTimer() {
        this.startTick = ServerTickListener.getTicks();
    }

    /**
     * 等待一段时间，具体用法请查看testTimer
     * @param tickTimer 计时器对象
     * @param tickDuration 等待时间
     * @return true: 等待时间已到
     *         false: 等待时间未到
     */
    public boolean waitTicks(@NotNull TickTimer tickTimer, int tickDuration) {
        tickTimer.setNowTick(ServerTickListener.getTicks() - tickTimer.getStartTick());
        return tickTimer.getNowTick()  >= tickDuration;
    }

    public int getStartTick() {
        return startTick;
    }

    public void setStartTick(int startTick) {
        this.startTick = startTick;
    }
}
