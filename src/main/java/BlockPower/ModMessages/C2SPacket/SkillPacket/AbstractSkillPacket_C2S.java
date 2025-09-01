package BlockPower.ModMessages.C2SPacket.SkillPacket;

import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.PlayerResourceData;
import BlockPower.Skills.MinerState.server.PlayerResourceManager;
import BlockPower.Skills.Skill;
import BlockPower.Util.Commons;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractSkillPacket_C2S extends AbstractC2SPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSkillPacket_C2S.class);
    protected Skill skill;

    public AbstractSkillPacket_C2S(Skill skill) {
        this.skill = skill;
    }

    public AbstractSkillPacket_C2S() {

    }

    protected static final PlayerResourceManager playerResourceManager = PlayerResourceManager.getInstance();

    @Override
    protected boolean checkLegit(ServerPlayer player) {
        PlayerResourceData playerResourceData = playerResourceManager.getPlayerData(player);
        // 检查技能资源是否足够
        if (skill != null) {
            if(Commons.checkServerPlayerMode(player)) return true;

            AllResourceType costType = skill.getSkillCostType();
            double costAmount = skill.getSkillCostAmount();
            return playerResourceData.hasEnoughResource(costType, costAmount);
        } else {
            LOGGER.info("技能对象为null，数据包类型：{}", this.getClass().getSimpleName());
            return false;
        }
    }

    protected void consumeResource(ServerPlayer player, Skill skill) {
        if(Commons.checkServerPlayerMode(player)) return;

        AllResourceType type = skill.getSkillCostType();
        double amount = skill.getSkillCostAmount();
        PlayerResourceData playerResourceData = playerResourceManager.getPlayerData(player);
        playerResourceData.consumeResource(type, amount, player);
    }
}
