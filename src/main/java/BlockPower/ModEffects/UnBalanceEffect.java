package BlockPower.ModEffects;

import BlockPower.Util.SkillLock.SkillLockManager;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;


public class UnBalanceEffect implements ITickBasedEffect {
    private static final SkillLockManager skillLockManager = SkillLockManager.getInstance();
    private final TickTimer timer;
    // 内部处理器，可以是 Mob 类型也可以是 Player 类型
    private final IEffectHandler handler;

    public UnBalanceEffect(Entity affectedEntity, int tickDuration) {
        timer = new TickTimer(tickDuration, false);
        // 根据实体类型，创建不同的处理器
        if (affectedEntity instanceof Mob mob) {
            this.handler = new MobUnbalanceHandler(mob);
        } else if (affectedEntity instanceof ServerPlayer player) {
            this.handler = new PlayerUnbalanceHandler(player);
        } else {
            this.handler = null;
        }
    }

    @Override
    public void tick() {
        if (handler != null) {
            handler.tick(timer.getTickDuration());
        }
    }

    @Override
    public void onEnd() {
        if (handler != null) {
            handler.onEnd();
        }
    }

    @Override
    public boolean isFinished() {
        return timer.isFinished();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    /**
     * 定义一个处理器接口，用于分离不同实体类型的逻辑
     */
    private interface IEffectHandler {
        void tick(int remainingTicks);
        void onEnd();
    }

    /**
     * 专门处理 Mob 的内部类
     */
    private static class MobUnbalanceHandler implements IEffectHandler {
        private final Mob affectedMob;

        public MobUnbalanceHandler(Mob mob) {
            this.affectedMob = mob;
            // 效果开始时，立即禁用AI
            this.affectedMob.setNoAi(true);
        }

        @Override
        public void tick(int remainingTicks) {
            if (affectedMob.isRemoved()) return;

            //确保 Mob 持续处于 NoAi 状态 (以防其他Mod或原版逻辑意外修改)
            if (!affectedMob.isNoAi()) {
                affectedMob.setNoAi(true);
            }

            //获取当前的运动向量 (这个向量包含了上一tick的击退、速度等信息)
            Vec3 motion = affectedMob.getDeltaMovement();

            //模拟重力
            if (!affectedMob.isNoGravity()) {
                motion = motion.add(0, -0.08, 0);
            }

            //模拟空气阻力/摩擦力
            motion = motion.multiply(0.98, 0.98, 0.98);

            //使用 mob.move() 来应用位移并处理碰撞
            affectedMob.move(MoverType.SELF, motion);

            //更新实体最终的运动向量
            affectedMob.setDeltaMovement(motion);
        }

        @Override
        public void onEnd() {
            // 效果结束时，恢复AI
            if (affectedMob.isAlive()) {
                affectedMob.setNoAi(false);
            }
        }
    }

    /**
     * 专门处理 Player 的内部类
     */
    private static class PlayerUnbalanceHandler implements IEffectHandler {
        private final ServerPlayer affectedPlayer;

        public PlayerUnbalanceHandler(ServerPlayer player) {
            this.affectedPlayer = player;
        }

        @Override
        public void tick(int remainingTicks) {
            if (affectedPlayer.isRemoved()) return;
            // 对玩家应用技能锁
            skillLockManager.overrideableLock(affectedPlayer, remainingTicks);
        }

        @Override
        public void onEnd() {
            //玩家的锁会自动结束
        }
    }
}
