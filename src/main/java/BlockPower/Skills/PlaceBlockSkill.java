package BlockPower.Skills;

import BlockPower.ModMessages.SkillC2SPacket.PlaceBlockSkillPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Util.PlayerData.PlayerSkillData.PlayerSkillsData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PlaceBlockSkill implements Skill {
    @Override
    public String getSkillName() {
        return "PlaceBlock";
    }

    @Override
    public String getSkillDescription(int skillLevel) {
        return "";
    }

    @Override
    public int getSkillLevel() {
        return PlayerSkillsData.getSkillLevel();
    }

    @Override
    public boolean canUse(Player player, @Nullable Entity target) {
        return false;
    }

    @Override
    public void triggerSkill(int skillLevel) {
        ModMessages.sendToServer(new PlaceBlockSkillPacket_C2S());
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.DIRT;
    }

    @Override
    public double getSkillCostAmount() {
        return 1;
    }
}
