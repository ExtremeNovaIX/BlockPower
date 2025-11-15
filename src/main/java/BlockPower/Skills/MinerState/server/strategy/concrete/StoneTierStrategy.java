package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class StoneTierStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();

    @Override
    public ResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.20) { // 20% 概率获取DIRT
            return ResourceType.DIRT;
        } else if (chance < 0.40) { // 20% 概率获取WOOD
            return ResourceType.WOOD;
        } else if (chance < 0.80) { // 40% 概率获取STONE
            return ResourceType.STONE;
        } else {
            return ResourceType.IRON;// 20% 概率获取IRON
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 7;
    }
}
