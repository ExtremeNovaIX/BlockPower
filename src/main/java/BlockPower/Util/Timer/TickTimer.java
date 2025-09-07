package BlockPower.Util.Timer;

public class TickTimer {
    private long startTick;

    private long nowTick;

    private int tickDuration;

    public TickTimer(int tickDuration) {
        this.startTick = TickListener.getServerTicks();
        this.tickDuration = tickDuration;
    }

    /**
     * 等待一段时间，具体用法请查看testTimer
     * @return true: 等待时间已到
     *         false: 等待时间未到
     */
    public boolean isFinished() {
        this.setNowTick(TickListener.getServerTicks() - this.getStartTick());
        return this.getNowTick()  >= tickDuration;
    }

    public long getStartTick() {
        return startTick;
    }

    public int getTickDuration() {
        return tickDuration;
    }

    public void setTickDuration(int tickDuration) {
        this.tickDuration = tickDuration;
    }

    public long getNowTick() {
        return nowTick;
    }

    public void setNowTick(long nowTick) {
        this.nowTick = nowTick;
    }
}
