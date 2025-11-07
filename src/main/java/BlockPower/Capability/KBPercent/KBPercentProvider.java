package BlockPower.Capability.KBPercent;

import BlockPower.Capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KBPercentProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final KBPercentImpl KBPercentImpl;
    private final LazyOptional<IKBPercent> optional;

    // 默认构造函数，用于客户端或不需要回调的场景
    public KBPercentProvider() {
        this.KBPercentImpl = new KBPercentImpl();
        this.optional = LazyOptional.of(() -> this.KBPercentImpl);
    }

    // 带回调的构造函数，用于服务器端
    public KBPercentProvider(Runnable onValueUpdate) {
        this.KBPercentImpl = new KBPercentImpl(onValueUpdate);
        this.optional = LazyOptional.of(() -> this.KBPercentImpl);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ModCapabilities.KNOCKBACK_VALUE_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return KBPercentImpl.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        KBPercentImpl.deserializeNBT(nbt);
    }
}
