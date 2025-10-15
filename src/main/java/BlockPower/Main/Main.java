package BlockPower.Main;

import BlockPower.ModBlocks.ModBlocks;
import BlockPower.ModBlocks.ModEntityBlocks;
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
    //TODO 修复铁砧技能放出失败（如在地上放置铁砧导致铁砧无法放出）导致技能锁死锁问题
    //TODO 为技能实体类加一个unlock方法，用于标识技能结束时调用，用于解锁技能

    public static final String MOD_ID = "blockpower";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Main() {
        printWelcome();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //注册事件总线
        ModSounds.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntityBlocks.BLOCK_ENTITIES.register(modEventBus);
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
