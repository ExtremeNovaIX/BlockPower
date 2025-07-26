package BlockPower.Util;

import BlockPower.DTO.S2C.PlaySoundData;
import BlockPower.DTO.S2C.ShakeData;
import BlockPower.ModMessages.PlayerActionPacket_S2C;
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
     * 播放音效
     *
     * @param serverPlayer 目标玩家
     * @param soundEvent   音效资源路径
     * @param volume       音效音量
     * @param pitch        音效音调
     */
    public static void sendPlaySound(ServerPlayer serverPlayer, SoundEvent soundEvent, float volume, float pitch) {
        sendToPlayer(new PlayerActionPacket_S2C(new PlaySoundData(serverPlayer, soundEvent, volume, pitch)), serverPlayer);
    }
}
