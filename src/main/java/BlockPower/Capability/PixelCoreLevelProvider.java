package BlockPower.Capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PixelCoreLevelProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private IPixelCoreLevel coreLevel = null;
    private final LazyOptional<IPixelCoreLevel> optional = LazyOptional.of(this::getOrCreateCoreLevel);

    private IPixelCoreLevel getOrCreateCoreLevel() {
        if (this.coreLevel == null) {
            this.coreLevel = new PixelCoreLevel();
        }
        return this.coreLevel;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.PIXEL_CORE_LEVEL) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("level", getOrCreateCoreLevel().getLevel());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateCoreLevel().setLevel(nbt.getInt("level"));
    }
}
