package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class DefaultStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();

    @Override
    public ResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.50) { // 50% 概率获取DIRT
            return ResourceType.DIRT;
        } else if (chance < 0.9) { // 40% 概率获取WOOD
            return ResourceType.WOOD;
        } else { // 10% 概率获取STONE
            return ResourceType.STONE;
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 9;
    }

}
