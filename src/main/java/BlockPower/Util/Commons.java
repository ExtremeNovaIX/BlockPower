package BlockPower.Util;

import BlockPower.Util.ModEffects.ServerEffect.CloudTrailEffect;
import BlockPower.Util.ModEffects.ServerEffect.UnBalanceEffect;
import BlockPower.ModItems.ModItems;
import BlockPower.Util.ModEffects.ModEffectManager;
import BlockPower.ModItems.PixelCore.PixelCoreSkillState;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Commons {
    private static final Random r = new Random();

    private static final TimerManager timerManager = TimerManager.getInstance(false);
    private static final TaskManager taskManager = TaskManager.getInstance(false);
    private static final Logger log = LoggerFactory.getLogger(Commons.class);

    /**
     * 检测半径内的非技能释放者的LivingEntity
     *
     * @param mainEntity 释放技能的实体
     * @param radius     检测半径
     * @return 半径内的非技能释放者和非自身LivingEntity列表
     */
    public static List<Entity> aabbDetectEntity(@NotNull Entity mainEntity, double radius, Player blacklist) {
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
     * 检测半径内的非技能释放者的LivingEntity
     *
     * @param pos    检测位置
     * @param radius 检测半径
     * @return 半径内的非技能释放者和非自身LivingEntity列表
     */
    public static List<Entity> aabbDetectEntity(@NotNull Vec3 pos, Level level, double radius, Player blacklist) {
        //创建一个默认半径为radius的检测区域
        AABB detectionArea = new AABB(
                pos.x - radius,
                pos.y - radius,
                pos.z - radius,
                pos.x + radius,
                pos.y + radius,
                pos.z + radius
        );

        //获取半径内的非技能释放者的LivingEntity
        return level.getEntities(
                blacklist,
                detectionArea,
                detectedEntity -> detectedEntity != null
                        && detectedEntity.distanceToSqr(pos) <= radius * radius
                        && detectedEntity instanceof LivingEntity
        );
    }

    /**
     * 射线式检测实体
     *
     * @param mainEntity  发出射线的实体
     * @param radius      射线半径
     * @param blacklist   检测中要忽略的实体列表
     * @param throughWall 射线是否穿透方块
     * @param length      射线的最大长度
     * @return 射线路径上检测到的实体列表
     */
    public static List<Entity> rayDetectEntity(@NotNull Entity mainEntity, double radius, @Nullable List<Entity> blacklist, boolean throughWall, double length) {
        return rayDetectEntity(mainEntity.level(), mainEntity.getEyePosition(), mainEntity.getLookAngle(), radius, blacklist, throughWall, length, mainEntity);
    }

    /**
     * 射线式检测实体
     *
     * @param level       进行检测的世界
     * @param startPos    射线起点
     * @param direction   射线方向 (应为单位向量)
     * @param radius      射线半径
     * @param blacklist   检测中要忽略的实体列表
     * @param throughWall 射线是否穿透方块
     * @param length      射线的最大长度
     * @param owner       射线的所有者，用于忽略自身和进行方块碰撞检测
     * @return 射线路径上检测到的实体列表
     */
    public static List<Entity> rayDetectEntity(@NotNull Level level, @NotNull Vec3 startPos, @NotNull Vec3 direction, double radius, @Nullable List<Entity> blacklist, boolean throughWall, double length, @Nullable Entity owner) {
        Vec3 endPos = startPos.add(direction.normalize().scale(length));

        if (!throughWall) {
            ClipContext clipContext = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner);
            BlockHitResult blockHitResult = level.clip(clipContext);
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                endPos = blockHitResult.getLocation();
            }
        }
        AABB searchBox = new AABB(startPos, endPos).inflate(radius);
        List<Entity> potentialEntities = level.getEntities(
                owner,
                searchBox,
                entity -> entity instanceof LivingEntity &&
                        entity.isPickable() &&
                        (owner == null || !entity.equals(owner)) &&
                        (blacklist == null || !blacklist.contains(entity))
        );

        List<Entity> detectedEntities = new ArrayList<>();
        for (Entity entity : potentialEntities) {
            AABB entityBoundingBox = entity.getBoundingBox().inflate(radius);
            // 首先检查起点是否就在实体的碰撞箱内，覆盖近距离检测盲区
            if (entityBoundingBox.contains(startPos)) {
                detectedEntities.add(entity);
                continue;
            }
            Optional<Vec3> intersection = entityBoundingBox.clip(startPos, endPos);
            if (intersection.isPresent()) {
                detectedEntities.add(entity);
            }
        }

        detectedEntities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(startPos)));

        return detectedEntities;
    }


    /**
     * 击退实体
     *
     * @param mainEntity         释放技能的实体
     * @param effectedEntity     被击退的实体
     * @param verticalStrength   垂直击退强度
     * @param horizontalStrength 水平击退强度
     */
    public static void knockBackEntity(@NotNull Entity mainEntity, List<Entity> effectedEntity, double verticalStrength, double horizontalStrength) {
        if (effectedEntity.isEmpty()) return;
        for (Entity entity : effectedEntity) {
            if (entity.isRemoved()) continue;
            knockBackEntity(mainEntity, entity, verticalStrength, horizontalStrength);
        }
    }

    /**
     * 击退实体
     *
     * @param mainEntity         释放技能的实体
     * @param effectedEntity     被击退的实体
     * @param verticalStrength   垂直击退强度
     * @param horizontalStrength 水平击退强度
     */
    public static void knockBackEntity(@NotNull Entity mainEntity, Entity effectedEntity, double verticalStrength, double horizontalStrength) {
        if (effectedEntity == null || effectedEntity.isRemoved()) return;
        if (verticalStrength == 0 && horizontalStrength == 0) return;
        Vec3 kbForce = effectedEntity.position().subtract(mainEntity.position()).normalize();
        Vec3 currentVelocity = effectedEntity.getDeltaMovement();

        double newVelX = currentVelocity.x + kbForce.x * horizontalStrength;
        double newVelZ = currentVelocity.z + kbForce.z * horizontalStrength;
        double newVelY = currentVelocity.y / 2.0 + verticalStrength;

        effectedEntity.setDeltaMovement(newVelX, newVelY, newVelZ);
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
        double horizontalRepelStrength = 0.2; // 水平推开的力度

        // 获取玩家的水平朝向向量（忽略Y轴的抬头或低头）
        Vec3 lookVec = mainEntity.getLookAngle();
        Vec3 horizontalLook = new Vec3(lookVec.x, 0.0, lookVec.z).normalize();

        // 玩家朝向的相反方向的向量
        Vec3 pushBackVec = horizontalLook.scale(horizontalRepelStrength);

        for (Entity entity : effectedEntity) {
            if (entity.isRemoved()) continue;

            // 将实体当前的速度与新向量组合
            Vec3 existingMotion = entity.getDeltaMovement();

            entity.setDeltaMovement(
                    existingMotion.x + pushBackVec.x,
                    strength,
                    existingMotion.z + pushBackVec.z
            );
        }
    }

    /**
     * 播放音效并设置冷却时间
     *
     * @param mainEntity 释放技能的实体
     * @param soundEvent 要播放的音效事件
     * @param volume     音效音量
     * @param cooldown   冷却时间
     */
    public static void playSoundWithCooldown(@NotNull Entity mainEntity, SoundEvent soundEvent, float volume, int cooldown) {
        if (mainEntity.level().isClientSide) return;
        taskManager.runOnceWithCooldown(mainEntity, "playSoundWithCooldown", cooldown, () -> {
            mainEntity.level().playSound(null,
                    mainEntity.getX(), mainEntity.getY(), mainEntity.getZ(),
                    soundEvent, SoundSource.PLAYERS, volume, r.nextFloat(0.4f) + 0.8f);
        });
    }

    public static boolean applyDamage(@NotNull Entity mainEntity, Player skillUser, Entity detectedEntity, double baseDamage) {
        if (detectedEntity.isRemoved()) return false;
        double finalDamage = KBUtils.calculateSkillDamage(detectedEntity, baseDamage);
        detectedEntity.hurt(mainEntity.level().damageSources().mobAttack(skillUser), (float) finalDamage);
        ModEffectManager.addEffect(detectedEntity, new UnBalanceEffect(detectedEntity, 9));
        //为每个被击中的实体启动粒子计时器
        ModEffectManager.addEffect(detectedEntity, new CloudTrailEffect(detectedEntity, 40));
        return true;
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

    public static void changePixelCoreNBT(Player player, @Nullable PixelCoreSkillState skillState, @Nullable Float toolType, @Nullable Float pixelCoreLevel) {
        if (player.level().isClientSide) return;
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() != ModItems.PIXEL_CORE.get()) return;
        CompoundTag NBT = mainHandItem.getOrCreateTag();
        NBT.putFloat("skill_state", Objects.requireNonNullElse(skillState, PixelCoreSkillState.DEFAULT).getId());
        NBT.putFloat("tool_type", Objects.requireNonNullElse(toolType, -1.0F));
        NBT.putFloat("pixel_core_level", Objects.requireNonNullElse(pixelCoreLevel, -1.0F));
        mainHandItem.setTag(NBT);
    }

    public static void playSoundWithRandomPitch(ServerPlayer player, SoundEvent soundEvent) {
        if (player.level().isClientSide) return;
        player.level().playSound(null,
                player.getX(), player.getY(), player.getZ(),
                soundEvent, SoundSource.PLAYERS, 1f, r.nextFloat(0.2f) + 0.9f);
    }

}
