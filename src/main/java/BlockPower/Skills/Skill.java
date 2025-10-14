package BlockPower.Skills;

import BlockPower.Skills.MinerState.server.AllResourceType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface Skill {
    // 技能名称
    String getSkillName();

    // 技能描述
    String getSkillDescription(int skillLevel);

    // 技能等级
    int getSkillLevel();

    // 当前技能是否可以使用(判断冷却，异常效果，资源消耗等等)
    boolean canUse(Player player, @Nullable Entity target);

    // 技能使用
    void triggerSkill(int skillLevel);

    // 获取技能消耗 - 返回资源类型
    AllResourceType getSkillCostType();

    // 获取技能消耗数量
    double getSkillCostAmount();

}
