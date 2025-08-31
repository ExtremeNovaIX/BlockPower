package BlockPower.ModItems;

import BlockPower.Util.Commons;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
            Commons.sendDebugMessage(player, "Server:调试物品使用于位置: " + pos.toShortString());
            testServerMethod(player);
        }else{
            Commons.sendDebugMessage(player, "Client:调试物品使用于位置: " + pos.toShortString());
            testClientMethod(player);
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void testServerMethod(Player player) {
        LOGGER.info("testServerMethod");
        Vec3 position = player.getEyePosition();
        Level level = player.level();
        level.playSound(null,
                position.x(), position.y(), position.z(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                3F, 1F);
    }

    private void testClientMethod(Player player) {
        LOGGER.info("testClientMethod");

    }
}