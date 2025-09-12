package BlockPower.ModMessages.SkillC2SPacket;

import BlockPower.ModBlocks.ModBlocks;
import BlockPower.Skills.PlaceBlockSkill;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockSkillPacket_C2S extends AbstractSkillPacket_C2S {
    public PlaceBlockSkillPacket_C2S() {
        super(new PlaceBlockSkill());
    }

    public PlaceBlockSkillPacket_C2S(FriendlyByteBuf buf) {
        super(new PlaceBlockSkill());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        Level level = player.level();
        BlockPos blockBelowPos = player.blockPosition().below();
        Block block = level.getBlockState(blockBelowPos).getBlock();
        if (block == Blocks.AIR || block instanceof LiquidBlock) {
            level.setBlockAndUpdate(blockBelowPos, ModBlocks.DECAYING_DIRT.get().defaultBlockState());
            BlockState newState = level.getBlockState(blockBelowPos);
            SoundType soundType = newState.getSoundType();
            SoundEvent placeSound = soundType.getPlaceSound();
            player.level().playSound(null, player, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
            super.consumeResource(player, skill);
        }
    }

    @Override
    protected void afterHandleServerSide(ServerPlayer player) {
    }
}
