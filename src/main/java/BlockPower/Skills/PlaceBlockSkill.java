package BlockPower.Skills;

import BlockPower.ModMessages.C2SPacket.SkillPacket.PlaceBlockSkillPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;

public class PlaceBlockSkill implements Skill {
    @Override
    public String getSkillName() {
        return "PlaceBlock";
    }

    @Override
    public String getSkillDescription() {
        return "";
    }

    @Override
    public int getSkillLevel() {
        return 0;
    }

    @Override
    public void triggerSkill(int skillLevel) {
        ModMessages.sendToServer(new PlaceBlockSkillPacket_C2S());
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.DIRT;
    }

    @Override
    public double getSkillCostAmount() {
        return 1;
    }
}
