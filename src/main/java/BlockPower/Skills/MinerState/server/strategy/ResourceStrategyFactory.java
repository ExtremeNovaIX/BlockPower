package BlockPower.Skills.MinerState.server.strategy;

import BlockPower.Skills.MinerState.server.strategy.concrete.*;

public class ResourceStrategyFactory {
    public static ResourceGenerationStrategy getStrategy(int level) {
        return switch (level) {
            case 1 -> new WoodTierStrategy();    // 1级对应木头
            case 2 -> new StoneTierStrategy();   // 2级对应石头
            case 3 -> new GoldTierStrategy();    // 3级对应金
            case 4 -> new IronTierStrategy();    // 4级对应铁
            case 5 -> new DiamondTierStrategy(); // 5级对应钻石
            case 6 -> new MaxTierStrategy();     // 6级对应下界合金
            default -> new DefaultStrategy();   // 默认策略
        };
    }
}
