package BlockPower.Util.Gson;

import BlockPower.DTO.ActionData;
import BlockPower.DTO.C2S.MinecartData;
import BlockPower.DTO.S2C.ShakeData;
import BlockPower.ModMessages.PlayerAction;
import BlockPower.ModMessages.ServerAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModGson {
    private static final Gson GSON_INSTANCE;

    static {
        final RuntimeTypeAdapterFactory<ActionData> typeFactory = RuntimeTypeAdapterFactory
                .of(ActionData.class, "actionType")
                // 注册S2C类型
                .registerSubtype(ShakeData.class, ServerAction.SHAKE.name())
                // 注册C2S类型
                .registerSubtype(MinecartData.class, PlayerAction.MINECART_RUSH.name());

        GSON_INSTANCE = new GsonBuilder()
                .registerTypeAdapterFactory(typeFactory)
                .create();
    }

    public static Gson getInstance() {
        return GSON_INSTANCE;
    }
}