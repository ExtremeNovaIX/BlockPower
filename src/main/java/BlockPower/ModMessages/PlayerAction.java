package BlockPower.ModMessages;

import BlockPower.DTO.ActionPacket;
import BlockPower.DTO.C2S.MinecartData;

// 这个枚举定义了所有可以从客户端发送到服务端的玩家动作指令
public enum PlayerAction {
    @ActionPacket(MinecartData.class)
    MINECART_RUSH,

}