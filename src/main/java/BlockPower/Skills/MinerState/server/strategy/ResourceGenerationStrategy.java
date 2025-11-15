package BlockPower.Skills.MinerState.server.strategy;

import BlockPower.Skills.MinerState.server.ResourceType;

public interface ResourceGenerationStrategy {
    ResourceType generateResource();

    Integer getDigCoolDown();
}
