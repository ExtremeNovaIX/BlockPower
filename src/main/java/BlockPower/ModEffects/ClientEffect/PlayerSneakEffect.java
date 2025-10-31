package BlockPower.ModEffects.ClientEffect;

import BlockPower.ModEffects.ModEffectManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;

//TODO 修复潜行效果自动跨端发送不成功问题
public class PlayerSneakEffect implements IClientTickBasedEffect {
    private boolean isPlayerSneak;

    public PlayerSneakEffect() {
        this.isPlayerSneak = true;
    }

    public PlayerSneakEffect(FriendlyByteBuf buf) {
        this.isPlayerSneak = true;
    }

    public static void handlePlayerSneakEffect(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        // 如果本地玩家没有PlayerSneakEffect效果，则不处理
        if (ModEffectManager.getEntityEffect(player, PlayerSneakEffect.class).isEmpty()) return;

        PlayerModel<?> model = event.getRenderer().getModel();
        // 强行设置为潜行姿势
        player.setPose(Pose.CROUCHING);
        model.crouching = true;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0, 0.75F, 0);
        poseStack.popPose();
    }

    @Override
    public void tick() {
        // 获取本地玩家，检测本地玩家按键输入
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;
        // 如果本地玩家没有PlayerSneakEffect效果，则不处理
        if (ModEffectManager.getEntityEffect(localPlayer, PlayerSneakEffect.class).isEmpty()) return;

        // 如果本地玩家在列表中，并且他按下了Shift键，则标记isPlayerSneak为false
        if (localPlayer.isShiftKeyDown()) {
            isPlayerSneak = false;
        }
    }

    @Override
    public boolean isFinished() {
        return !isPlayerSneak;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {

    }

    @Override
    public ClientEffectEnum getType() {
        return ClientEffectEnum.PLAYER_SNEAK;
    }
}