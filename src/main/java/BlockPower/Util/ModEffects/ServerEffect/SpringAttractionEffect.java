package BlockPower.Util.ModEffects.ServerEffect;

import BlockPower.Util.ModEffects.ITickBasedEffect;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SpringAttractionEffect implements ITickBasedEffect {

    // 理想的作战距离（格），弹簧会试图维持这个距离
    private double OPTIMAL_DISTANCE = 1;
    // 弹簧的力度系数 (劲度)。值越大，拉/推的力越强。
    private double SPRING_CONSTANT = 0.25;
    // 弹簧引力的最大有效范围（的平方）
    private double MAX_COMBO_RANGE = 5;
    // 弹簧引力的最大有效范围（的平方）
    private double MAX_COMBO_RANGE_SQR = Math.pow(MAX_COMBO_RANGE, 2);
    // 弹簧的“死区”范围（格）。在最佳距离±此范围内，不施加力，以防止抖动。
    private double DEAD_ZONE = 0.5;
    // 连击状态的持续时间 (Ticks)
    private int COMBO_DURATION_TICKS = 15;

    private int currentTick = 0;

    private Player player;
    private Entity targetEntity;

    public SpringAttractionEffect(Player player, Entity targetEntity) {
        this.player = player;
        this.targetEntity = targetEntity;
        currentTick = 0;
    }

    @Override
    public void tick() {
        // 安全检查: 任何一方失效、死亡或不在同一维度，则停止
        if (!player.isAlive() || !targetEntity.isAlive() || player.level() != targetEntity.level()) {
            return;
        }

        double distSqr = player.distanceToSqr(targetEntity);

        // 范围检查: 如果目标超出最大连击范围，则停止
        if (distSqr > MAX_COMBO_RANGE_SQR) {
            return;
        }

        double currentDistance = Math.sqrt(distSqr);
        double distanceError = currentDistance - OPTIMAL_DISTANCE;

        // 应用“死区”，防止在最佳距离附近时发生抖动
        if (Math.abs(distanceError) < DEAD_ZONE) {
            return;
        }

        // 计算方向和力的大小 (力 = 误差 * 劲度系数)
        Vec3 directionVec = targetEntity.getPosition(0).subtract(player.getPosition(0)).normalize();
        Vec3 forceVector = directionVec.scale(distanceError * SPRING_CONSTANT);

        // 应用力：太远则拉近(吸引力)，太近则推开(排斥力)
        ((ServerPlayer) player).connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement().add(forceVector.scale(0.9))));
        targetEntity.addDeltaMovement(forceVector.scale(-0.15));

        currentTick++;
    }

    @Override
    public boolean isFinished() {
        return currentTick >= COMBO_DURATION_TICKS;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    public double getOPTIMAL_DISTANCE() {
        return OPTIMAL_DISTANCE;
    }

    public void setOPTIMAL_DISTANCE(double OPTIMAL_DISTANCE) {
        this.OPTIMAL_DISTANCE = OPTIMAL_DISTANCE;
    }

    public double getSPRING_CONSTANT() {
        return SPRING_CONSTANT;
    }

    public void setSPRING_CONSTANT(double SPRING_CONSTANT) {
        this.SPRING_CONSTANT = SPRING_CONSTANT;
    }

    public double getMAX_COMBO_RANGE() {
        return MAX_COMBO_RANGE;
    }

    public void setMAX_COMBO_RANGE(double MAX_COMBO_RANGE) {
        this.MAX_COMBO_RANGE = MAX_COMBO_RANGE;
    }

    public double getDEAD_ZONE() {
        return DEAD_ZONE;
    }

    public void setDEAD_ZONE(double DEAD_ZONE) {
        this.DEAD_ZONE = DEAD_ZONE;
    }

    public int getCOMBO_DURATION_TICKS() {
        return COMBO_DURATION_TICKS;
    }

    public void setCOMBO_DURATION_TICKS(int COMBO_DURATION_TICKS) {
        this.COMBO_DURATION_TICKS = COMBO_DURATION_TICKS;
    }
}
