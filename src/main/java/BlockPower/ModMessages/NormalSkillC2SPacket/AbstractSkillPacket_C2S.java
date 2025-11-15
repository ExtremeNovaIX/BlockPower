package BlockPower.ModMessages.NormalSkillC2SPacket;

import BlockPower.Capability.ModCapabilities;
import BlockPower.ModItems.ModItems;
import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.NormalSkills.ISkill;
import BlockPower.Skills.SkillLock.LockPriority;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.Commons;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

abstract class AbstractSkillPacket_C2S extends AbstractC2SPacket {
    private static final Logger log = LoggerFactory.getLogger(AbstractSkillPacket_C2S.class);
    protected ISkill skill;

    public AbstractSkillPacket_C2S(ISkill skill) {
        this.skill = skill;
    }

    public AbstractSkillPacket_C2S() {

    }

    @Override
    protected boolean checkLegit(ServerPlayer player) {
        if (Commons.isSpectatorOrCreativeMode(player)) return true;
        if (SkillLockManager.isLocked(player, LockPriority.MEDIUM)) return false;
        if (skill.isMustMainHandItemPixelCore()) {
            if (player.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) return false;
        }

        if (skill != null) {
            if (skill.getSkillCostType() == null || skill.getSkillCostAmount() == 0) return true;

            ResourceType costType = skill.getSkillCostType();
            double costAmount = skill.getSkillCostAmount();

            // 使用Capability进行资源检查
            AtomicBoolean hasEnough = new AtomicBoolean(false);
            player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(minerState -> {
                if (minerState.hasEnoughResource(costType, costAmount)) {
                    hasEnough.set(true);
                }
            });
            return hasEnough.get();
        } else {
            log.info("技能对象为null，数据包类型：{}", this.getClass().getSimpleName());
            return false;
        }
    }

    protected void consumeResource(ServerPlayer player, ISkill skill) {
        if (Commons.isSpectatorOrCreativeMode(player)) return;
        if (skill.getSkillCostType() == null && skill.getSkillCostAmount() == 0) return;
        ResourceType type = skill.getSkillCostType();
        double amount = skill.getSkillCostAmount();

        // 使用Capability消耗资源
        player.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(minerState -> {
            minerState.consumeResource(type, amount, player);
        });
    }

    protected boolean isSkillConsumeResource() {
        return skill.isSkillConsumeResource();
    }
}
