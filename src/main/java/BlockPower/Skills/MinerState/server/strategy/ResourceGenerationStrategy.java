package BlockPower.Skills.MinerState.server.strategy;

import BlockPower.Skills.MinerState.server.AllResourceType;

public interface ResourceGenerationStrategy {
    AllResourceType generateResource();

    Integer getDigCoolDown();
}
