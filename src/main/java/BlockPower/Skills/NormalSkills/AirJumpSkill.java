package BlockPower.Skills.NormalSkills;

import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;

public class AirJumpSkill implements IPacketSerializableSkill {

    public static final Map<Player, Integer> playerAirTicks = new WeakHashMap<>();//记录玩家滞空时间

    private static final TaskManager taskManager = TaskManager.getInstance(false);

    private static final Logger log = LoggerFactory.getLogger(AirJumpSkill.class);

    private static final TimerManager timerManager = TimerManager.getInstance(false);

    private String keyResult;

    public AirJumpSkill(String keyResult) {
        this.keyResult = keyResult;
    }

    public AirJumpSkill() {
    }

    @Override
    public String getSkillName() {
        return "AirJump";
    }

    @Override
    public String getSkillDescription() {
        return "";
    }

    @Override
    public int getSkillLevel() {
        return 0;
    }

    @Override
    public void triggerSkill(ServerPlayer player) {
        if (!player.onGround() && AirJumpSkill.getPlayerAirTicks(player) >= 3) {
            taskManager.runOnce(player, "airJump", () -> {
                Vec3 motion;
                if (keyResult.equals("w")) {
                    motion = new Vec3(player.getDeltaMovement().x * 6, 0.8, player.getDeltaMovement().z * 6);
                } else {
                    motion = new Vec3(0, 0.9, 0);
                }
                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), motion));
                timerManager.setTimer(player, "noFallDamage", 100);
            });
        }
    }

    /**
     * 处理玩家空中跳跃计时器逻辑，在专门的event类调用
     *
     * @param player 目标玩家
     */
    public static void handleAirJump(ServerPlayer player) {
        if (player.onGround() && taskManager.queryRemainExecutions(player, "airJump") == 0) {
            taskManager.flushTasks(player, "airJump");
        }

        if (player.onGround()) {
            // 如果玩家在地上，移除计时器
            playerAirTicks.remove(player);
        } else {
            // 如果玩家在空中，将计时器+1
            playerAirTicks.merge(player, 1, Integer::sum);
        }
    }

    public String getKeyResult() {
        return keyResult;
    }

    /**
     * 获取玩家的滞空时长
     *
     * @param player 目标玩家
     * @return 玩家在空中的tick数，如果在地上则为0
     */
    public static int getPlayerAirTicks(Player player) {
        return playerAirTicks.getOrDefault(player, 0);
    }

    @Override
    public void writeParams(FriendlyByteBuf buf) {
        buf.writeUtf(this.keyResult);
    }

    @Override
    public void readParams(FriendlyByteBuf buf) {
        this.keyResult = buf.readUtf();
    }

    @Override
    public AllResourceType getSkillCostType() {
        return null;
    }

    @Override
    public double getSkillCostAmount() {
        return 0;
    }

    @Override
    public boolean isSkillConsumeResource() {
        return false;
    }

    @Override
    public boolean isSkillAutoLocked() {
        return false;
    }
}
