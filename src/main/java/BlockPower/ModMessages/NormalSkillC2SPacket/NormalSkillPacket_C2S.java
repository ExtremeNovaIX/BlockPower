package BlockPower.ModMessages.NormalSkillC2SPacket;

import BlockPower.ModException.SilentSkillException;
import BlockPower.ModException.SkillException;
import BlockPower.ModItems.ModItems;
import BlockPower.Skills.NormalSkills.IPacketSerializableSkill;
import BlockPower.Skills.NormalSkills.ISkill;
import BlockPower.Skills.SkillExecutionResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class NormalSkillPacket_C2S extends AbstractSkillPacket_C2S {
    // 存储技能类型 ID
    private final NormalSkillType skillType;

    // 客户端构造：将 Skill 实例和类型 ID 传入
    public NormalSkillPacket_C2S(NormalSkillType skillType, IPacketSerializableSkill skill) {
        super(skill);
        this.skillType = skillType;
    }

    // 网络接收构造：服务器端接收时调用
    public NormalSkillPacket_C2S(FriendlyByteBuf buf) {
        // 读取存有技能类型的枚举值
        this.skillType = buf.readEnum(NormalSkillType.class);

        // 根据枚举值实例化正确的技能类
        try {
            // 注意：这里需要一个默认的无参构造函数
            ISkill newSkill = skillType.skillClass.getDeclaredConstructor().newInstance();
            // 检查是否是可序列化的，并读取参数
            if (newSkill instanceof IPacketSerializableSkill paramSkill) {
                // 调用技能的readParams方法读取参数
                paramSkill.readParams(buf);
            }
            // 赋值给父类的protected skill字段
            super.skill = newSkill;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate skill: " + skillType, e);
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        // 写入 ID
        buf.writeEnum(this.skillType);

        // 写入参数
        if (skill instanceof IPacketSerializableSkill paramSkill) {
            // 调用技能的writeParams方法写入参数
            paramSkill.writeParams(buf);
        }
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        SkillExecutionResult skillExecutionResult = skill.triggerSkill(player);
        if (!skillExecutionResult.isSuccess()) {
            throw new SilentSkillException(skill, skillExecutionResult.getMessage());
        }
    }
}
