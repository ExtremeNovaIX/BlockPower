package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class MaxTierStrategy implements ResourceGenerationStrategy {
    private final Random r = new Random();

    @Override
    public ResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.30) { // 30% 概率获取DIRT
            return ResourceType.DIRT;
        } else if (chance < 0.35) { // 5% 概率获取WOOD
            return ResourceType.WOOD;
        } else if (chance < 0.45) { // 15% 概率获取STONE
            return ResourceType.STONE;
        } else if (chance < 0.95) { // 50% 概率获取IRON
            return ResourceType.IRON;
        } else if (chance < 0.985) { // 3.5% 概率获取GOLD
            return ResourceType.GOLD;
        } else if (chance < 0.995) { // 1% 概率获取DIAMOND
            return ResourceType.DIAMOND;
        } else {
            return ResourceType.NETHERITE;// 0.5% 概率获取NETHERITE
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 3;
    }
}
