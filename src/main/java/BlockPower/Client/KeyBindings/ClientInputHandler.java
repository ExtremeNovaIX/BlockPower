package BlockPower.Client.KeyBindings;

import BlockPower.ModItems.ModItems;
import BlockPower.ModMessages.C2SPacket.ChangeMinerStatePacket_C2S;
import BlockPower.ModMessages.ComboSkillPacket.ComboTriggeredPacket_C2S;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.NormalSkillC2SPacket.NormalSkillPacket_C2S;
import BlockPower.ModMessages.NormalSkillC2SPacket.NormalSkillType;
import BlockPower.Skills.ComboSkills.ComboManager.Client.ClientComboData;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Skills.MinerState.client.ClientMinerState;
import BlockPower.Skills.NormalSkills.*;
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
            ModMessages.sendToServer(new ChangeMinerStatePacket_C2S());
        }

        if (KeyBindings.CUSTOM_SPACE.consumeClick()) {
            if (!localPlayer.onGround()) {
                if (localPlayer.input.up) {
                    ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.AIR_JUMP, new AirJumpSkill("w")));
                } else {
                    ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.AIR_JUMP, new AirJumpSkill("")));
                }
            }
        }

        if (KeyBindings.DASH.consumeClick()) {
            Input playerInput = localPlayer.input;
            if (playerInput.left) {
                ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.DASH, new DashSkill("a")));
            } else if (playerInput.right) {
                ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.DASH, new DashSkill("d")));
            } else if (playerInput.down) {
                ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.DASH, new DashSkill("s")));
            } else {
                ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.DASH, new DashSkill("w")));
            }
        }

        if (KeyBindings.MINECART_RUSH.consumeClick()) {
            // 挖掘状态下才能触发技能
            if (!ClientMinerState.isMinerMode()) return;
            ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.MINECART_RUSH, new RushMinecartSkill()));
        }

        if (KeyBindings.DROP_ANVIL.consumeClick()) {
            // 挖掘状态下才能触发技能
            if (!ClientMinerState.isMinerMode()) return;
            ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.DROP_ANVIL, new DropAnvilSkill()));
        }

        if (KeyBindings.PLACE_BLOCK.consumeClick()) {
            // 挖掘状态下才能触发技能
            if (!ClientMinerState.isMinerMode()) return;
            if (localPlayer.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) return;
            ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.PLACE_BLOCK, new PlaceBlockSkill()));
        }

        if (KeyBindings.LAUNCHER_SWING.consumeClick()) {
            // 挖掘状态下才能触发技能
            if (!ClientMinerState.isMinerMode()) return;
            if (localPlayer.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) return;
            if (localPlayer.getXRot() >= -25.0F) return;//玩家抬头角度大于25度时才会触发LauncherSwing
            taskManager.runOnceWithCooldown(localPlayer, "LAUNCHER_SWING", 9, () -> {
                ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.LAUNCHER_SWING, new LauncherSwingSkill()));
            });
        }

        if (KeyBindings.COMBO_SKILL.consumeClick()) {
            // 挖掘状态下才能触发技能
            if (!ClientMinerState.isMinerMode()) return;
            if (ClientComboData.getFirstActiveComboSkill() == null) return;
            // 总是触发第一个可释放的连携技
            ClientComboData.ActiveSkillData activeSkillData = ClientComboData.getFirstActiveComboSkill();
            if (activeSkillData == null) return;

            LOGGER.info("COMBO_SKILL key triggered");
            ComboSkillType comboSkillType = activeSkillData.getType();
            ModMessages.sendToServer(new ComboTriggeredPacket_C2S(comboSkillType));
            // 移除已触发的连携技
            ClientComboData.triggerSkillAnimation(comboSkillType);
        }

        if(KeyBindings.BARRIER_WALL.consumeClick()) {
            if (!ClientMinerState.isMinerMode()) return;
            if (localPlayer.getMainHandItem().getItem() != ModItems.PIXEL_CORE.get()) return;
            ModMessages.sendToServer(new NormalSkillPacket_C2S(NormalSkillType.BARRIER_WALL, new BarrierWallSkill()));
        }

    }
}
