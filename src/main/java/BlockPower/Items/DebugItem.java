package BlockPower.Items;

import BlockPower.Main.Main;

import BlockPower.ModSounds.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugItem extends Item {
    public static final Logger LOGGER = LoggerFactory.getLogger(DebugItem.class);
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockPos pos = player.getOnPos();

        if (!level.isClientSide) {
            Main.sendDebugMessage(player, "Server:调试物品使用于位置: " + pos.toShortString());
            testServerMethod(player);
        }else{
            Main.sendDebugMessage(player, "Client:调试物品使用于位置: " + pos.toShortString());
            testClientMethod(player);
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void testServerMethod(Player player) {
        LOGGER.info("testServerMethod");
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.ANVIL_SOUND.get(),
                SoundSource.PLAYERS, 1f,1f);
    }

    private void testClientMethod(Player player) {
        LOGGER.info("testClientMethod");

    }
}