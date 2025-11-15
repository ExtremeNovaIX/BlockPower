package BlockPower.Capability;

/**
 * IPixelCoreLevel 接口的默认实现。
 */
public class PixelCoreLevel implements IPixelCoreLevel {
    private int level = 1;
    private static final int MAX_LEVEL = 6;

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int level) {
        if (level > 0 && level <= MAX_LEVEL) {
            this.level = level;
        }
    }

    @Override
    public int upgradeLevel() {
        this.level++;
        if (this.level > MAX_LEVEL) {
            this.level = 1;
        }
        return this.level;
    }
}
