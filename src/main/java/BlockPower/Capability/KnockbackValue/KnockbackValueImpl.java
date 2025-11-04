package BlockPower.Capability.KnockbackValue;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class KnockbackValueImpl implements IKnockbackValue, INBTSerializable<CompoundTag> {
    private double knockbackValue = 0.0;
    private long lastUpdateTick = 0;

    private static final int DECAY_DELAY_TICKS = 300;// 开始衰减的时间
    private static final double DECAY_AMOUNT_PER_TICK = 0.5; // 每tick衰减

    private Runnable onValueChanged;

    public KnockbackValueImpl() {
        this(null);
    }

    public KnockbackValueImpl(Runnable onValueChanged) {
        this.onValueChanged = onValueChanged;
    }

    @Override
    public double getKnockbackValue() {
        return knockbackValue;
    }

    @Override
    public void setKnockbackValue(double value) {
        double oldValue = this.knockbackValue;
        this.knockbackValue = Math.max(0.0, value);
        if (this.knockbackValue != oldValue) {
            markDirty();
        }
    }

    @Override
    public void addKnockbackValue(double amount) {
        if (amount > 0) {
            this.knockbackValue += amount;
            // 只有当值真正增加时才更新时间戳
            this.lastUpdateTick = -1; // 使用-1作为标记，在tick方法中更新为当前tick
            markDirty();
        }
    }

    @Override
    public void resetKnockbackValue() {
        this.knockbackValue = 0.0;
        this.lastUpdateTick = 0;
        markDirty();
    }

    @Override
    public void tick(long currentTick) {
        // 如果lastUpdateTick是-1，说明是在本tick内被addKnockbackValue调用的，更新为当前tick
        if (this.lastUpdateTick == -1) {
            this.lastUpdateTick = currentTick;
        }

        if (knockbackValue <= 0) {
            return; // 如果击退值为0，则无需处理
        }

        // 检查是否超过了不衰减的时间
        if (currentTick - lastUpdateTick > DECAY_DELAY_TICKS) {
            setKnockbackValue(this.knockbackValue - DECAY_AMOUNT_PER_TICK);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("knockbackValue", knockbackValue);
        nbt.putLong("lastUpdateTick", lastUpdateTick);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.knockbackValue = nbt.getDouble("knockbackValue");
        this.lastUpdateTick = nbt.getLong("lastUpdateTick");
    }

    @Override
    public void markDirty() {
        if (onValueChanged != null) {
            onValueChanged.run();
        }
    }
}
