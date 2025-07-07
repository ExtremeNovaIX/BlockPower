package BlockPower.KeyBindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

public class KeyBindings {
    public static final String BLOCKPOWER_KEY = "key.blockpower";

    public static final KeyMapping MINECART_RUSH = new KeyMapping(
            "key.blockpower.minecart_rush",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,//按键绑定
            BLOCKPOWER_KEY
    );


}
