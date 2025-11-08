package BlockPower.Skills.ComboSkills.ComboManager.Client;

import BlockPower.Skills.ComboSkills.ComboSkillType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 客户端缓存连击数据，用于渲染连携技图标
 */
@OnlyIn(Dist.CLIENT)
public class ClientComboData {
    // 连携技“按下”动画持续时间（Tick）
    public static final int ANIMATION_DURATION_TICKS = 4;

    /**
     * 内部数据类，用于存储激活的技能及其剩余Tick
     */
    public static class ActiveSkillData {
        private final ComboSkillType type;
        private final int totalDurationTicks; // 技能的总时长，用于计算百分比
        private int remainingTicks;      // 技能剩余的 Tick

        // 技能“按下”动画相关字段
        private boolean isTriggered = false;
        private int triggerAnimTicks = 0;

        public ActiveSkillData(ComboSkillType type, int durationTicks) {
            this.type = type;
            this.totalDurationTicks = durationTicks;
            this.remainingTicks = durationTicks;
        }

        public ComboSkillType getType() {
            return type;
        }

        /**
         * 获取剩余时间百分比 (1.0 -> 0.0)
         */
        public float getRemainingPercent() {
            if (totalDurationTicks == 0) return 0.0f;
            return Math.max(0.0f, (float) remainingTicks / (float) totalDurationTicks);
        }

        /**
         * 获取连携技“按下”动画进度百分比 (0.0 -> 1.0)
         */
        public float getTriggerAnimPercent(float partialTick) {
            if (!isTriggered) return 0.0f;
            float progress = (this.triggerAnimTicks + partialTick) / (float) ANIMATION_DURATION_TICKS;
            return Math.min(1.0f, progress);
        }

        // 内部方法，由 clientTick 调用
        private void tick() {
            if (remainingTicks > 0) {
                this.remainingTicks--;
            }

            if (isTriggered) {
                this.triggerAnimTicks++;
            }
        }

        private boolean isExpired() {
            return this.remainingTicks <= 0;
        }

        public boolean isTriggered() {
            return isTriggered;
        }

        private boolean isAnimationFinished() {
            return isTriggered && this.triggerAnimTicks > ANIMATION_DURATION_TICKS;
        }
    }

    //用于记录当前可释放的连携技
    private static final List<ActiveSkillData> activeComboSkills = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ClientComboData.class);
    private static boolean isComboSkillAvailable = true;

    /**
     * 添加可释放的连携技
     */
    public static void addActiveChainSkill(ComboSkillType comboSkillType, int durationTicks) {
        log.info("ClientComboData add: {} for {} ticks", comboSkillType, durationTicks);
        // 检查是否已存在同类型，如果需要则刷新
        activeComboSkills.removeIf(skill -> skill.getType() == comboSkillType);

        activeComboSkills.add(new ActiveSkillData(comboSkillType, durationTicks));
        log.info("ClientComboData after add: activeComboSkills: {}", activeComboSkills);
    }

    /**
     * 获取当前可释放的连携技
     */
    public static List<ActiveSkillData> getActiveComboSkills() {
        return activeComboSkills;
    }

    /**
     * 返回第一个未被触发的连携技
     */
    public static ActiveSkillData getFirstActiveComboSkill() {
        for (ActiveSkillData skill : activeComboSkills) {
            if (!skill.isTriggered()) {
                return skill;
            }
        }
        return null;
    }

    public static boolean isComboSkillAvailable() {
        return isComboSkillAvailable;
    }

    /**
     * 移除已释放的连携技
     */
    public static void triggerSkillAnimation(ComboSkillType comboSkillType) {
        log.info("ClientComboData trigger: {}", comboSkillType);
        // 找到第一个匹配的技能并标记它
        for (ActiveSkillData skill : activeComboSkills) {
            if (skill.getType() == comboSkillType && !skill.isTriggered()) {
                skill.isTriggered = true;
                skill.triggerAnimTicks = 0;
                break; // 只触发第一个
            }
        }
    }

    public static void clientTick() {
        Iterator<ActiveSkillData> iterator = activeComboSkills.iterator();
        while (iterator.hasNext()) {
            ActiveSkillData skill = iterator.next();
            skill.tick(); // 推进主计时器和动画计时器

            // 如果技能过期了，或者技能动画播放完毕，则移除
            if (skill.isExpired() || skill.isAnimationFinished()) {
                log.info("ClientComboData: Skill {} removed (Expired: {}, AnimFinished: {})",
                        skill.getType(), skill.isExpired(), skill.isAnimationFinished());
                iterator.remove();
            }
        }
    }
}
