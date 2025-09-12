package BlockPower.KeyBindings;

import BlockPower.ModItems.ModItems;
import BlockPower.ModMessages.C2SPacket.ChangeMinerStatePacket_C2S;
import BlockPower.ModMessages.SkillC2SPacket.AirJumpPacket_C2S;
import BlockPower.ModMessages.SkillC2SPacket.DashSkillPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.Skills.*;
import BlockPower.Util.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
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

    private static final TaskManager taskManager = TaskManager.getInstance(true);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        if (KeyBindings.MINER_MODE.consumeClick()) {
            LOGGER.info("MINER_MODE key triggered");
            ModMessages.sendToServer(new ChangeMinerStatePacket_C2S());
        }

        if (KeyBindings.MINECART_RUSH.consumeClick()) {
            LOGGER.info("MINECART_RUSH key triggered");
            SkillTrigger.triggerSkill(new RushMinecartSkill());
        }

        if (KeyBindings.DROP_ANVIL.consumeClick()) {
            LOGGER.info("DROP_ANVIL key triggered");
            SkillTrigger.triggerSkill(new DropAnvilSkill());
        }

        if (KeyBindings.CUSTOM_SPACE.consumeClick()) {
            String result = "";
            if (!localPlayer.onGround()) {
                if (localPlayer.input.up) {
                    result = "w";
                    ModMessages.sendToServer(new AirJumpPacket_C2S("w"));
                } else {
                    ModMessages.sendToServer(new AirJumpPacket_C2S(""));
                }
                LOGGER.info("CUSTOM_SPACE key triggered:{}", result);
            }
        }

        if (KeyBindings.DASH.consumeClick()) {
            Input playerInput = localPlayer.input;
            String result = "w";
            if (playerInput.left) {
                result = "a";
                ModMessages.sendToServer(new DashSkillPacket_C2S("a"));
            } else if (playerInput.right) {
                result = "d";
                ModMessages.sendToServer(new DashSkillPacket_C2S("d"));
            } else if (playerInput.down) {
                result = "s";
                ModMessages.sendToServer(new DashSkillPacket_C2S("s"));
            } else {
                ModMessages.sendToServer(new DashSkillPacket_C2S("w"));
            }
            LOGGER.info("Client DASH key triggered:{}", result);
        }

        if (KeyBindings.PLACE_BLOCK.consumeClick()) {
            if (localPlayer.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) return;
            LOGGER.info("PLACE_BLOCK key triggered");
            SkillTrigger.triggerSkill(new PlaceBlockSkill());
        }

        if (KeyBindings.LAUNCHER_SWING.consumeClick()) {
            if (localPlayer.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) return;
            if (localPlayer.getXRot() >= -25.0F) return;//玩家抬头角度大于25度时才会触发LauncherSwing
            taskManager.runOnceWithCooldown(localPlayer, "LAUNCHER_SWING", 7, () -> {
                LOGGER.info("LAUNCHER_SWING key triggered");
                SkillTrigger.triggerSkill(new LauncherSwingSkill());
            });
        }

    }
}
