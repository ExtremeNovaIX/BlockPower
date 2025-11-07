package BlockPower.Capability.KBPercent;

import net.minecraft.nbt.CompoundTag;

public interface IKBPercent {

    /**
     * 获取当前的击退值百分比。
     * @return 击退值百分比 (例如，100.0 代表 100%)
     */
    double getKBPercent();

    /**
     * 设置击退值百分比。
     * @param value 新的击退值百分比
     */
    void setKBPercent(double value);

    /**
     * 增加击退值百分比。
     * @param amount 增加的量
     */
    void addKBPercent(double amount);

    /**
     * 重置击退值百分比为0。
     */
    void resetKBPercent();

    /**
     * 在每个tick更新时调用，用于处理内部逻辑，例如自动衰减。
     * @param currentTick 当前世界的总tick数
     */
    void tick(long currentTick);

    /**
     * 数据序列化到NBT
     * @return 包含数据的CompoundTag
     */
    CompoundTag serializeNBT();

    /**
     * 从NBT反序列化数据
     * @param nbt 包含数据的CompoundTag
     */
    void deserializeNBT(CompoundTag nbt);

    /**
     * 标记数据已更改，需要同步到客户端或保存。
     * (这通常在实现类中调用，并触发网络同步)
     */
    void markDirty();
}
