package BlockPower.Skills;

import BlockPower.ModMessages.SkillC2SPacket.AirJumpPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Util.TaskManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class AirJumpSkill implements Skill {
    public static final Map<Player, Integer> playerAirTicks = new WeakHashMap<>();//记录玩家滞空时间

    private static final TaskManager taskManager = TaskManager.getInstance(false);

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
    public void triggerSkill(int skillLevel) {
        ModMessages.sendToServer(new AirJumpPacket_C2S());
    }

    @Override
    public AllResourceType getSkillCostType() {
        return null;
    }

    @Override
    public double getSkillCostAmount() {
        return 0;
    }


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

    /**
     * 获取玩家的滞空时长
     *
     * @param player 目标玩家
     * @return 玩家在空中的tick数，如果在地上则为0
     */
    public static int getPlayerAirTicks(Player player) {
        return playerAirTicks.getOrDefault(player, 0);
    }
}
