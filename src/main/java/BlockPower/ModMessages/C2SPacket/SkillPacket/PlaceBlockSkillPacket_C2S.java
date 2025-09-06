package BlockPower.ModMessages.C2SPacket.SkillPacket;

import BlockPower.ModBlocks.ModBlocks;
import BlockPower.Skills.PlaceBlockSkill;
import BlockPower.Skills.Skill;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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
        if (block == Blocks.AIR) {
            level.setBlockAndUpdate(blockBelowPos, ModBlocks.DECAYING_DIRT.get().defaultBlockState());
            consumeResource(player, skill);
        }
    }
}
