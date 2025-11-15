package BlockPower.Capability.AirJump;

import BlockPower.Capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerAirJumpDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private IPlayerAirJumpData airJumpData = null;
    private final LazyOptional<IPlayerAirJumpData> optional = LazyOptional.of(this::getOrCreateAirJumpData);

    private IPlayerAirJumpData getOrCreateAirJumpData() {
        if (this.airJumpData == null) {
            this.airJumpData = new PlayerAirJumpData();
        }
        return this.airJumpData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.PLAYER_AIR_JUMP_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("jumpCount", getOrCreateAirJumpData().getJumpCount());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateAirJumpData().setJumpCount(nbt.getInt("jumpCount"));
    }
}
