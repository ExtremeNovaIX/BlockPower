package BlockPower.Skills.MinerState.server;

import java.util.EnumMap;
import java.util.Map;
//TODO 根据不同镐子获取资源效率产生区分

/**
 * 存储并管理单个玩家的资源数据。
 * 使用一个独立的Map来管理【真实资源】，确保其数值不会因UI压缩而减少。
 * 提供给UI的方法会根据真实资源量动态计算出【视觉资源】。
 */
public class PlayerResourceData {
    // 用于存储每种资源的【真实】数量。
    private final Map<ResourceType, Double> trueResourceCounts = new EnumMap<>(ResourceType.class);

    // 临界值1：开始挤压。当【真实】总量超过此值，视觉资源将被等比压缩。
    private static final double UI_COMPRESSION_START = 600.0;
    // 临界值2：存满。当【真实】总量达到此值，将不再接受任何新资源。
    private static final double MAX_CAPACITY = 800.0;
    // 每次获取资源时增加的量。
    private static final double RESOURCE_GAIN_AMOUNT = 5.0;

    /**
     * 构造函数，初始化所有资源的真实数量为0。
     */
    public PlayerResourceData() {
        for (ResourceType type : ResourceType.values()) {
            trueResourceCounts.put(type, 0.0);
        }
    }

    /**
     * 为指定的资源类型增加【真实】数量。
     * 此方法只修改真实数据，与视觉表现无关。
     *
     * @param type 要增加的资源类型。
     */
    public void addResource(ResourceType type) {
        double currentTrueTotal = this.getTrueTotal();

        // 检查真实资源总量是否已满
        if (currentTrueTotal >= MAX_CAPACITY) {
            return;
        }

        // 计算本次实际能增加的资源量，确保真实总量不会超过上限。
        double amountToAdd = Math.min(RESOURCE_GAIN_AMOUNT, MAX_CAPACITY - currentTrueTotal);

        // 更新对应资源的真实数量。这里的数值只会增加，不会减少。
        trueResourceCounts.compute(type, (k, v) -> (v == null ? 0 : v) + amountToAdd);
    }

    /**
     * 计算并获取当前真实的资源总量。
     *
     * @return 真实的资源总量。
     */
    private double getTrueTotal() {
        return trueResourceCounts.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * 动态计算并获取供GUI渲染的【视觉】资源映射表。
     * 如果真实总量未达到压缩点，视觉量 = 真实量。
     * 如果真实总量超过压缩点，所有视觉量将根据真实占比进行等比压缩，使视觉总和等于压缩点。
     *
     * @return 一个包含所有资源类型及其【视觉】数量的Map，可直接用于渲染，无需修改。
     */
    public Map<ResourceType, Double> getResourceCounts() {
        Map<ResourceType, Double> visualCounts = new EnumMap<>(ResourceType.class);
        double trueTotal = getTrueTotal();

        // 未达到压缩临界值，视觉数量等于真实数量，直接返回。
        if (trueTotal <= UI_COMPRESSION_START) {
            visualCounts.putAll(trueResourceCounts);
            return visualCounts;
        }
        //进入压缩逻辑
        double specialAmount = getSpecialResourceAmount();

        //处理特殊资源（金和铁）已经占满或超出压缩条的情况
        if (specialAmount >= UI_COMPRESSION_START) {
            // 此时，只显示金和铁，其他资源的视觉大小为0
            for (Map.Entry<ResourceType, Double> entry : trueResourceCounts.entrySet()) {
                if (entry.getKey() == ResourceType.GOLD || entry.getKey() == ResourceType.IRON) {
                    visualCounts.put(entry.getKey(), entry.getValue());
                } else {
                    visualCounts.put(entry.getKey(), 0.0);
                }
            }
            return visualCounts;
        }

        // 计算需要被压缩的资源的总量
        double compressibleTotal = trueTotal - specialAmount;

        // 处理没有可压缩资源的情况，防止除以零
        if (compressibleTotal <= 0) {
            // 此时说明只有金和铁，直接返回即可
            visualCounts.putAll(trueResourceCounts);
            return visualCounts;
        }

        // 计算剩余的视觉空间和压缩比例
        double remainingVisualSpace = UI_COMPRESSION_START - specialAmount;
        double scalingFactor = remainingVisualSpace / compressibleTotal;

        // 应用缩放
        for (Map.Entry<ResourceType, Double> entry : trueResourceCounts.entrySet()) {
            ResourceType type = entry.getKey();
            double value = entry.getValue();
            if (type == ResourceType.GOLD || type == ResourceType.IRON) {
                visualCounts.put(type, value); // 特殊资源使用原值
            } else {
                visualCounts.put(type, value * scalingFactor); // 普通资源使用缩放后的值
            }
        }

        return visualCounts;
    }

    public double getSpecialResourceAmount() {
        return trueResourceCounts.get(ResourceType.GOLD) + trueResourceCounts.get(ResourceType.IRON);
    }

    /**
     * 获取玩家当前的真实资源总量，可用于在UI上显示数值文本，如 "125/150"。
     *
     * @return 真实的资源总量。
     */
    public double getCurrentTrueTotal() {
        return getTrueTotal();
    }

    /**
     * 获取资源条的真实容量上限。
     *
     * @return 真实容量上限。
     */
    public double getMaxCapacity() {
        return MAX_CAPACITY;
    }

    public static double getCompressionThreshold() {
        return UI_COMPRESSION_START;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        trueResourceCounts.forEach((type, count) -> {
            sb.append(type).append(":").append(count).append(",");
        });
        return sb.toString();
    }
}