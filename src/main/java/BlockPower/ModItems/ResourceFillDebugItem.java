package BlockPower.ModItems;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Skills.MinerState.server.ResourceType;
import BlockPower.Util.Commons;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ResourceFillDebugItem extends Item {
    public ResourceFillDebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ModCapabilities.PLAYER_MINER_STATE).ifPresent(minerState -> {
                // 获取内部的资源Map
                Map<ResourceType, Double> resourceCounts = minerState.getResourceCounts();

                // 填充所有资源到最大上限
                for (ResourceType type : ResourceType.values()) {
                    resourceCounts.put(type, (double) type.getMaxAmount());
                }
                
                // 使用Capability的方法来设置和同步数据
                minerState.setResourceCounts(resourceCounts);
                minerState.syncToClient(serverPlayer);

                Commons.sendDebugMessage(player, "已充满所有资源");
            });
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
