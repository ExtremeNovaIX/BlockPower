package BlockPower.Capability;

import BlockPower.Skills.MinerState.server.ResourceType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class PlayerMinerStateProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private IPlayerMinerState minerState = null;
    private final LazyOptional<IPlayerMinerState> optional = LazyOptional.of(this::getOrCreateMinerState);

    private IPlayerMinerState getOrCreateMinerState() {
        if (this.minerState == null) {
            this.minerState = new PlayerMinerState();
        }
        return this.minerState;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.PLAYER_MINER_STATE) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        IPlayerMinerState state = getOrCreateMinerState();
        nbt.putBoolean("isMinerMode", state.isMinerMode());

        CompoundTag resourcesTag = new CompoundTag();
        state.getResourceCounts().forEach((type, count) -> {
            resourcesTag.putDouble(type.name(), count);
        });
        nbt.put("resources", resourcesTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        IPlayerMinerState state = getOrCreateMinerState();
        // 调用不会触发网络同步的NBT专用方法
        state.setMinerModeFromNBT(nbt.getBoolean("isMinerMode"));

        // 创建一个新的Map来存储反序列化的数据，然后调用setResourceCounts
        Map<ResourceType, Double> loadedResources = new EnumMap<>(ResourceType.class);
        CompoundTag resourcesTag = nbt.getCompound("resources");
        for (String key : resourcesTag.getAllKeys()) {
            try {
                ResourceType type = ResourceType.valueOf(key);
                double count = resourcesTag.getDouble(key);
                loadedResources.put(type, count);
            } catch (IllegalArgumentException e) {

            }
        }
        // 使用setResourceCounts来正确地更新内部状态
        state.setResourceCounts(loadedResources);
    }
}
