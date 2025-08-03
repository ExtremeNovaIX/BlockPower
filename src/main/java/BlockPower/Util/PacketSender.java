package BlockPower.Util;

import BlockPower.DTO.S2C.HitStopData;
import BlockPower.DTO.S2C.ShakeData;
import BlockPower.Effects.GlobalEffectHandler;
import BlockPower.ModMessages.PlayerActionPacket_S2C;
import BlockPower.Util.Timer.TickTimer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.WeakHashMap;

import static BlockPower.ModMessages.ModMessages.sendToPlayer;

public class PacketSender {

    /**
     * 触发玩家屏幕震动
     *
     * @param duration     震动持续时间
     * @param strength     震动强度
     * @param serverPlayer 目标玩家
     */
    public static void sendScreenShake(int duration, float strength, ServerPlayer serverPlayer) {
        sendToPlayer(new PlayerActionPacket_S2C(new ShakeData(duration, strength)), serverPlayer);
    }

    /**
     * 触发卡帧效果
     *
     * @param duration     卡帧持续时间
     * @param serverPlayer 目标玩家
     * @param skillEntity  触发卡帧的技能实体
     */
    public static void sendHitStop(int duration, ServerPlayer serverPlayer, @Nullable Entity skillEntity) {
        if (skillEntity != null) {
            Map.Entry<Vec3, TickTimer> entry = new AbstractMap.SimpleEntry<>(skillEntity.getDeltaMovement(), new TickTimer(duration));
            GlobalEffectHandler.hitStopTimers.put(skillEntity, entry);
            skillEntity.setDeltaMovement(Vec3.ZERO);
        }
        sendToPlayer(new PlayerActionPacket_S2C(new HitStopData(duration)), serverPlayer);
    }

}
