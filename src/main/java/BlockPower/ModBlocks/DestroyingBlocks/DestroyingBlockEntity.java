package BlockPower.ModBlocks.DestroyingBlocks;

import BlockPower.ModBlocks.ModEntityBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DestroyingBlockEntity extends BlockEntity {

    private int tickCounter = 0;
    private int currentProgressStage = -1; // -1 = 未开始
    private final int virtualBreakerId;
    private final int stageTime;

    public DestroyingBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModEntityBlocks.DESTROYING_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.virtualBreakerId = pPos.hashCode();
        this.stageTime = 10;
    }

    public DestroyingBlockEntity(BlockPos pPos, BlockState pBlockState, int LifeTime) {
        super(ModEntityBlocks.DESTROYING_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.virtualBreakerId = pPos.hashCode();
        this.stageTime = LifeTime / 10;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DestroyingBlockEntity be) {

        be.tickCounter++;

        if (be.tickCounter == be.stageTime) {
            be.tickCounter = 0;
            be.currentProgressStage++;

            // 广播裂纹动画
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlockProgress(be.virtualBreakerId, pos, be.currentProgressStage);
            }

            // 破坏方块 (0-9是10个阶段，当进度到10时代表破坏)
            if (be.currentProgressStage >= 10) {
                level.destroyBlock(pos, false);
            }
        }
    }
}