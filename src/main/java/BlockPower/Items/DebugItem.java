package BlockPower.Items;

import BlockPower.Main.Main;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.Commons;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DebugItem extends Item {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockPos pos = player.getOnPos();

        if (!level.isClientSide) {
            Main.sendDebugMessage(player, "调试物品使用于位置: " + pos.toShortString());
            testMethod(player);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void testMethod(Player player) {
        Commons.sendPlaySound((ServerPlayer) player, ModSounds.MINECART_CRASH.get(), 1.0f, 1.0f);
    }
}