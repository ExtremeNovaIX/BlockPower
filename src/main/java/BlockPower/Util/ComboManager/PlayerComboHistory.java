package BlockPower.Util.ComboManager;

import BlockPower.Skills.ComboSkills.ComboSkill;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Util.TaskManager;
import BlockPower.Util.Timer.TickListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlayerComboHistory {
    private static final Logger log = LoggerFactory.getLogger(PlayerComboHistory.class);
    private final Map<ComboSkillType, AtomicChainData> comboCountMap;//每个玩家独有的连击记录
    private static final List<ComboSkillType> COMBO_SKILL_TYPES = Arrays.asList(ComboSkillType.values());


    private static final TaskManager taskManager = TaskManager.getInstance(false);

    public PlayerComboHistory() {
        comboCountMap = new EnumMap<>(ComboSkillType.class);
    }

    public void recordCombo(ComboSkillType type) {
        AtomicChainData atomicChainData = comboCountMap.computeIfAbsent(type, k -> new AtomicChainData());
        atomicChainData.addCount();
    }

    public void comboStandby(ComboSkillType type) {
        AtomicChainData atomicChainData = comboCountMap.get(type);
        if (atomicChainData == null) {
            return;
        }
        log.info("ComboStandbyC2SPacket: {} is ready to trigger", type);
        atomicChainData.triggerSkill(type);
    }

    //获取当前连击数
    public int getComboCount(ComboSkillType type) {
        AtomicChainData atomicChainData = comboCountMap.get(type);
        if (atomicChainData == null) {
            return 0;
        }
        return atomicChainData.count;
    }

    /**
     * 遍历并移除所有过时的连击段。
     * 注意：此方法应由 PlayerComboManager调用。
     */
    public void refreshComboWindow() {
        long currentTick = TickListener.getServerTicks();

        for (Map.Entry<ComboSkillType, AtomicChainData> entry : comboCountMap.entrySet()) {
            AtomicChainData atomicChainData = entry.getValue();

            ComboSkill comboSkill = entry.getKey().getSkill();
            int comboWindowTick = comboSkill.getComboWindowTick();
            //检查是否过期
            if (currentTick - atomicChainData.lastTriggerTick > comboWindowTick) {
                //把当前连击数重置为0
                if (atomicChainData.count > 0) {
                    log.debug("Combo reset: {} count {} exceeded window of {} ticks.",
                            entry.getKey(), atomicChainData.count, comboWindowTick);
                }
                atomicChainData.count = 0;
            }
        }
    }

    /**
     * 获取当前玩家所有可触发的连击技能。
     *
     * @return 可触发的连击技能列表
     */
    public List<ComboSkillType> getActiveChainSkills() {
        List<ComboSkillType> activeChainSkills = new ArrayList<>();
        //遍历所有技能，查看技能条件是否达成
        for (ComboSkillType type : COMBO_SKILL_TYPES) {
            //获取当前技能的连击记录
            AtomicChainData atomicChainData = comboCountMap.get(type);
            //如果连击记录为空或技能冷却中，则跳过
            if (atomicChainData == null || !atomicChainData.isActive) {
                continue;
            }
            //获取当前技能实例
            ComboSkill skill = type.getSkill();
            //获取当前技能连击次数
            int comboCount = atomicChainData.count;
            if (skill.canTriggerSkill(comboCount)) {
                activeChainSkills.add(type);
            }
        }
        return activeChainSkills;
    }


    public class AtomicChainData {
        //连击的当前段数
        public int count;
        //上一次成功施放该段连击时的游戏 Tick 计数
        public long lastTriggerTick;
        //技能是否能够使用(冷却中为false)
        public boolean isActive;

        public AtomicChainData() {
            this.count = 0;
            this.lastTriggerTick = TickListener.getServerTicks();
            this.isActive = true;
        }

        /**
         * 触发技能，重置连击数和冷却时间。
         */
        public void triggerSkill(ComboSkillType type) {
            count = 0;
            lastTriggerTick = TickListener.getServerTicks();
            isActive = false;
            //添加任务，在冷却时间后将isActive设置为true
            taskManager.runTaskAfterTicks(type.getSkill().getCooldownTick(), () -> isActive = true);
        }

        public void addCount() {
            count++;
            lastTriggerTick = TickListener.getServerTicks();
        }
    }
}
