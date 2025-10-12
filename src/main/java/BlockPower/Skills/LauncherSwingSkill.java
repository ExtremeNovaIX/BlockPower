package BlockPower.Skills;

import BlockPower.ModMessages.SkillC2SPacket.LauncherSwingSkillPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;
//TODO 修改成一段时间内combo上限6次，并且后几次击退明显增大，防止无限连
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
