package BlockPower.Skills.MinerState.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientMinerState {
    private static boolean isMinerMode = false;

    public static void setMinerState(boolean state) {
        isMinerMode = state;
    }

    public static boolean isMinerMode() {
        return isMinerMode;
    }
}