package BlockPower.Skills.MinerState.server.strategy.concrete;

import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.strategy.ResourceGenerationStrategy;

import java.util.Random;

public class GoldTierStrategy implements ResourceGenerationStrategy {
    private static final Random r = new Random();
    @Override
    public AllResourceType generateResource() {
        double chance = r.nextDouble();
        if (chance < 0.35) { // 35% 概率获取DIRT
            return AllResourceType.DIRT;
        } else if (chance < 0.65) { // 30% 概率获取WOOD
            return AllResourceType.WOOD;
        } else if (chance < 0.90) { // 25% 概率获取STONE
            return AllResourceType.STONE;
        } else if (chance < 0.97) { // 7% 概率获取GOLD
            return AllResourceType.GOLD;
        } else { // 3% 概率获取IRON
            return AllResourceType.IRON;
        }
    }

    @Override
    public Integer getDigCoolDown() {
        return 2;
    }
}
