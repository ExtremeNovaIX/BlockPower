package BlockPower.Capability.AirJump;

/**
 * IPlayerAirJumpData 接口的默认实现。
 */
public class PlayerAirJumpData implements IPlayerAirJumpData {
    private int jumpCount = 0;

    @Override
    public int getJumpCount() {
        return this.jumpCount;
    }

    @Override
    public void setJumpCount(int count) {
        this.jumpCount = count;
    }

    @Override
    public void incrementJumpCount() {
        this.jumpCount++;
    }

    @Override
    public void resetJumpCount() {
        this.jumpCount = 0;
    }
}
