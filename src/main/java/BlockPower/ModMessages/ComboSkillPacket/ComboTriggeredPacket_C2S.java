package BlockPower.ModMessages.ComboSkillPacket;

import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端发送连携技触发请求的数据包，用于调用服务端对应连携技的触发逻辑
 */
public class ComboTriggeredPacket_C2S extends AbstractC2SPacket {
    private static final Logger log = LoggerFactory.getLogger(ComboTriggeredPacket_C2S.class);
    private final ComboSkillType comboSkillType;
    public ComboTriggeredPacket_C2S(ComboSkillType comboSkillType) {
        this.comboSkillType = comboSkillType;
    }

    public ComboTriggeredPacket_C2S(FriendlyByteBuf buf) {
        this.comboSkillType = buf.readEnum(ComboSkillType.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(comboSkillType);
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        log.info("Server received ComboTriggeredPacket_C2S:{} from:{}", comboSkillType,player.getUUID());
        //调用服务端对应连携技的触发逻辑
        comboSkillType.getSkill().triggerSkill(player);
    }
}
