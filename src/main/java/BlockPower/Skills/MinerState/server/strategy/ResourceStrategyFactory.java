package BlockPower.Skills.MinerState.server.strategy;

import BlockPower.Skills.MinerState.server.strategy.concrete.*;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public class ResourceStrategyFactory {
    public static ResourceGenerationStrategy getStrategy(ItemStack tool) {
        if (tool.getItem() instanceof DiggerItem diggerItem) {
            Tier tier = diggerItem.getTier();
            int tierLevel = tier.getLevel();

            //对于等级过高的工具，统一使用MaxTierStrategy
            if (tierLevel >= 4) {
                return new MaxTierStrategy();
            }

            //特殊区分木头和金工具
            if (tier == Tiers.WOOD) {
                return new WoodTierStrategy();
            } else if (tier == Tiers.GOLD) {
                return new GoldTierStrategy();
            }

            //其他工具按等级返回
            return switch (tierLevel) {
                case 1 -> new StoneTierStrategy();
                case 2 -> new IronTierStrategy();
                case 3 -> new DiamondTierStrategy();
                default -> new DefaultStrategy();
            };
        }

        return new DefaultStrategy();
    }
}
