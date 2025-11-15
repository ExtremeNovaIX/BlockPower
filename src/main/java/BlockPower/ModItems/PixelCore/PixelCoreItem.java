package BlockPower.ModItems.PixelCore;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Util.Commons;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PixelCoreItem extends Item {
    public PixelCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pStack.getCapability(ModCapabilities.PIXEL_CORE_LEVEL).ifPresent(coreLevel -> {
            pTooltipComponents.add(Component.translatable("tooltip.blockpower.pixel_core.level", coreLevel.getLevel()).withStyle(ChatFormatting.GRAY));
        });
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
