package BlockPower.ModMessages.ComboSkillPacket;

import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.ComboSkills.IComboSkill;
import BlockPower.Skills.IPacketSerializable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端发送连携技触发请求的数据包，用于调用服务端对应连携技的触发逻辑
 */
public class ComboTriggeredPacket_C2S extends AbstractC2SPacket {
    private static final Logger log = LoggerFactory.getLogger(ComboTriggeredPacket_C2S.class);
    private ComboSkillType comboSkillType;
    private IComboSkill skill; // 持有实际要被触发的技能实例

    // 客户端构造：将 ComboSkill 实例和类型 ID 传入
    public ComboTriggeredPacket_C2S(ComboSkillType comboSkillType, IComboSkill skill) {
        this.comboSkillType = comboSkillType;
        this.skill = skill;
    }

    public ComboTriggeredPacket_C2S(ComboSkillType comboSkillType) {
        this.comboSkillType = comboSkillType;
    }

    // 网络接收构造：服务器端接收时调用
    public ComboTriggeredPacket_C2S(FriendlyByteBuf buf) {
        this.comboSkillType = buf.readEnum(ComboSkillType.class);

        // 根据枚举值实例化正确的技能类
        try {
            // 注意：技能类需要一个无参构造函数
            IComboSkill newSkill = comboSkillType.getSkill().getClass().getDeclaredConstructor().newInstance();
            // 检查是否是可序列化的，并读取参数
            if (newSkill instanceof IPacketSerializable paramSkill) {
                // 调用技能的readParams方法读取参数
                paramSkill.readParams(buf);
            }
            this.skill = newSkill; // 将反序列化后的技能实例赋值给内部字段
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate skill: " + comboSkillType, e);
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(this.comboSkillType);

        // 写入参数
        if (this.skill instanceof IPacketSerializable paramSkill) {
            // 调用技能的writeParams方法写入参数
            paramSkill.writeParams(buf);
        }
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        log.info("Server received ComboTriggeredPacket_C2S:{} from:{}", comboSkillType, player.getUUID());
        // 调用数据包内部持有的、经过反序列化并携带参数的技能实例
        this.skill.triggerSkill(player);
    }
}
