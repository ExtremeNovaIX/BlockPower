package BlockPower.Skills.SkillTrigger;

import BlockPower.Skills.MinerState.client.ClientMinerState;
import BlockPower.Skills.Skill;
import BlockPower.Util.Commons;

public class SkillTrigger {

    public static void triggerSkill(Skill skill) {
        if (!ClientMinerState.isMinerMode()) return;// 非挖掘状态下不能触发技能

        /**
         * 客户端先检查资源是否足够，未启用
         * 目前在服务端检查
         */
//        AllResourceType skillCostType = skill.getSkillCostType();
//        Double currentAmount = ClientResourceData.getResources().get(skillCostType);
//        if (currentAmount == null) return;
//        if (currentAmount < skill.getSkillCostAmount()) return;// 资源不足，不能触发技能

        skill.triggerSkill(skill.getSkillLevel());
    }
}
