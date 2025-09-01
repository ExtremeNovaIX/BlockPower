package BlockPower.Skills;

import BlockPower.Skills.MinerState.server.AllResourceType;
import net.minecraft.world.entity.player.Player;

public interface Skill {
    // 技能名称
    String getSkillName();

    // 技能描述
    String getSkillDescription();

    // 技能等级
    int getSkillLevel();

    // 技能使用
    void triggerSkill(int skillLevel);

    // 获取技能消耗 - 返回资源类型
    AllResourceType getSkillCostType();

    // 获取技能消耗数量
    double getSkillCostAmount();
}
