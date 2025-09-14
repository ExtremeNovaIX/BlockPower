package BlockPower.ModMessages.SkillC2SPacket;

import BlockPower.ModItems.ModItems;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.CameraLockPacket_S2C;
import BlockPower.ModMessages.S2CPacket.HitStopPacket_S2C;
import BlockPower.ModMessages.S2CPacket.ShakePacket_S2C;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Skills.LauncherSwingSkill;
import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LauncherSwingSkillPacket_C2S extends AbstractSkillPacket_C2S {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final TaskManager taskManager = TaskManager.getInstance(false);

    // 理想的作战距离（格），弹簧会试图维持这个距离
    private static final double OPTIMAL_DISTANCE = 1;
    // 弹簧的力度系数 (劲度)。值越大，拉/推的力越强。
    private static final double SPRING_CONSTANT = 0.25;
    // 弹簧引力的最大有效范围（的平方）
    private static final double MAX_COMBO_RANGE_SQR = Math.pow(5, 2);
    // 弹簧的“死区”范围（格）。在最佳距离±此范围内，不施加力，以防止抖动。
    private static final double DEAD_ZONE = 0.5;
    // 连击状态的持续时间 (Ticks)
    private static final int COMBO_DURATION_TICKS = 15;


    public LauncherSwingSkillPacket_C2S() {
        super(new LauncherSwingSkill());
    }

    public LauncherSwingSkillPacket_C2S(FriendlyByteBuf buf) {
        super(new LauncherSwingSkill());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        if (player.getXRot() >= -25.0F) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() != ModItems.PIXEL_CORE.get()) return;
        Commons.changePixelCoreNBT(player,3F,1F,1F);
        launcherSwing(player);
    }

    private void launcherSwing(ServerPlayer player) {
        List<Entity> entities = Commons.applyDamage(player, player, 3F, 6, ModSounds.HIT_SOUND.get());
        Commons.knockBackEntityUp(player, entities, 1); // 施加初始的上挑击飞

        if (entities.isEmpty()) {
            return; // 未命中则不执行后续逻辑
        }

        final Entity targetEntity = entities.get(0); // 锁定第一个目标
        Runnable physicsTask = () -> {
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
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement().add(forceVector.scale(0.9))));
            targetEntity.addDeltaMovement(forceVector.scale(-0.15));
        };

        String taskID = "combo_attract_" + player.getUUID();

        taskManager.scheduleRepeatingTaskPerTick(taskID, COMBO_DURATION_TICKS, physicsTask, true);

        ModMessages.sendToPlayer(new CameraLockPacket_S2C(targetEntity.getId()), player);
        ModMessages.sendToPlayer(new HitStopPacket_S2C(2), player);
        ModMessages.sendToPlayer(new ShakePacket_S2C(4, 2F), player);
    }

    @Override
    protected void afterHandleServerSide(ServerPlayer player) {
    }
}