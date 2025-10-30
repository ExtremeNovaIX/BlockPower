package BlockPower.ModMessages.ComboSkillPacket;

import BlockPower.ModMessages.S2CPacket.AbstractS2CPacket;
import BlockPower.Skills.ComboSkills.ComboManager.Client.ClientComboData;
import BlockPower.Skills.ComboSkills.IComboSkill;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComboStandbyPacket_S2C extends AbstractS2CPacket {
    private static final Logger log = LoggerFactory.getLogger(ComboStandbyPacket_S2C.class);
    private final ComboSkillType comboSkillType;


    public ComboStandbyPacket_S2C(ComboSkillType comboSkillType) {
        this.comboSkillType = comboSkillType;
    }

    public ComboStandbyPacket_S2C(FriendlyByteBuf buf) {
        this.comboSkillType = buf.readEnum(ComboSkillType.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(comboSkillType);
    }

    @Override
    protected void handleClientSide() {
        log.info("Client-ComboStandbyC2SPacket: {} is ready to trigger", comboSkillType);
        IComboSkill skill = comboSkillType.getSkill();
        if (skill == null) {
            log.warn("Received ComboStandbyPacket_S2C for a skill type with no instance: {}", comboSkillType);
            return;
        }
        int durationTicks = skill.getComboWindowTick();
        log.info("Client-ComboStandbyC2SPacket: {} is ready for {} ticks", comboSkillType, durationTicks);
        ClientComboData.addActiveChainSkill(comboSkillType, durationTicks);
    }

}
