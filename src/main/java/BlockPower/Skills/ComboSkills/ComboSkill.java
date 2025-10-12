package BlockPower.Skills.ComboSkills;

import net.minecraft.world.entity.player.Player;

public interface ComboSkill {
    // 技能名称
    String getSkillName();

    // 技能描述
    String getSkillDescription();

    // 技能等级
    int getSkillLevel();

    // 技能使用
    void triggerSkill(Player player);

    // 触发条件
    boolean canTriggerSkill(int comboCount);

    // 连携技冷却时间（单位：tick）
    int getCooldownTick();

    // 连携技窗口时间（单位：tick）
    int getComboWindowTick();
}
