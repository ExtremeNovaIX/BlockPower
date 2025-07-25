package BlockPower.ModMessages;

import BlockPower.DTO.ActionPacket;
import BlockPower.DTO.S2C.ShakeData;

public enum ServerAction {
    @ActionPacket(ShakeData.class)
    SHAKE,
}
