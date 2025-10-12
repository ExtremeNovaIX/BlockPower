package BlockPower.Util.ComboManager;

import BlockPower.ModMessages.ComboSkillPacket.ComboStandbyS2CPacket;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Util.Timer.TickListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class PlayerComboManager {
    // 玩家连击记录映射表，键为玩家实体，值为玩家的连击记录
    private static final Map<Player, PlayerComboHistory> comboHistoryMap = new WeakHashMap<>();

    /**
     * 获取玩家的连击记录。
     * 如果玩家不存在记录，则创建一个新记录并返回。
     *
     * @param player 玩家实体
     * @return 玩家的连击记录
     */
    public static PlayerComboHistory getComboHistory(Player player) {
        return comboHistoryMap.computeIfAbsent(player, p -> new PlayerComboHistory());
    }

    /**
     * 记录玩家的连击，达到一定条件以后可以触发连携技
     *
     * @param player 玩家实体
     * @param type   连击技能类型
     */
    public static void recordCombo(Player player, ComboSkillType type) {
        PlayerComboHistory history = getComboHistory(player);
        history.recordCombo(type);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 每3Tick刷新一次所有玩家的连击记录
            if (TickListener.getServerTicks() % 3 != 0) return;
            // 遍历所有玩家
            for (Player player : comboHistoryMap.keySet()) {
                // 获取玩家的连击记录
                PlayerComboHistory history = comboHistoryMap.computeIfAbsent(player, p -> new PlayerComboHistory());
                // 刷新玩家的连击窗口
                history.refreshComboWindow();

                // 获取玩家当前可触发的连击技能
                List<ComboSkillType> activeChainSkills = history.getActiveChainSkills();
                // 遍历所有可触发的连击技能
                for (ComboSkillType type : activeChainSkills) {
                    // 通知客户端连携已就绪
                    ModMessages.sendToPlayer(new ComboStandbyS2CPacket(type), (ServerPlayer) player);
                    // 重置连击数，并进入冷却
                    history.comboStandby(type);
                }
            }
        }
    }
}
