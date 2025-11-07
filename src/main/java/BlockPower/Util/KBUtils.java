package BlockPower.Util;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Skills.NormalSkills.ISkill;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class KBUtils {

    private static final Logger log = LoggerFactory.getLogger(KBUtils.class);

    private static final TaskManager taskManager = TaskManager.getInstance(false);

    /**
     * 为实体增加击退百分比属性
     *
     * @param entity    被击退的实体
     * @param kbPercent 击退百分比
     */
    public static void addKBPercent(@NotNull Entity entity, double kbPercent) {
        entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(knockbackValue -> {
            knockbackValue.addKBPercent(kbPercent);
        });
    }

    /**
     * 为实体增加击退百分比属性
     *
     * @param entities  被击退的实体列表
     * @param kbPercent 击退百分比
     */
    public static void addKBPercent(@NotNull List<Entity> entities, double kbPercent) {
        for (Entity entity : entities) {
            entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(knockbackValue -> {
                knockbackValue.addKBPercent(kbPercent);
            });
        }
    }

    /**
     * 为实体添加击退百分比属性并击退，会根据实体当前的击退百分比，应用最终对实体造成的击退值倍率
     *
     * @param entity        被击退的实体
     * @param baseKBPercent 技能基础击退值百分比
     */
    public static void applyKB(@NotNull Entity mainEntity, @NotNull Entity entity, double baseKBPercent, double baseKBForce) {
        double kbPercentMultiplier = calculateKBPercentMultiplier(entity);
        double kbForceMultiplier = calculateKBForceMultiplier(entity);
        double finalKBPercent = baseKBPercent * kbPercentMultiplier;
        double finalVerticalForce = baseKBForce * kbForceMultiplier * 1.1;
        double finalHorizontalForce = baseKBForce * kbForceMultiplier * 0.7;

        addKBPercent(entity, finalKBPercent);
        taskManager.runOnceWithCooldown(entity, "Commons.knockBackEntity", 8, () -> {
            Commons.knockBackEntity(mainEntity, entity, finalVerticalForce, finalHorizontalForce);
        });
        log.info("Add {} (base) * {} (multiplier) = {} kb value to {}", baseKBPercent, kbPercentMultiplier, finalKBPercent, entity.getName().getString());
    }

    public static void applyKB(@NotNull Entity mainEntity, @NotNull List<Entity> entities, double baseKBPercent, double baseKBForce) {
        if (entities.isEmpty()) return;
        for (Entity entity : entities) {
            applyKB(mainEntity, entity, baseKBPercent, baseKBForce);
        }
    }

    /**
     * 计算技能伤害值，会根据实体当前的击退值百分比，计算最终对实体造成的伤害倍率
     *
     * @param entity     受到伤害的实体
     * @param baseDamage 基础伤害值
     * @return 最终伤害值
     */
    public static double calculateSkillDamage(@NotNull Entity entity, double baseDamage) {
        double multiplier = calculateDamageMultiplier(entity);
        log.info("Add {} (base) * {} (multiplier) = {} damage value to {}", baseDamage, multiplier, baseDamage * multiplier, entity.getName().getString());
        return baseDamage * multiplier;
    }

    /**
     * 根据实体当前的击退百分比，计算本次技能附加的击退值的增长倍率。
     *
     * @param entity 被击退的实体
     * @return 击退值增长倍率
     */
    public static double calculateKBPercentMultiplier(@NotNull Entity entity) {
        AtomicReference<Double> currentKBPercent = new AtomicReference<>(0.0);
        entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
            currentKBPercent.set(cap.getKBPercent());
        });

        double kbPercent = currentKBPercent.get();
        double multiplier;

        if (kbPercent <= 100) {
            // 0-100% 区间: 0.01 -> 0.8，x^2曲线
            double progress = kbPercent / 100.0;
            multiplier = 0.01 + Math.pow(progress, 2) * 0.79;
        } else if (kbPercent <= 200) {
            // 100-200% 区间: 0.8 -> 1.5，线性增长
            double progress = (kbPercent - 100.0) / 100.0;
            multiplier = 0.8 + progress * 0.7;
        } else {
            // 200%+ 区间: 1.5 -> 100，线性增长
            double progress = (kbPercent - 200.0) / 200.0;
            multiplier = 1.5 + progress * 98.5;
        }

        return Mth.clamp(multiplier, 0.01, 100.0);
    }

    private static double calculateKBForceMultiplier(@NotNull Entity entity) {
        AtomicReference<Double> currentKBPercent = new AtomicReference<>(0.0);
        entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
            currentKBPercent.set(cap.getKBPercent());
        });

        double kbPercent = currentKBPercent.get();
        double multiplier;

        if (kbPercent <= 100) {
            // 0-100% 区间: 0.5 -> 0.7，x^2曲线
            double progress = kbPercent / 100.0;
            multiplier = 0.5 + Math.pow(progress, 2) * 0.2;
        } else if (kbPercent <= 200) {
            // 100-200% 区间: 0.7 -> 1.0，线性增长
            double progress = (kbPercent - 100.0) / 100.0;
            multiplier = 0.7 + progress * 0.3;
        } else {
            // 200%+ 区间: 1.0 -> 1.5，线性增长
            double progress = (kbPercent - 200.0) / (1000.0 - 200.0);
            multiplier = 1.0 + progress * 0.5;
        }

        return Mth.clamp(multiplier, 0.1, 2);
    }

    /**
     * 根据实体当前的击退百分比，计算最终对实体造成的伤害倍率。
     *
     * @param entity 被击退的实体
     * @return 计算后的最终伤害倍率
     */
    private static double calculateDamageMultiplier(@NotNull Entity entity) {
        AtomicReference<Double> currentKBPercent = new AtomicReference<>(0.0);
        entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
            currentKBPercent.set(cap.getKBPercent());
        });

        double kbPercent = currentKBPercent.get();
        double multiplier;

        if (kbPercent <= 100) {
            // 0-100%: 三次缓动曲线，伤害带有减免
            double progress = kbPercent / 100.0;
            double easeOutCubic = MathUtils.easeOutCubic((float) progress);
            multiplier = 0.1 + easeOutCubic * 0.7;
        } else if (kbPercent <= 200) {
            // 100-200%: 对数增长，伤害带有增益，最大增益为1.5倍
            double progress = (kbPercent - 100.0) / 100.0;
            double logValue = Math.log1p(progress);
            multiplier = 0.8 + (logValue / Math.log(2.0)) * 0.7;
        } else {
            // 200%+: 指数增长，每提高10%增加一倍
            double progress = (kbPercent - 200.0) / 10.0;
            // 上限为1024倍
            if (progress > 10) {
                progress = 10;
            }
            multiplier = 1.5 * Math.pow(2, progress);
        }
        return multiplier;
    }
}
