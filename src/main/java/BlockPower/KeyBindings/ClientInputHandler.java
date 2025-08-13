package BlockPower.KeyBindings;

import BlockPower.ModMessages.C2SPacket.SpawnDropAnvilPacket_C2S;
import BlockPower.ModMessages.C2SPacket.SpawnRushMinecartPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static BlockPower.Main.Main.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientInputHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (KeyBindings.MINECART_RUSH.consumeClick()) {
                LOGGER.info("MINECART_RUSH key 触发!");
                ModMessages.sendToServer(new SpawnRushMinecartPacket_C2S());
            }
            if (KeyBindings.DROP_ANVIL.consumeClick()) {
                LOGGER.info("DROP_ANVIL key 触发!");
                ModMessages.sendToServer(new SpawnDropAnvilPacket_C2S());
            }
        }
    }
}
