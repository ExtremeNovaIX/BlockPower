package BlockPower.Skills;

import BlockPower.ModMessages.SkillC2SPacket.BarrierWallSkillPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;

public class BarrierWallSkill implements Skill {
    @Override
    public String getSkillName() {
        return "BarrierWall";
    }

    @Override
    public String getSkillDescription() {
        return "在玩家前方生成一堵墙";
    }

    @Override
    public int getSkillLevel() {
        return 1;
    }

    @Override
    public void triggerSkill(int skillLevel) {
        ModMessages.sendToServer(new BarrierWallSkillPacket_C2S());
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.STONE;
    }

    @Override
    public double getSkillCostAmount() {
        return 5;
    }
}