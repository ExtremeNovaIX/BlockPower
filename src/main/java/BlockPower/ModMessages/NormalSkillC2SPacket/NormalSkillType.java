package BlockPower.ModMessages.NormalSkillC2SPacket;

import BlockPower.Skills.NormalSkills.*;

// 普通技能类型的枚举
public enum NormalSkillType {
    AIR_JUMP(AirJumpSkill.class),
    DASH(DashSkill.class),
    MINECART_RUSH(RushMinecartSkill.class),
    DROP_ANVIL(DropAnvilSkill.class),
    PLACE_BLOCK(PlaceBlockSkill.class),
    LAUNCHER_SWING(LauncherSwingSkill.class);

    public final Class<? extends ISkill> skillClass;

    NormalSkillType(Class<? extends ISkill> skillClass) {
        this.skillClass = skillClass;
    }
}
