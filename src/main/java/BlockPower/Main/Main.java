package BlockPower.Main;

import BlockPower.Entities.ModEntities;
import BlockPower.Items.ModItems;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModSounds.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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

    //TODO 完成全局的受伤管理计时器，防止反复触发效果
    //TODO 修复有时音效播放异常问题
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
}
