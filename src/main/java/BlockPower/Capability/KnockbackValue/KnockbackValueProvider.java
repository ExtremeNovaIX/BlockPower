package BlockPower.Capability.KnockbackValue;

import BlockPower.Capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockbackValueProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final KnockbackValueImpl knockbackValueImpl;
    private final LazyOptional<IKnockbackValue> optional;

    // 默认构造函数，用于客户端或不需要回调的场景
    public KnockbackValueProvider() {
        this.knockbackValueImpl = new KnockbackValueImpl();
        this.optional = LazyOptional.of(() -> this.knockbackValueImpl);
    }

    // 带回调的构造函数，用于服务器端
    public KnockbackValueProvider(Runnable onValueUpdate) {
        this.knockbackValueImpl = new KnockbackValueImpl(onValueUpdate);
        this.optional = LazyOptional.of(() -> this.knockbackValueImpl);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ModCapabilities.KNOCKBACK_VALUE_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return knockbackValueImpl.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        knockbackValueImpl.deserializeNBT(nbt);
    }
}
