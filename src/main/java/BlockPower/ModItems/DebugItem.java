package BlockPower.ModItems;

import BlockPower.Capability.ModCapabilities;
import BlockPower.Util.Commons;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DebugItem extends Item {
    public static final Logger LOGGER = LoggerFactory.getLogger(DebugItem.class);

    public DebugItem(Properties properties) {
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
        AABB area = new AABB(player.blockPosition()).inflate(11);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(knockbackValue -> {
                double value = knockbackValue.getKnockbackValue();
                Commons.sendDebugMessage(player, entity.getName().getString() + " Knockback: " + value);
            });
        }
    }
}
