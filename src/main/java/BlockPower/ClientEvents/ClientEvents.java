package BlockPower.ClientEvents;

import BlockPower.Entities.DropAnvil.DropAnvilRenderer;
import BlockPower.Entities.RushMinecart.FakeRailRenderer;
import BlockPower.Entities.ModEntities;
import BlockPower.Entities.RushMinecart.RushMinecartRenderer;
import BlockPower.KeyBindings.KeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
public class ClientEvents {

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

    @SubscribeEvent
    public static void onRenderRegister(final EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntities.FAKE_RAIL_ENTITY.get(), FakeRailRenderer::new);
        EntityRenderers.register(ModEntities.RUSH_MINECART.get(), RushMinecartRenderer::new);
        EntityRenderers.register(ModEntities.DROP_ANVIL.get(), DropAnvilRenderer::new);
    }
}
