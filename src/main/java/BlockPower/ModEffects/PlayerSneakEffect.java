package BlockPower.ModEffects;

import BlockPower.Main.Main;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerSneakEffect {
    private static final Set<UUID> playersToForceSneak = new HashSet<>();

    public static void start(Player player) {
        playersToForceSneak.add(player.getUUID());
    }

    public static void end(Player player) {
        playersToForceSneak.remove(player.getUUID());
    }

    /**
     * 监听玩家渲染前的事件。
     * 如果强制潜行状态开启，就在这里修改玩家的姿势。
     */
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUUID();
        // 检查当前渲染的玩家是否在列表中
        if (playersToForceSneak.contains(playerUUID)) {
            PlayerModel<?> model = event.getRenderer().getModel();
            // 强行设置为潜行姿势
            player.setPose(Pose.CROUCHING);
            model.crouching = true;

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(0, 0.75F, 0);
            poseStack.popPose();
        }
    }

    /**
     * 监听客户端的游戏刻事件。
     * 在这里检查玩家是否按下了Shift键来解除强制潜行状态。
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (playersToForceSneak.isEmpty()) return;

            // 获取本地玩家，检测本地玩家按键输入
            Player localPlayer = Minecraft.getInstance().player;
            if (localPlayer == null) return;

            UUID localPlayerUUID = localPlayer.getUUID();

            // 如果本地玩家在列表中，并且他按下了Shift键，则将他从列表中移除
            if (playersToForceSneak.contains(localPlayerUUID) && localPlayer.isShiftKeyDown()) {
                end(localPlayer);
            }
        }
    }
}