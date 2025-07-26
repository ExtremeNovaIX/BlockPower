package BlockPower.Items;

import BlockPower.Main.Main;
import BlockPower.ModSounds.ModSounds;
import BlockPower.Util.Commons;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
            Main.sendDebugMessage(player, "Server:调试物品使用于位置: " + pos.toShortString());
            testServerMethod(player);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }else{
            testClientMethod(player);
            Main.sendDebugMessage(player, "Client:调试物品使用于位置: " + pos.toShortString());
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void testServerMethod(Player player) {
        Commons.sendPlaySound((ServerPlayer) player, SoundEvents.PLAYER_HURT, 2.0f, 1.0f);
        //ModSounds.MINECART_CRASH.get()
    }

    private void testClientMethod(Player player) {
//        if (Minecraft.getInstance().level != null) {
//            if (Minecraft.getInstance().level.isClientSide()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_HURT
                        , SoundSource.PLAYERS,
                        1f, 1f);
            

    }
}