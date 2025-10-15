package BlockPower.Skills.NormalSkills;

import BlockPower.ModEntities.RushMinecart.RushMinecartEntity;
import BlockPower.Skills.MinerState.server.AllResourceType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class RushMinecartSkill implements IPacketSerializableSkill {
    @Override
    public String getSkillName() {
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
    public void triggerSkill(ServerPlayer player) {
        RushMinecartEntity.createRushMinecart(player);
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
    public boolean isSkillAutoLocked() {
        return true;
    }

    @Override
    public void writeParams(FriendlyByteBuf buf) {

    }

    @Override
    public void readParams(FriendlyByteBuf buf) {

    }


}
