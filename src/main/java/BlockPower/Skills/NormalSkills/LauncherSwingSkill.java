package BlockPower.Skills.NormalSkills;

import BlockPower.ModEffects.ServerEffect.SpringAttractionEffect;
import BlockPower.ModItems.ModItems;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.CameraLockPacket_S2C;
import BlockPower.ModMessages.S2CPacket.HitStopPacket_S2C;
import BlockPower.ModMessages.S2CPacket.ShakePacket_S2C;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Util.ComboManager.PlayerComboManager;
import BlockPower.Util.Commons;
import BlockPower.Util.ModEffect.ModEffectManager;
import BlockPower.Util.TaskManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

//TODO 修改成一段时间内combo上限6次，并且后几次击退明显增大，防止无限连
public class LauncherSwingSkill implements IPacketSerializableSkill {
    private static final TaskManager taskManager = TaskManager.getInstance(false);

    @Override
    public String getSkillName() {
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
    public void triggerSkill(ServerPlayer player) {
        if (player.getXRot() >= -25.0F) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() != ModItems.PIXEL_CORE.get()) return;
        Commons.changePixelCoreNBT(player,3F,1F,1F);
        launcherSwing(player);
    }

    private void launcherSwing(ServerPlayer player) {
        List<Entity> entities = Commons.applyDamage(player, player, 3F, 6, ModSounds.HIT_SOUND.get());
        Commons.knockBackEntityUp(player, entities, 1); // 施加初始的上挑击飞

        if (entities.isEmpty()) {
            return; // 未命中则不执行后续逻辑
        }

        final Entity targetEntity = entities.get(0); // 锁定第一个目标
        ModEffectManager.addEffect(player,new SpringAttractionEffect(player, targetEntity));
        ModMessages.sendToPlayer(new CameraLockPacket_S2C(targetEntity.getId()), player);
        ModMessages.sendToPlayer(new HitStopPacket_S2C(2), player);
        ModMessages.sendToPlayer(new ShakePacket_S2C(4, 2F), player);

        // 记录连击
        PlayerComboManager.recordCombo(player, ComboSkillType.MAGMA_BLOCK);
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
    public boolean isSkillAutoLocked() {
        return false;
    }

    @Override
    public void writeParams(FriendlyByteBuf buf) {

    }

    @Override
    public void readParams(FriendlyByteBuf buf) {

    }
}
