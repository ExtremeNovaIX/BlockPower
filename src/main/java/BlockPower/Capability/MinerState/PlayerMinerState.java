package BlockPower.Capability.MinerState;

import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.ResourceSyncPacket_S2C;
import BlockPower.Skills.MinerState.server.PlayerResourceData;
import BlockPower.Skills.MinerState.server.ResourceType;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * IPlayerMinerState 接口的默认实现。
 */
public class PlayerMinerState implements IPlayerMinerState {
    private boolean isMinerMode = false;
    private final PlayerResourceData resourceData = new PlayerResourceData();

    @Override
    public boolean isMinerMode() {
        return this.isMinerMode;
    }

    @Override
    public void setMinerMode(boolean enabled, ServerPlayer player) {
        if (this.isMinerMode != enabled) {
            this.isMinerMode = enabled;
            syncToClient(player);
        }
    }

    @Override
    public void setMinerModeFromNBT(boolean enabled) {
        this.isMinerMode = enabled;
    }

    @Override
    public void addResource(ResourceType type, ServerPlayer player) {
        this.resourceData.addResource(type);
        syncToClient(player);
    }

    @Override
    public boolean consumeResource(ResourceType type, double amount, ServerPlayer player) {
        boolean success = this.resourceData.consumeResource(type, amount, player);
        if (success) {
            syncToClient(player);
        }
        return success;
    }

    @Override
    public boolean hasEnoughResource(ResourceType type, double amount) {
        return this.resourceData.hasEnoughResource(type, amount);
    }

    @Override
    public Map<ResourceType, Double> getResourceCounts() {
        return this.resourceData.getResourceCounts();
    }

    @Override
    public void setResourceCounts(Map<ResourceType, Double> resourceCounts) {
        this.resourceData.trueResourceCounts.clear();
        this.resourceData.trueResourceCounts.putAll(resourceCounts);
    }

    /**
     * 将当前完整的MinerState数据同步到客户端。
     * @param player 目标玩家。
     */
    @Override
    public void syncToClient(ServerPlayer player) {
        ModMessages.sendToPlayer(new ResourceSyncPacket_S2C(this.isMinerMode, this.getResourceCounts()), player);
    }
}
