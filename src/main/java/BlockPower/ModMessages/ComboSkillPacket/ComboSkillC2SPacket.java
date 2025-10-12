package BlockPower.ModMessages.ComboSkillPacket;

import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.Skills.ComboSkills.ComboSkill;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComboSkillC2SPacket extends AbstractC2SPacket {
    private static final Logger log = LoggerFactory.getLogger(ComboSkillC2SPacket.class);
    private ComboSkillType comboSkillType;

    public ComboSkillC2SPacket(ComboSkillType comboSkillType) {
        this.comboSkillType = comboSkillType;
    }

    public ComboSkillC2SPacket(FriendlyByteBuf buf) {
        this.comboSkillType = buf.readEnum(ComboSkillType.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(comboSkillType);
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        ComboSkill skill = comboSkillType.getSkill();
        if (skill != null) {
            skill.triggerSkill(player);
        } else {
            log.error("ComboSkillC2SPacket: 无法触发技能,comboSkillType={}", comboSkillType);
        }
    }
}
