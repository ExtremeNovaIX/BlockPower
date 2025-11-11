package BlockPower.Events.SkillEvents;


import BlockPower.Capability.IPlayerAccessor;
import BlockPower.Capability.ModCapabilities;
import BlockPower.Main.Main;
import BlockPower.ModBlocks.DestroyingBlocks.DestroyingBlock;
import BlockPower.Skills.NormalSkills.AirJumpSkill;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.Commons;
import BlockPower.Util.Timer.TimerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AirJumpSkillEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide() || event.phase != TickEvent.Phase.END) {
            return;
        }
        ServerPlayer player = (ServerPlayer) event.player;
        if (Commons.isSpectatorOrCreativeMode(player)) {
            return;
        }

        if (player.onGround()) {
//            // 检查玩家脚下方块是否属于DestroyingBlock
//            BlockPos footPos = player.blockPosition().below();
//            BlockState blockState = player.level().getBlockState(footPos);
//
//            // 如果脚下方块是DestroyingBlock，则不执行逻辑
//            if (blockState.getBlock() instanceof DestroyingBlock) {
//                return;
//            }

            player.getCapability(ModCapabilities.PLAYER_AIR_JUMP_DATA).ifPresent(airJumpData -> {
                if (airJumpData.getJumpCount() > 0) {
                    airJumpData.resetJumpCount();
                    SkillLockManager.unlock(player, AirJumpSkill.SKILL_ID);
                }
            });
        }
    }

}
