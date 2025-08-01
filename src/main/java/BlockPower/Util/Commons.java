package BlockPower.Util;

import BlockPower.DTO.S2C.HitStopData;
import BlockPower.DTO.S2C.ShakeData;
import BlockPower.ModMessages.PlayerActionPacket_S2C;
import BlockPower.Util.HitStop.HitStopHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

import static BlockPower.ModMessages.ModMessages.sendToPlayer;

public class Commons {

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
     */
    public static void sendHitStop(int duration, ServerPlayer serverPlayer) {
        sendToPlayer(new PlayerActionPacket_S2C(new HitStopData(duration)), serverPlayer);
    }

}
