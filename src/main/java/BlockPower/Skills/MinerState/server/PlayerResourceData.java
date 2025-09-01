package BlockPower.Skills.MinerState.server;

import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.ResourceSyncPacket_S2C;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.Map;

/**
 * 存储并管理单个玩家的资源数据。
 * 采用每种资源拥有独立上限的模式，不再有压缩逻辑。
 */
public class PlayerResourceData {
    // 用于存储每种资源的真实数量。
    public final Map<AllResourceType, Double> trueResourceCounts = new EnumMap<>(AllResourceType.class);

    // 每次获取资源时增加的量。
    private static final double RESOURCE_GAIN_AMOUNT = 1.0;

    /**
     * 构造函数，初始化所有资源的真实数量为0。
     */
    public PlayerResourceData() {
        for (AllResourceType type : AllResourceType.values()) {
            trueResourceCounts.put(type, 0.0);
        }
    }

    /**
     * 为指定的资源类型增加数量，会检查该资源自身的上限。
     *
     * @param type 要增加的资源类型。
     */
    public void addResource(AllResourceType type) {
        // 获取当前数量和该资源类型的独立上限
        double currentAmount = trueResourceCounts.getOrDefault(type, 0.0);
        double maxAmount = type.getMaxAmount(); // 从枚举中获取上限

        // 如果当前数量已达到或超过上限，则直接返回，不添加任何资源
        if (currentAmount >= maxAmount) {
            return;
        }

        // 计算实际能增加的数量，确保不会超过上限
        double amountToAdd = Math.min(RESOURCE_GAIN_AMOUNT, maxAmount - currentAmount);

        // 更新对应资源的真实数量
        trueResourceCounts.compute(type, (k, v) -> (v == null ? 0 : v) + amountToAdd);
    }

    /**
     * 直接返回当前所有资源的真实数量。
     * UI层可以直接使用这些数据进行显示和计算。
     *
     * @return 一个包含所有资源类型及其真实数量的Map。
     */
    public Map<AllResourceType, Double> getResourceCounts() {
        return new EnumMap<>(trueResourceCounts);
    }

    /**
     * @return 资源条每种资源的上限数量总和
     */
    public static double getTotalMaxAmount() {
        double totalMax = 0;
        for (AllResourceType type : AllResourceType.getNormalResourceType()) {
            totalMax += type.getMaxAmount();
        }
        return totalMax;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        trueResourceCounts.forEach((type, count) -> {
            sb.append(type).append(":").append(count).append(",");
        });
        return sb.toString();
    }

    /**
     * 检查指定资源类型的数量是否足够
     *
     * @param type   资源类型
     * @param amount 需要检查的数量
     * @return 是否足够
     */
    public boolean hasEnoughResource(AllResourceType type, double amount) {
        Double currentAmount = trueResourceCounts.get(type);
        return currentAmount != null && currentAmount >= amount;
    }

    /**
     * 消耗指定数量的资源
     *
     * @param type   资源类型
     * @param amount 要消耗的数量
     * @return 是否成功消耗（如果资源不足则返回false）
     */
    public boolean consumeResource(AllResourceType type, double amount, ServerPlayer player) {
        if (!hasEnoughResource(type, amount)) {
            return false;
        }
        trueResourceCounts.compute(type, (k, v) -> v - amount);
        ModMessages.sendToPlayer(new ResourceSyncPacket_S2C(trueResourceCounts), player);
        return true;
    }
}