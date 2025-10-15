package BlockPower.Skills.NormalSkills;

import BlockPower.Skills.MinerState.server.AllResourceType;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ISkill {
    Logger log = LoggerFactory.getLogger(ISkill.class);

    // 技能名称
    String getSkillName();

    // 技能描述
    String getSkillDescription();

    // 技能等级
    int getSkillLevel();

    // 技能使用
    void triggerSkill(ServerPlayer player);

    // 获取技能消耗 - 返回资源类型
    AllResourceType getSkillCostType();

    // 获取技能消耗数量
    double getSkillCostAmount();

    // 是否消耗资源
    boolean isSkillConsumeResource();

    // 是否自动锁定技能（不会自动解锁，必须要在编写技能处理方法时手动解锁）
    boolean isSkillAutoLocked();
}
