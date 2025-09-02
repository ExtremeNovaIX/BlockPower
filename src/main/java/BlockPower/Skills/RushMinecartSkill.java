package BlockPower.Skills;

import BlockPower.ModMessages.C2SPacket.SkillPacket.SpawnRushMinecartPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;

public class RushMinecartSkill implements Skill {
    @Override
    public String getSkillName() {
        return "RushMinecart";
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
        ModMessages.sendToServer(new SpawnRushMinecartPacket_C2S());
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.IRON;
    }

    @Override
    public double getSkillCostAmount() {
        return 2;
    }
}
