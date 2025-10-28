package BlockPower.ModMessages.NormalSkillC2SPacket;

import BlockPower.ModItems.ModItems;
import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.PlayerResourceData;
import BlockPower.Skills.MinerState.server.PlayerResourceManager;
import BlockPower.Skills.NormalSkills.ISkill;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.Commons;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractSkillPacket_C2S extends AbstractC2SPacket{
    private static final Logger log = LoggerFactory.getLogger(AbstractSkillPacket_C2S.class);
    protected ISkill skill;
    protected static final PlayerResourceManager playerResourceManager = PlayerResourceManager.getInstance();

    public AbstractSkillPacket_C2S(ISkill skill) {
        this.skill = skill;
    }

    public AbstractSkillPacket_C2S() {

    }

    @Override
    protected boolean checkLegit(ServerPlayer player) {
        if (Commons.isSpectatorOrCreativeMode(player)) return true;
        if (SkillLockManager.isLocked(player)) return false;//如果玩家被技能锁锁定，直接返回false
        if (player.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) return false;//如果玩家主手物品不是像素核心，直接返回false

        PlayerResourceData playerResourceData = playerResourceManager.getPlayerData(player);
        // 检查技能资源是否足够
        if (skill != null) {
            if (skill.getSkillCostType() == null || skill.getSkillCostAmount() == 0) return true;

            AllResourceType costType = skill.getSkillCostType();
            double costAmount = skill.getSkillCostAmount();
            return playerResourceData.hasEnoughResource(costType, costAmount);
        } else {
            log.info("技能对象为null，数据包类型：{}", this.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    protected void afterHandleServerSide(ServerPlayer player) {
        consumeResource(player, skill);
    }

    protected void consumeResource(ServerPlayer player, ISkill skill) {
        if (!this.isSkillConsumeResource()) return;
        if (Commons.isSpectatorOrCreativeMode(player)) return;
        if (skill.getSkillCostType() == null && skill.getSkillCostAmount() == 0) return;
        AllResourceType type = skill.getSkillCostType();
        double amount = skill.getSkillCostAmount();
        PlayerResourceData playerResourceData = playerResourceManager.getPlayerData(player);
        playerResourceData.consumeResource(type, amount, player);
    }

    protected boolean isSkillConsumeResource() {
        return skill.isSkillConsumeResource();
    }
}
