package BlockPower.Main;

import BlockPower.ModEntities.ModEntities;
import BlockPower.ModItems.ModItems;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModSounds.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static BlockPower.Main.Main.MOD_ID;

@Mod(MOD_ID)
public class Main {

    public static final String MOD_ID = "blockpower";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //TODO 完成全局的受伤管理计时器，防止反复触发效果
    public Main() {
        printWelcome();
        //不要管这个报错，它是正常的
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //注册事件总线
        ModSounds.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        ModMessages.register();
        LOGGER.debug("BlockPower register over,have fun!");
    }

    public static void printWelcome() {
        String welcomeArt =
                        "░░░░░░▒▒▒▒▒▒▒▒▓▒▒▓▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░░▒▒▒▒░▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒░▒▒▓▓▓▒▒▒▒▒▒▒" +
                        "░░░░░░░▒▒▒▒▓▓▓▓▒▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▒▒▒▒▒▒▒▒" +
                        "░░░░░░░▒▒▓▒▒▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▒▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░░░░▒▒▓▒▒▒▓█▓▒▒▒▒▒▒▒██▓▒▒▒▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░░░░▒▒▓▒░░▒▓░░░░░░░░░▒▒▒░▒▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░░░▒▒▒▒▒░░░░░░▒░░▒░░░░░░▒▒▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░▒▒▒▒▒▓▓▒▒░░▒▓▓▓▓▓▒▓▒▒░░▒▒▒▒▒▒▒▒▒▒▒▒" +
                        "░░░▒▒▒▓▓▒▓▒▒▒▒▒▒▓▓▓▓▓▓▓▓▓▓░░░░▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░▒▓▓▓▓▒▓▒▒▒▒▓▓▓▓▓▓▓▓▓▓▒░░░▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░░▒▒▓▓▒▒▒▒░▒▓▓▓▓████▓▓▒░░░▒▒▓▒▒▒▒▒▒▒" +
                        "░░░░░░░░▒▓▓▒▒▒▒▓▓▓▓▓████▓▒░░░░▒▒▒▒▒▒▒▒▒▒" +
                        "░░░░░░░░░░▒▓▒▒░░▓▓▓▓▓██▓▓▓▒▒▒▒▓▒▒▒▒▒▒▒▒▒" +
                        "░░░░░░░░░▒▒▒▒▒▓█▓▓██████████▓▓▓▒▓▓▒▒▒▒▒" +
                        "░░░░░░░░▒▒▒▓▓▓▓▓▓▓███▓▓▓███▓▓▓▓▓▓▓▓▓▓▓▒▒" +
                        "░░░░░░░▒▒▒▒▓▓▓▓▓▓▒▒▒▒▒▓▓▓█▓▓▓▓▓██▓▓▓▓▓▓▓" +
                        "░░░░░░░▒▒▒▒▓▓▓▓▓▓▓▒▒▒▒▓▓▓▓▓▒▓███████████" +
                        "░░░░░░▒▒▒▒▒▒▒▒▒▒▓▓▒▒▒▒▓▓▓▓▒▒▒██▓▓▓██████" +
                        "░░░░▒▒▓▓▒▒▒▒▒▒▒▒▓▓▒▒▒▒▓▓▓▓▓▒▒███████████" +
                        "▒▒▒▓▓▓▓▒▒▒▒▒▒▒▒▓▓▓▒▒▒▒▓▓▓▒▒▓▓▓▓▓▓▒▓▓▓▓▓▓";

        LOGGER.info("Hi, I'm BlockPower, a mod that adds some new skills to Minecraft.");

        for (int i = 0; i < welcomeArt.length(); i += 40) {
            String line = welcomeArt.substring(i, Math.min(i + 40, welcomeArt.length()));
            LOGGER.info(line);
        }

    }
}
