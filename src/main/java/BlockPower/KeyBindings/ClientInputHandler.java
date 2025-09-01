package BlockPower.KeyBindings;

import BlockPower.ModMessages.C2SPacket.ChangeMinerStatePacket_C2S;
import BlockPower.ModMessages.C2SPacket.SkillPacket.SpawnDropAnvilPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.SkillTrigger.DropAnvilSkill;
import BlockPower.Skills.SkillTrigger.RushMinecartSkill;
import BlockPower.Skills.SkillTrigger.SkillTrigger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static BlockPower.Main.Main.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientInputHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (KeyBindings.MINER_MODE.consumeClick()) {
                LOGGER.info("MINER_MODE key 触发!");
                ModMessages.sendToServer(new ChangeMinerStatePacket_C2S());
            }
            if (KeyBindings.MINECART_RUSH.consumeClick()) {
                SkillTrigger.triggerSkill(new RushMinecartSkill());
            }
            if (KeyBindings.DROP_ANVIL.consumeClick()) {
                SkillTrigger.triggerSkill(new DropAnvilSkill());
            }
        }
    }
}
