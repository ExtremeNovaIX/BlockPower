package BlockPower.Skills.NormalSkills;

import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public interface ISkill {
    Logger log = LoggerFactory.getLogger(ISkill.class);
    // 技能名称
    @NotNull String getSkillName();

    // 技能描述
    String getSkillDescription();

    // 技能等级
    int getSkillLevel();

    /**
     * 触发技能执行
     *
     * @param player 执行技能的玩家
     * @return 是否成功执行技能
     */
    SkillExecutionResult triggerSkill(ServerPlayer player);

    // 获取技能消耗 - 返回资源类型
    AllResourceType getSkillCostType();

    // 获取技能消耗数量
    double getSkillCostAmount();

    // 是否消耗资源
    boolean isSkillConsumeResource();
}
