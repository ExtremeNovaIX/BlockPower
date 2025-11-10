package BlockPower.Util.ModEffects.ServerEffect;

import BlockPower.Util.ModEffects.ITickBasedEffect;
import lombok.Builder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

// 注意：这里不再需要类级别的 @Builder 注解

public class SpringAttractionEffect implements ITickBasedEffect {

    private final Player player;
    private final Entity targetEntity;
    private final double optimalDistance;
    private final double springConstant;
    private final double maxRangeSq;
    private final double deadZone;
    private final int duration;

    private int currentTick = 0;

    /**
     * 兼容旧代码的公共构造函数，使用默认预设值。
     * @param player 施加引力的玩家。
     * @param targetEntity 被吸引的目标实体。
     */
    public SpringAttractionEffect(Player player, Entity targetEntity) {
        this.player = player;
        this.targetEntity = targetEntity;
        // 使用默认值初始化 final 字段
        this.optimalDistance = 1.0;
        this.springConstant = 0.25;
        this.maxRangeSq = 5.0 * 5.0;
        this.deadZone = 0.5;
        this.duration = 8;
    }

    @Builder
    private SpringAttractionEffect(Player player, Entity targetEntity, double optimalDistance, double springConstant, double maxRange, double deadZone, int duration) {
        this.player = player;
        this.targetEntity = targetEntity;
        this.optimalDistance = optimalDistance;
        this.springConstant = springConstant;
        this.maxRangeSq = maxRange * maxRange;
        this.deadZone = deadZone;
        this.duration = duration;
    }

    @Override
    public void tick() {
        // 安全检查: 任何一方失效、死亡或不在同一维度，则停止
        if (!player.isAlive() || !targetEntity.isAlive() || player.level() != targetEntity.level()) {
            return;
        }

        double distSqr = player.distanceToSqr(targetEntity);

        // 范围检查: 如果目标超出最大连击范围，则停止
        if (distSqr > maxRangeSq) {
            return;
        }

        double currentDistance = Math.sqrt(distSqr);
        double distanceError = currentDistance - optimalDistance;

        // 应用“死区”，防止在最佳距离附近时发生抖动
        if (Math.abs(distanceError) < deadZone) {
            return;
        }

        // 计算方向和力的大小 (力 = 误差 * 劲度系数)
        Vec3 directionVec = targetEntity.getPosition(0).subtract(player.getPosition(0)).normalize();
        Vec3 forceVector = directionVec.scale(distanceError * springConstant);

        // 应用力：太远则拉近(吸引力)，太近则推开(排斥力)
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement().add(forceVector.scale(0.9))));
        }
        targetEntity.addDeltaMovement(forceVector.scale(-0.15));

        currentTick++;
    }

    @Override
    public boolean isFinished() {
        return currentTick >= duration;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    // 3. 移除所有的 setter 方法，因为所有配置都在创建时通过 Builder 完成。
}