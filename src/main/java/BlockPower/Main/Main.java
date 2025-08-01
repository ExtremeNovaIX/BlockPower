package BlockPower.Main;

import BlockPower.Entities.ModEntities;
import BlockPower.Items.ModItems;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModSounds.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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

    public Main() {
        //不要管这个报错，它是正常的
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //注册事件总线
        ModSounds.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        ModMessages.register();
    }

    /**
     * 发送调试信息到指定的玩家
     * 调试信息会以“[DEBUG]”开头，并且字体颜色为金色
     *
     * @param player  指定的玩家
     * @param message 要发送的调试信息
     */
    public static void sendDebugMessage(Player player, String message) {
        Component debugMessage = Component.literal("[DEBUG] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));
        player.sendSystemMessage(debugMessage);
    }
}
