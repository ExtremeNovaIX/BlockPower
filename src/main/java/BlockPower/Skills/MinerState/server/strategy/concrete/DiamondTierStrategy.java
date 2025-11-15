package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class DiamondTierStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();

    @Override
    public ResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.20) { // 20% 概率获取DIRT
            return ResourceType.DIRT;
        } else if (chance < 0.35) { // 15% 概率获取WOOD
            return ResourceType.WOOD;
        } else if (chance < 0.54) { // 19% 概率获取STONE
            return ResourceType.STONE;
        } else if (chance < 0.85) { // 31% 概率获取IRON
            return ResourceType.IRON;
        } else if (chance < 0.96) { // 11% 概率获取GOLD
            return ResourceType.GOLD;
        } else if (chance < 0.99) { // 3% 概率获取DIAMOND
            return ResourceType.DIAMOND;
        } else {
            return ResourceType.NETHERITE;// 1% 概率获取NETHERITE
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 3;
    }
}
