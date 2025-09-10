package BlockPower.Skills;

import BlockPower.ModMessages.SkillC2SPacket.LauncherSwingSkillPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;

public class LauncherSwingSkill implements Skill {
    @Override
    public String getSkillName() {
        return "LauncherSwing";
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
        ModMessages.sendToServer(new LauncherSwingSkillPacket_C2S());
    }

    @Override
    public AllResourceType getSkillCostType() {
        return null;
    }

    @Override
    public double getSkillCostAmount() {
        return 0;
    }
}
