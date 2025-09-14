package BlockPower.ModEvents.EffectEvents;

import BlockPower.Main.Main;
import BlockPower.ModEffects.UnBalanceEffect;
import BlockPower.Util.ModEffect.ModEffectManager;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnbalanceEffectEvents {
    //如果玩家有unBalanceEffect,则取消攻击事件
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            ModEffectManager.getEntityEffect(event.getEntity(), UnBalanceEffect.class).ifPresent(unBalanceEffect -> {
                event.setCanceled(true); //取消攻击事件
            });
        }
    }

    //如果玩家有unBalanceEffect,则取消物品使用事件
    @SubscribeEvent
    public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getEntity().level().isClientSide()) {
            ModEffectManager.getEntityEffect(event.getEntity(), UnBalanceEffect.class).ifPresent(unBalanceEffect -> {
                event.setCanceled(true); //取消物品使用事件
            });
        }
    }
}