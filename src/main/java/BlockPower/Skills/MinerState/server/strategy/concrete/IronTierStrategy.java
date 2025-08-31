package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class IronTierStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();

    @Override
    public AllResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.20) { // 20% 概率获取DIRT
            return AllResourceType.DIRT;
        } else if (chance < 0.40) { // 20% 概率获取WOOD
            return AllResourceType.WOOD;
        } else if (chance < 0.59) { // 19% 概率获取STONE
            return AllResourceType.STONE;
        } else if (chance < 0.90) { // 31% 概率获取IRON
            return AllResourceType.IRON;
        } else if (chance < 0.99) { // 9% 概率获取GOLD
            return AllResourceType.GOLD;
        } else {
            return AllResourceType.DIAMOND;// 1% 概率获取DIAMOND
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 5;
    }
}
