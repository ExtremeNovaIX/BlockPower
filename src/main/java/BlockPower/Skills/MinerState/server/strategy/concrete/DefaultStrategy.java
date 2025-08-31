package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class DefaultStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();

    @Override
    public AllResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.50) { // 50% 概率获取DIRT
            return AllResourceType.DIRT;
        } else if (chance < 0.9) { // 40% 概率获取WOOD
            return AllResourceType.WOOD;
        } else { // 10% 概率获取STONE
            return AllResourceType.STONE;
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 10;
    }

}
