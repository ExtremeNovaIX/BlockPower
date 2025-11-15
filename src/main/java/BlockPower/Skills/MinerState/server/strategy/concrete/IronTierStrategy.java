package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class IronTierStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();

    @Override
    public ResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.20) { // 20% 概率获取DIRT
            return ResourceType.DIRT;
        } else if (chance < 0.40) { // 20% 概率获取WOOD
            return ResourceType.WOOD;
        } else if (chance < 0.59) { // 19% 概率获取STONE
            return ResourceType.STONE;
        } else if (chance < 0.90) { // 31% 概率获取IRON
            return ResourceType.IRON;
        } else if (chance < 0.99) { // 9% 概率获取GOLD
            return ResourceType.GOLD;
        } else {
            return ResourceType.DIAMOND;// 1% 概率获取DIAMOND
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 5;
    }
}
