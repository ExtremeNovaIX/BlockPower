package BlockPower.Skills.NormalSkills;

import BlockPower.ModEntities.RushMinecart.RushMinecartEntity;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class RushMinecartSkill implements IPacketSerializableSkill {
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
        RushMinecartEntity.createRushMinecart(player);
        return SkillExecutionResult.success();
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.IRON;
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
    public void writeParams(FriendlyByteBuf buf) {

    }

    @Override
    public void readParams(FriendlyByteBuf buf) {

    }


}
