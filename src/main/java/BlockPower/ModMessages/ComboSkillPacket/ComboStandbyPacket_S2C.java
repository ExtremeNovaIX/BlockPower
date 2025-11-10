package BlockPower.ModMessages.ComboSkillPacket;

import BlockPower.ModMessages.S2CPacket.AbstractS2CPacket;
import BlockPower.Client.ComboSkills.ComboManager.Client.ClientComboData;
import BlockPower.Skills.ComboSkills.IComboSkill;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.IPacketSerializable;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端发送连携技就绪通知的数据包，用于通知客户端连携技已准备就绪
 */
public class ComboStandbyPacket_S2C extends AbstractS2CPacket {
    private static final Logger log = LoggerFactory.getLogger(ComboStandbyPacket_S2C.class);
    private final ComboSkillType comboSkillType;

    // 服务端构造
    public ComboStandbyPacket_S2C(ComboSkillType comboSkillType) {
        this.comboSkillType = comboSkillType;
    }

    // 客户端构造
    public ComboStandbyPacket_S2C(FriendlyByteBuf buf) {
        this.comboSkillType = buf.readEnum(ComboSkillType.class);

        // 根据枚举值实例化正确的技能类
        try {
            // 注意：技能类需要一个无参构造函数
            IComboSkill comboSkill = comboSkillType.getSkill().getClass().getDeclaredConstructor().newInstance();
            // 检查是否是可序列化的，并读取参数
            if (comboSkill instanceof IPacketSerializable paramSkill) {
                // 调用技能的readParams方法读取参数
                paramSkill.readParams(buf);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate skill: " + comboSkillType, e);
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(comboSkillType);

        // 写入参数
        if (comboSkillType.getSkill() instanceof IPacketSerializable paramSkill) {
            // 调用技能的writeParams方法写入参数
            paramSkill.writeParams(buf);
        }
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
