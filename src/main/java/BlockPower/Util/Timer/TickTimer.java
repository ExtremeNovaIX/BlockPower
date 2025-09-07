package BlockPower.Util.Timer;

public class TickTimer {
    private final long startTick;
    private final int tickDuration;
    private final boolean isClientSide;

    public TickTimer(int tickDuration, boolean isClientSide) {
        this.startTick = isClientSide ? TickListener.getClientTicks() : TickListener.getServerTicks();
        this.tickDuration = tickDuration;
        this.isClientSide = isClientSide;
    }

    /**
     * @return true: 等待时间已到;false: 等待时间未到
     */
    public boolean isFinished() {
        // 根据这个计时器的归属，获取当前时间
        long currentTicks = this.isClientSide ? TickListener.getClientTicks() : TickListener.getServerTicks();
        return (currentTicks - this.getStartTick()) >= tickDuration;
    }

    public long getStartTick() {
        return startTick;
    }

    public int getTickDuration() {
        return tickDuration;
    }

}
