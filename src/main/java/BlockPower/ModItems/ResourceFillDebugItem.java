package BlockPower.ModItems;

import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.ResourceSyncPacket_S2C;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.MinerState.server.PlayerResourceManager;
import BlockPower.Util.Commons;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ResourceFillDebugItem extends Item {
    public ResourceFillDebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            // 获取玩家的资源数据
            var playerData = PlayerResourceManager.getInstance().getPlayerData(player);

            // 填充所有资源到最大上限
            for (AllResourceType type : AllResourceType.values()) {
                playerData.trueResourceCounts.put(type, (double) type.getMaxAmount());
            }
            if (player instanceof ServerPlayer) {
                ModMessages.sendToPlayer(new ResourceSyncPacket_S2C(playerData.trueResourceCounts), (ServerPlayer) player);
            }
            Commons.sendDebugMessage(player, "已填充满所有资源条！");
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}