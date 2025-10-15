package BlockPower.Skills.NormalSkills;

import BlockPower.ModEntities.DropAnvil.DropAnvilEntity;
import BlockPower.Skills.MinerState.server.AllResourceType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class DropAnvilSkill implements IPacketSerializableSkill {
    @Override
    public String getSkillName() {
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
    public void triggerSkill(ServerPlayer player) {
        DropAnvilEntity.createDropAnvil(player);
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
    public boolean isSkillAutoLocked() {
        return true;
    }
}
