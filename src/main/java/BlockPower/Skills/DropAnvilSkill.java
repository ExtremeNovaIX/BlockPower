package BlockPower.Skills;

import BlockPower.ModMessages.C2SPacket.SkillPacket.SpawnDropAnvilPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;

public class DropAnvilSkill implements Skill {
    @Override
    public String getSkillName() {
        return "DropAnvil";
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
        ModMessages.sendToServer(new SpawnDropAnvilPacket_C2S());
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.IRON;
    }

    @Override
    public double getSkillCostAmount() {
        return 10;
    }
}
