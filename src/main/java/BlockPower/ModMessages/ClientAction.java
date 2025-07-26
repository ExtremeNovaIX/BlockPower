package BlockPower.ModMessages;

import BlockPower.DTO.ActionPacket;
import BlockPower.DTO.S2C.PlaySoundData;
import BlockPower.DTO.S2C.ShakeData;

//这个枚举定义了所有可以从服务端发送到客户端并让客户端执行的动作指令
public enum ClientAction {
    @ActionPacket(ShakeData.class)
    SHAKE,
    @ActionPacket(PlaySoundData.class)
    PLAY_SOUND,
}
