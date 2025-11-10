package BlockPower.Skills.NormalSkills;

import BlockPower.Skills.IPacketSerializable;
import BlockPower.Util.KBUtils;
import BlockPower.Util.ModEffects.ServerEffect.SpringAttractionEffect;
import BlockPower.ModItems.ModItems;
import BlockPower.ModItems.PixelCore.PixelCoreSkillState;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.HitStopPacket_S2C;
import BlockPower.ModMessages.S2CPacket.ShakePacket_S2C;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import BlockPower.Skills.ComboSkills.ComboManager.PlayerComboManager;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffects.ModEffectManager;
import BlockPower.Util.TaskManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

//TODO 修改成一段时间内combo上限6次，并且后几次击退明显增大，防止无限连
//TODO 修复异常吸附问题
public class LauncherSwingSkill implements ISkill {
    private static final TaskManager taskManager = TaskManager.getInstance(false);

    @Override
    public @NotNull String getSkillName() {
        return "LauncherSwing";
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
    public SkillExecutionResult triggerSkill(ServerPlayer player) {
        if (player.getXRot() >= -25.0F) return SkillExecutionResult.fail("XRot must be less than -25.0F");

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() != ModItems.PIXEL_CORE.get())
            return SkillExecutionResult.fail("Main hand item must be PIXEL_CORE");
        Commons.changePixelCoreNBT(player, PixelCoreSkillState.TOOL, 1F, 1F);
        launcherSwing(player);
        return SkillExecutionResult.success();
    }

    private void launcherSwing(ServerPlayer player) {
        List<Entity> entities = Commons.aabbDetectEntity(player, 6, player);
        if (entities.isEmpty()) {
            return; // 未命中则不执行后续逻辑
        }

        // 对每个实体单独应用伤害和击退百分比
        entities.forEach(entity -> {
            boolean result = Commons.applyDamage(player, null, entity, getSkillDamage());
            if (result) {
                // 分别对每个实体应用其对应的KB增长值
                KBUtils.addKBPercent(List.of(entity), getSkillKBPercent() * KBUtils.calculateKBPercentMultiplier(entity));
            }
        });

        for (int i = 0; i < entities.size(); i++) {
            if (i == 0) {
                Commons.knockBackEntityUp(player, entities, 1);
            } else {
                Commons.knockBackEntity(player, entities.get(i), 0.7, 1);
            }
        }
        Commons.playSoundWithCooldown(player, ModSounds.HIT_SOUND.get(), 1f, 8);

        final Entity targetEntity = entities.get(0);
        ModEffectManager.addEffect(player, new SpringAttractionEffect(player, targetEntity));
        ModMessages.sendToPlayer(new HitStopPacket_S2C(2), player);
        ModMessages.sendToPlayer(new ShakePacket_S2C(4, 2F), player);
    }


    @Override
    public AllResourceType getSkillCostType() {
        return null;
    }

    @Override
    public double getSkillCostAmount() {
        return 0;
    }

    @Override
    public boolean isSkillConsumeResource() {
        return false;
    }

    @Override
    public void recordCombo(ServerPlayer player) {
        PlayerComboManager.recordCombo(player, ComboSkillType.MAGMA_BLOCK);
    }

    @Override
    public double getSkillKBPercent() {
        return 4;
    }

    @Override
    public double getSkillDamage() {
        return 3;
    }
}
