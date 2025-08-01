package BlockPower.ModMessages;

import BlockPower.DTO.ActionPacket;
import BlockPower.DTO.C2S.DropAnvilData;
import BlockPower.DTO.C2S.MinecartData;

//这个枚举定义了所有可以从客户端发送到服务端并让服务端执行的动作指令
public enum ServerAction {
    @ActionPacket(MinecartData.class)
    MINECART_RUSH,
    @ActionPacket(DropAnvilData.class)
    DROP_ANVIL,
}