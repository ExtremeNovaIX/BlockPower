package BlockPower.ModException;

import BlockPower.Skills.SkillLock.SkillLockManager;

import static BlockPower.Skills.SkillLock.SkillLockManager.LOCK_MAX_TICK;

/**
 * 与 SkillLockManager 相关的操作中发生的特定异常。
 * 当 SkillLockManager 的API被不正确地调用时，应抛出此异常，
 * 例如：为已经上锁的玩家技能重复加锁，或传入不符合逻辑的参数。
 * 继承自 RuntimeException，表示这是一个非检查性异常，
 * 通常由编程错误引起，调用者应修复其调用代码，而不是捕获此异常。
 */
public class SkillLockException extends RuntimeException {

    /**
     * 超时异常，如果超过20秒没解锁，则认为技能意外锁死。
     * 此时抛出异常提醒开发者修复代码，避免技能锁死。
     */
    public SkillLockException(String lockId, SkillLockManager.LockData lockData,long currentTick) {
        super("Skill Lock out of time(" + LOCK_MAX_TICK + "ticks). " +
                "LockId: " + lockId + ", Priority: " + lockData.priority + ", " +
                "Duration: " + lockData.duration + ", " +
                "StartTick: " + lockData.startTick + ", " +
                "CurrentTick: " + currentTick
        );
    }
}
