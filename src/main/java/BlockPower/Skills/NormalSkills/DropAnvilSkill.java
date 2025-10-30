package BlockPower.Skills.NormalSkills;

import BlockPower.ModEntities.DropAnvilEntity;
import BlockPower.Skills.ComboSkills.ComboManager.Server.PlayerComboManager;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class DropAnvilSkill implements IPacketSerializableSkill {
    @Override
    public @NotNull String getSkillName() {
        return "DropAnvil";
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
    public SkillExecutionResult triggerSkill(ServerPlayer player) {
        DropAnvilEntity.createDropAnvil(player);
        return SkillExecutionResult.success();
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.IRON;
    }

    @Override
    public double getSkillCostAmount() {
        return 10;
    }

    @Override
    public void writeParams(FriendlyByteBuf buf) {

    }

    @Override
    public void readParams(FriendlyByteBuf buf) {

    }

    @Override
    public boolean isSkillConsumeResource() {
        return true;
    }

    @Override
    public void recordCombo(ServerPlayer player) {
        PlayerComboManager.recordCombo(player, ComboSkillType.FIRECRACKER);
    }

}
