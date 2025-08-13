package BlockPower.KeyBindings;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static BlockPower.Main.Main.MOD_ID;

//客户端事件处理类
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyRegisterEvents {

    private static final Logger LOGGER = LogManager.getLogger("ClientEvents");

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        //自动读取KeyBindings并注册按键绑定
        for (Field field : KeyBindings.class.getFields()) {
            //检查字段是否是public static final
            if(Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                if(field.getType() == KeyMapping.class) {
                    try {
                        //自动注册按键绑定
                        event.register((KeyMapping) field.get(field));
                        LOGGER.info("Auto registered key binding: {}", field.getName());
                    } catch (Exception e) {
                        LOGGER.error("{}:Failed to register key binding: {}", e, field.getName());
                    }
                }
            }
        }
    }
}
