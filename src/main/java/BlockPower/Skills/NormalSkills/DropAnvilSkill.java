package BlockPower.Skills.NormalSkills;

import BlockPower.ModEntities.DropAnvilEntity;
import BlockPower.Skills.ComboSkills.ComboManager.PlayerComboManager;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Skills.SkillExecutionResult;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class DropAnvilSkill implements ISkill {
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
        DropAnvilEntity.createDropAnvil(player, this);
        return SkillExecutionResult.success();
    }

    @Override
    public ResourceType getSkillCostType() {
        return ResourceType.IRON;
    }

    @Override
    public double getSkillCostAmount() {
        return 8;
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
        return 10;
    }
}
