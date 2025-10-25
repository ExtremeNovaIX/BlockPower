package BlockPower.ModItems;

import BlockPower.ModMessages.ComboSkillPacket.ComboStandbyPacket_S2C;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Util.Commons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugItem2 extends Item {
    public static final Logger LOGGER = LoggerFactory.getLogger(DebugItem2.class);

    public DebugItem2(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockPos pos = player.getOnPos();

        if (!level.isClientSide) {
            Commons.sendDebugMessage(player, "Server:调试物品使用于位置: " + pos.toShortString());
            testServerMethod(player);
        } else {
            Commons.sendDebugMessage(player, "Client:调试物品使用于位置: " + pos.toShortString());
            testClientMethod(player);
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void testServerMethod(Player player) {
        LOGGER.info("testServerMethod");
        ModMessages.sendToPlayer(new ComboStandbyPacket_S2C(ComboSkillType.TEST), (ServerPlayer) player);
    }

    private void testClientMethod(Player player) {
        LOGGER.info("testClientMethod");

    }
}