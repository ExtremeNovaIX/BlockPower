package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class WoodTierStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();

    @Override
    public AllResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.40) { // 40% 概率获取DIRT
            return AllResourceType.DIRT;
        } else if (chance < 0.75) { // 35% 概率获取WOOD
            return AllResourceType.WOOD;
        } else { // 25% 概率获取STONE
            return AllResourceType.STONE;
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 8;
    }
}
