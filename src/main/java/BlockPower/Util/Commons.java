package BlockPower.Util;

import BlockPower.Util.Timer.TickTimer;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static BlockPower.ModEffects.GlobalEffectHandler.cloudParticleTimers;

public class Commons {
    private static final Random r = new Random();

    private static final TimerManager timerManager = TimerManager.getInstance(false);
    private static final TaskManager taskManager = TaskManager.getInstance(false);

    /**
     * 检测半径内的非技能释放者的LivingEntity
     *
     * @param mainEntity 释放技能的实体
     * @param radius     检测半径
     * @return 半径内的非技能释放者和非自身LivingEntity列表
     */
    public static List<Entity> detectEntity(@NotNull Entity mainEntity, double radius, Player blacklist) {
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
     * @param mainEntity     释放技能的实体
     * @param effectedEntity 被击退的实体
     * @param strength       击退强度
     */
    public static void knockBackEntity(@NotNull Entity mainEntity, List<Entity> effectedEntity, double strength) {
        if (effectedEntity == null || effectedEntity.isEmpty()) return;
        for (Entity entity : effectedEntity) {
            if (entity.isRemoved()) continue;
            Vec3 knockbackVector = entity.position().subtract(mainEntity.position()).normalize();
            entity.setDeltaMovement(mainEntity.getDeltaMovement().add(
                    knockbackVector.x * strength,
                    1 * strength,
                    knockbackVector.z * strength
            ));
        }
    }

    /**
     * 向上击退实体
     *
     * @param mainEntity     释放技能的实体
     * @param effectedEntity 被击退的实体
     * @param strength       击退强度
     */
    public static void knockBackEntityUp(@NotNull Entity mainEntity, List<Entity> effectedEntity, double strength) {
        if (effectedEntity.isEmpty()) return;
        for (Entity entity : effectedEntity) {
            if (entity.isRemoved()) continue;
            Vec3 mainEntityLookAngle = mainEntity.getLookAngle().normalize().scale(0.05);
            entity.setDeltaMovement(mainEntityLookAngle.x, 1 * strength, mainEntityLookAngle.z);
        }
    }

    /**
     * 对半径内的实体造成伤害并击退
     *
     * @param mainEntity   释放技能的实体
     * @param skillUser    释放技能的玩家
     * @param damage       伤害值
     * @param detectRadius 检测半径
     * @param soundEvent   音效
     * @return 半径内的实体列表
     */
    public static List<Entity> applyDamage(@NotNull Entity mainEntity, Player skillUser, float damage, double detectRadius, SoundEvent soundEvent) {
        List<Entity> entities = detectEntity(mainEntity, detectRadius, skillUser);
        if (!entities.isEmpty()) {
            entities.forEach(entity -> {
                entity.hurt(mainEntity.level().damageSources().mobAttack(skillUser), damage);
                //为每个被击中的实体启动粒子计时器
                cloudParticleTimers.put(entity, new TickTimer(40,false));
                if (!mainEntity.level().isClientSide) {
                    //限制5tick内最多播放3次声音
                    taskManager.runTimesWithCooldown(mainEntity, "play_sound", 3, 5, () ->
                            mainEntity.level().playSound(null,
                                    mainEntity.getX(), mainEntity.getY(), mainEntity.getZ(),
                                    soundEvent, SoundSource.PLAYERS, 5f, r.nextFloat(0.2f) + 0.9f));
                }
            });
        }
        return entities;
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

    /**
     * 检查服务器玩家是否为游戏模式 spectator 或 creative
     *
     * @param player 服务器玩家
     */
    public static boolean isSpectatorOrCreativeMode(ServerPlayer player) {
        GameType gameType = player.gameMode.getGameModeForPlayer();
        return gameType == GameType.SPECTATOR || gameType == GameType.CREATIVE;
    }

}
