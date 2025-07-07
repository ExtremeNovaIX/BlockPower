package BlockPower.Main;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static BlockPower.Main.Main.MOD_ID;

@Mod(MOD_ID)
public class Main {

    public static final String MOD_ID = "blockpower";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Main(IEventBus modEventBus) {

        //注册事件总线
        MinecraftForge.EVENT_BUS.register(this);

    }
}
