package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class MaxTierStrategy implements ResourceGenerationStrategy {
    private final Random r = new Random();

    @Override
    public AllResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.20) { // 20% 概率获取DIRT
            return AllResourceType.DIRT;
        } else if (chance < 0.35) { // 15% 概率获取WOOD
            return AllResourceType.WOOD;
        } else if (chance < 0.54) { // 19% 概率获取STONE
            return AllResourceType.STONE;
        } else if (chance < 0.85) { // 31% 概率获取IRON
            return AllResourceType.IRON;
        } else if (chance < 0.95) { // 10% 概率获取GOLD
            return AllResourceType.GOLD;
        } else if (chance < 0.99) { // 4% 概率获取DIAMOND
            return AllResourceType.DIAMOND;
        } else {
            return AllResourceType.NETHERITE;// 1% 概率获取NETHERITE
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 3;
    }
}
