package BlockPower.ModException;

import BlockPower.Skills.NormalSkills.ISkill;

/**
 * 一个“安静”的异常，用于中断技能执行流程（如阻止资源消耗）。
 * 它不会填充堆栈跟踪，因此在日志中是“安静”的，不会产生垃圾信息。
 * 这专门用于逻辑失败（例如“方块被阻挡”），而不是真正的程序错误。
 */
public class SilentSkillException extends RuntimeException {

  public SilentSkillException(ISkill skill, String message) {
    super("Skill " + skill.getSkillName() + " logical fail: " + message);
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}