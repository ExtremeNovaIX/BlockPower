package BlockPower.Capability;

import BlockPower.Skills.MinerState.server.ResourceType;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * 该接口定义了玩家挖掘状态（MinerState）的能力。
 * 它用于存储和管理玩家是否处于挖掘模式，以及相关的资源数据。
 */
public interface IPlayerMinerState {

    /**
     * 检查玩家是否处于挖掘模式。
     * @return 如果是，则返回true。
     */
    boolean isMinerMode();

    /**
     * 设置玩家的挖掘模式状态，并同步到客户端。
     * @param enabled 新的状态。
     * @param player 玩家实体，用于同步。
     */
    void setMinerMode(boolean enabled, ServerPlayer player);

    /**
     * 仅用于从NBT加载时设置挖掘模式状态，不会触发网络同步。
     * @param enabled 新的状态。
     */
    void setMinerModeFromNBT(boolean enabled);

    /**
     * 为指定的资源类型增加数量，并同步到客户端。
     * @param type 要增加的资源类型。
     * @param player 玩家实体，用于同步。
     */
    void addResource(ResourceType type, ServerPlayer player);

    /**
     * 消耗指定数量的资源。
     * @param type   资源类型。
     * @param amount 要消耗的数量。
     * @param player 玩家实体，用于同步数据。
     * @return 是否成功消耗。
     */
    boolean consumeResource(ResourceType type, double amount, ServerPlayer player);

    /**
     * 检查指定资源类型的数量是否足够。
     * @param type   资源类型。
     * @param amount 需要检查的数量。
     * @return 是否足够。
     */
    boolean hasEnoughResource(ResourceType type, double amount);

    /**
     * 获取当前所有资源的真实数量。
     * @return 一个包含所有资源类型及其真实数量的Map。
     */
    Map<ResourceType, Double> getResourceCounts();

    /**
     * 直接设置所有资源的数量（通常用于NBT反序列化）。
     * @param resourceCounts 包含新资源数量的Map。
     */
    void setResourceCounts(Map<ResourceType, Double> resourceCounts);

    /**
     * 将当前完整的MinerState数据同步到客户端。
     * @param player 目标玩家。
     */
    void syncToClient(ServerPlayer player);
}
