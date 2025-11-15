package BlockPower.Skills.NormalSkills;

import BlockPower.ModEntities.RushMinecartEntity;
import BlockPower.Skills.ComboSkills.ComboManager.PlayerComboManager;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.SkillExecutionResult;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class RushMinecartSkill implements ISkill {
    @Override
    public @NotNull String getSkillName() {
        return "RushMinecart";
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
        RushMinecartEntity.createRushMinecart(player,this);
        return SkillExecutionResult.success();
    }

    @Override
    public ResourceType getSkillCostType() {
        return ResourceType.IRON;
    }

    @Override
    public double getSkillCostAmount() {
        return 4;
    }

    @Override
    public boolean isSkillConsumeResource() {
        return true;
    }

    @Override
    public void recordCombo(ServerPlayer player) {
        PlayerComboManager.recordCombo(player, ComboSkillType.FIRECRACKER);
    }

    @Override
    public double getSkillKBPercent() {
        return 10;
    }

    @Override
    public double getSkillDamage() {
        return 15;
    }
}
