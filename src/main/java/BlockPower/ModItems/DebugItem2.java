package BlockPower.ModItems;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Util.Commons;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DebugItem2 extends Item {
    public static final Logger LOGGER = LoggerFactory.getLogger(DebugItem2.class);

    public DebugItem2(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            testServerMethod(player);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void testServerMethod(Player player) {
        List<Entity> entities = Commons.aabbDetectEntity(player, 11, player);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(knockbackValue -> {
                    knockbackValue.addKBPercent(10);
                    double value = knockbackValue.getKBPercent();
                    Commons.sendDebugMessage(player, entity.getName().getString() + " New Knockback: " + value);
                });
            }
        }
    }
}
