package BlockPower.ModMessages.ComboSkillPacket;

import BlockPower.ModMessages.S2CPacket.AbstractS2CPacket;
import BlockPower.Skills.ComboSkills.Client.ClientComboData;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComboStandbyS2CPacket extends AbstractS2CPacket {
    private static final Logger log = LoggerFactory.getLogger(ComboStandbyS2CPacket.class);
    private final ComboSkillType comboSkillType;


    public ComboStandbyS2CPacket(ComboSkillType comboSkillType) {
        this.comboSkillType = comboSkillType;
    }

    public ComboStandbyS2CPacket(FriendlyByteBuf buf) {
        this.comboSkillType = buf.readEnum(ComboSkillType.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(comboSkillType);
    }

    @Override
    protected void handleClientSide() {
        log.info("Client-ComboStandbyC2SPacket: {} is ready to trigger", comboSkillType);
        //TODO 调用渲染逻辑等
        ClientComboData.addActiveChainSkill(comboSkillType);
    }

}
