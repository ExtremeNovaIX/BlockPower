package BlockPower.ModMessages.ComboSkillPacket;

import BlockPower.Main.Main;
import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.ModMessages.S2CPacket.AbstractS2CPacket;
import BlockPower.Skills.ComboSkills.Client.ClientComboData;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端发送连携技触发请求的数据包，用于调用服务端对应连携技的触发逻辑
 */
public class ComboTriggeredC2SPacket extends AbstractC2SPacket {
    private static final Logger log = LoggerFactory.getLogger(Main.MOD_ID);
    private final ComboSkillType comboSkillType;
    public ComboTriggeredC2SPacket(ComboSkillType comboSkillType) {
        this.comboSkillType = comboSkillType;
    }

    public ComboTriggeredC2SPacket(FriendlyByteBuf buf) {
        this.comboSkillType = buf.readEnum(ComboSkillType.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(comboSkillType);
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        log.info("Server-ComboTriggeredC2SPacket: {} is triggered", comboSkillType);
        //调用服务端对应连携技的触发逻辑
        comboSkillType.getSkill().triggerSkill(player);
    }
}
