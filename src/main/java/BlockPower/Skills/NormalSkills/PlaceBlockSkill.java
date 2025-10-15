package BlockPower.Skills.NormalSkills;

import BlockPower.ModBlocks.ModBlocks;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Util.Commons;
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

public class PlaceBlockSkill implements IPacketSerializableSkill {
    @Override
    public String getSkillName() {
        return "PlaceBlock";
    }

    @Override
    public String getSkillDescription() {
        return "";
    }

    @Override
    public int getSkillLevel() {
        return 0;
    }

    @Override
    public void triggerSkill(ServerPlayer player) {
        Level level = player.level();
        BlockPos blockBelowPos = player.blockPosition().below();
        Block block = level.getBlockState(blockBelowPos).getBlock();
        if (block == Blocks.AIR || block instanceof LiquidBlock) {
            level.setBlockAndUpdate(blockBelowPos, ModBlocks.DECAYING_DIRT.get().defaultBlockState());
            Commons.changePixelCoreNBT(player, 4.0F, 1.0F, -1.0F);
            BlockState newState = level.getBlockState(blockBelowPos);
            SoundType soundType = newState.getSoundType();
            SoundEvent placeSound = soundType.getPlaceSound();
            player.level().playSound(null, player, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public AllResourceType getSkillCostType() {
        return AllResourceType.DIRT;
    }

    @Override
    public double getSkillCostAmount() {
        return 1;
    }

    @Override
    public boolean isSkillConsumeResource() {
        return true;
    }

    @Override
    public boolean isSkillAutoLocked() {
        return false;
    }

    @Override
    public void writeParams(FriendlyByteBuf buf) {

    }

    @Override
    public void readParams(FriendlyByteBuf buf) {

    }
}
