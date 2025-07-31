package BlockPower.ModMessages;

import BlockPower.DTO.ActionPacket;
import BlockPower.DTO.S2C.CrossStarRenderData;
import BlockPower.DTO.S2C.HitStopData;
import BlockPower.DTO.S2C.ShakeData;

//这个枚举定义了所有可以从服务端发送到客户端并让客户端执行的动作指令
public enum ClientAction {
    @ActionPacket(ShakeData.class)
    SHAKE,
    @ActionPacket(HitStopData.class)
    HIT_STOP,
    @ActionPacket(CrossStarRenderData.class)
    CROSS_STAR,
}
