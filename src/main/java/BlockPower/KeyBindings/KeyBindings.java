package BlockPower.KeyBindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindings {
    public static final String BLOCKPOWER_KEY = "key.blockpower";

    public static final KeyMapping MINER_MODE = new KeyMapping(
            "key.blockpower.miner_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_TAB,//按键绑定
            BLOCKPOWER_KEY
    );

    public static final KeyMapping MINECART_RUSH = new KeyMapping(
            "key.blockpower.minecart_rush",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,//按键绑定
            BLOCKPOWER_KEY
    );

    public static final KeyMapping DROP_ANVIL = new KeyMapping(
            "key.blockpower.drop_anvil",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F,//按键绑定
            BLOCKPOWER_KEY
    );

    public static final KeyMapping CUSTOM_SPACE = new KeyMapping(
            "key.blockpower.custom_space",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_SPACE,
            BLOCKPOWER_KEY
    );

    public static final KeyMapping DASH = new KeyMapping(
            "key.blockpower.dash",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL,
            BLOCKPOWER_KEY
    );

    public static final KeyMapping PLACE_BLOCK = new KeyMapping(
            "key.blockpower.place_block",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            BLOCKPOWER_KEY
    );

    public static final KeyMapping LAUNCHER_SWING = new KeyMapping(
            "key.blockpower.launcher_swing",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_LEFT,
            BLOCKPOWER_KEY
    );


}
