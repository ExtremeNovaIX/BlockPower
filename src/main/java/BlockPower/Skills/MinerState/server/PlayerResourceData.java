package BlockPower.Skills.MinerState.server;

import BlockPower.Util.Commons;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
//TODO 根据不同镐子获取资源效率产生区分

/**
 * 存储并管理单个玩家的资源数据。
 * 使用一个独立的Map来管理【真实资源】，确保其数值不会因UI压缩而减少。
 * 提供给UI的方法会根据真实资源量动态计算出【视觉资源】。
 */
public class PlayerResourceData {
    // 用于存储每种资源的【真实】数量。
    private final Map<AllResourceType, Double> trueResourceCounts = new EnumMap<>(AllResourceType.class);

    // 临界值1：开始挤压。当【真实】总量超过此值，视觉资源将被等比压缩。
    private static final double UI_COMPRESSION_START = 100.0;
    // 临界值2：存满。当【真实】总量达到此值，将不再接受任何新资源。
    private static final double MAX_CAPACITY = 200.0;
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
     * 为指定的资源类型增加【真实】数量。
     * 此方法只修改trueResourceCounts的数据，与视觉表现无关。
     *
     * @param type 要增加的资源类型。
     */
    public void addResource(AllResourceType type) {
        double amountToAdd;
        if (AllResourceType.getNormalResourceType().contains(type)) {//普通资源添加逻辑
            double currentTrueTotal = this.getNormalResourceTotal();
            // 检查真实资源总量是否已满

            if (currentTrueTotal >= MAX_CAPACITY) return;

            // 计算本次实际能增加的资源量，确保真实总量不会超过上限。
            amountToAdd = Math.min(RESOURCE_GAIN_AMOUNT, MAX_CAPACITY - currentTrueTotal);

        } else {//特殊资源添加逻辑
            Double currentAmount = trueResourceCounts.getOrDefault(type, 0.0);
            if (currentAmount >= 64) return;//返回数据无效或者大于64时，不执行添加逻辑
            amountToAdd = 1.0;
        }

        // 更新对应资源的真实数量。这里的数值只会增加，不会减少。
        double finalAmountToAdd = amountToAdd;
        trueResourceCounts.compute(type, (k, v) -> (v == null ? 0 : v) + finalAmountToAdd);
    }

    /**
     * 动态计算并获取供GUI渲染的【视觉】资源映射表。
     * 如果真实总量未达到压缩点，视觉量 = 真实量。
     * 如果真实总量超过压缩点，视觉量将进行压缩
     *
     * @return 一个包含所有资源类型及其【视觉】数量的Map，可直接用于渲染，无需修改。
     */
    public Map<AllResourceType, Double> getResourceCounts() {
        Map<AllResourceType, Double> visualCounts = new EnumMap<>(AllResourceType.class);
        double trueTotal = getNormalResourceTotal();

        // 未达到压缩临界值，视觉数量等于真实数量，直接返回。
        if (trueTotal <= UI_COMPRESSION_START) {
            visualCounts.putAll(trueResourceCounts);
            return visualCounts;
        }

        //进入压缩逻辑
        double IronAmount = trueResourceCounts.get(AllResourceType.IRON);
        //处理铁已经占满或超出压缩条的情况
        if (IronAmount >= UI_COMPRESSION_START) {
            // 此时，资源条内只显示铁，其他资源的视觉大小为0
            for (Map.Entry<AllResourceType, Double> entry : trueResourceCounts.entrySet()) {
                if (AllResourceType.getNoCompressionResourceType().contains(entry.getKey())) {
                    visualCounts.put(entry.getKey(), entry.getValue());
                } else {
                    visualCounts.put(entry.getKey(), 0.0);
                }
            }
            return visualCounts;
        }

        // 计算需要被压缩的资源的总量
        double compressibleTotal = trueTotal - IronAmount;

        // 处理没有可压缩资源的情况，防止除以零
        if (compressibleTotal <= 0) {
            // 此时说明只有铁，直接返回
            visualCounts.putAll(trueResourceCounts);
            return visualCounts;
        }

        // 计算剩余的视觉空间和压缩比例
        double remainingVisualSpace = UI_COMPRESSION_START - IronAmount;
        double scalingFactor = remainingVisualSpace / compressibleTotal;

        // 应用缩放
        for (Map.Entry<AllResourceType, Double> entry : trueResourceCounts.entrySet()) {
            AllResourceType type = entry.getKey();
            double value = entry.getValue();
            if (AllResourceType.getNoCompressionResourceType().contains(type)) {
                visualCounts.put(type, value); // 特殊资源使用原值
            } else {
                visualCounts.put(type, value * scalingFactor); // 普通资源使用缩放后的值
            }
        }
        return visualCounts;
    }

    private double getNormalResourceTotal() {
        return trueResourceCounts.entrySet().stream()
                .filter(entry -> AllResourceType.getNormalResourceType().contains(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
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