package BlockPower.Util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Commons {
    /**
     * 检测半径内的非技能释放者的LivingEntity
     *
     * @param mainEntity 释放技能的实体
     * @param radius 检测半径
     * @return 半径内的非技能释放者和非自身LivingEntity列表
     */
    public static List<Entity> detectEntity(@NotNull Entity mainEntity, double radius, @NotNull Player blacklist) {
        //创建一个默认半径为radius的检测区域
        AABB detectionArea = new AABB(
                mainEntity.getX() - radius,
                mainEntity.getY() - radius,
                mainEntity.getZ() - radius,
                mainEntity.getX() + radius,
                mainEntity.getY() + radius,
                mainEntity.getZ() + radius
        );

        //获取半径内的非技能释放者的LivingEntity
        return mainEntity.level().getEntities(
                mainEntity,
                detectionArea,
                detectedEntity -> detectedEntity != mainEntity
                        && detectedEntity.distanceToSqr(mainEntity) <= radius * radius
                        && detectedEntity != blacklist
                        && detectedEntity instanceof LivingEntity
        );
    }

    /**
     * 击退实体
     *
     * @param mainEntity  释放技能的实体
     * @param effectedEntity  被击退的实体
     * @param strength 击退强度
     */
    public static void knockBackEntity(@NotNull Entity mainEntity, Entity effectedEntity, double strength) {
        if (effectedEntity == null || effectedEntity.isRemoved()) return;
        Vec3 knockbackVector = mainEntity.position().subtract(effectedEntity.position()).normalize();
        effectedEntity.setDeltaMovement(mainEntity.getDeltaMovement().add(
                knockbackVector.x * strength,
                1 * strength,
                knockbackVector.z * strength
        ));
    }

    /**
     * 发送调试信息到指定的玩家
     * 调试信息会以“[DEBUG]”开头，并且字体颜色为金色
     *
     * @param player  指定的玩家
     * @param message 要发送的调试信息
     */
    public static void sendDebugMessage(Player player, String message) {
        Component debugMessage = Component.literal("[DEBUG] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));
        player.sendSystemMessage(debugMessage);
    }

    /**
     * 向所有在线玩家广播消息
     * 广播消息会以"[DEBUG]"开头，字体颜色为金色
     *
     * @param message 要广播的消息内容
     */
    public static void broadcastMessage(String message) {
        Component broadcastMsg = Component.literal("[DEBUG] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));

        // 获取服务器实例并遍历所有玩家
        MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> {
                player.sendSystemMessage(broadcastMsg);
            });
        }
    }

}
