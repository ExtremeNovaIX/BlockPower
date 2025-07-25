package BlockPower.Util.Gson;

import BlockPower.DTO.ActionData;
import BlockPower.DTO.ActionPacket;
import BlockPower.DTO.C2S.MinecartData;
import BlockPower.DTO.S2C.ShakeData;
import BlockPower.ModMessages.PlayerAction;
import BlockPower.ModMessages.ServerAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ModGson {
    private static final Gson GSON_INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger("ModGson");

    static {
        final RuntimeTypeAdapterFactory<ActionData> typeFactory = RuntimeTypeAdapterFactory
                .of(ActionData.class, "actionType");
        LOGGER.info("开始自动注册所有S2C动作");
        autoRegisterFromEnum(typeFactory, ServerAction.class);
        LOGGER.info("自动注册所有S2C动作完成");

        LOGGER.info("开始自动注册所有C2S动作");
        autoRegisterFromEnum(typeFactory, PlayerAction.class);
        LOGGER.info("自动注册所有C2S动作完成");

        GSON_INSTANCE = new GsonBuilder()
                .registerTypeAdapterFactory(typeFactory)
                .create();
    }

    public static Gson getInstance() {
        return GSON_INSTANCE;
    }

    private static void autoRegisterFromEnum(RuntimeTypeAdapterFactory<ActionData> factory, Class<? extends Enum<?>> enumClass) {
        for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
            try {
                //通过反射获取该枚举常量的字段定义
                Field field = enumClass.getField(enumConstant.name());
                //检查这个字段上是否有@ActionPacket注解
                if (field.isAnnotationPresent(ActionPacket.class)) {
                    LOGGER.info("自动注册:{}-{}", enumClass.getName(), enumConstant.name());
                    ActionPacket actionPacket = field.getAnnotation(ActionPacket.class);
                    //自动执行注册
                    factory.registerSubtype(actionPacket.value(), enumConstant.name());
                }
            } catch (NoSuchFieldException e) {
                LOGGER.error("自动注册失败:{}-{} 原因:{}", enumClass.getName(), enumConstant.name(), e.getMessage());
            }
        }
    }
}