package BlockPower.ModException;

import BlockPower.Skills.NormalSkills.ISkill;

public class SkillException extends RuntimeException {
    public SkillException(String message) {
        super(message);
    }

    /**
     * 技能执行失败异常
     * @param skill 尝试执行的技能
     * @param cause 捕获到的原始异常 (e.g., NullPointerException)
     */
    public SkillException(ISkill skill, Throwable cause) {
        super("Skill " + skill.getSkillName() + " encountered a destructible error during execution.", cause);
    }

}