package BlockPower.ModBlocks.DestroyingBlocks;

import BlockPower.ModBlocks.ModEntityBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DestroyingBlock extends BaseEntityBlock {
    private final int LifeTime;

    public DestroyingBlock(Properties properties, int LifeTime) {
        super(properties);
        this.LifeTime = LifeTime;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DestroyingBlockEntity(pos, state, LifeTime);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(type, ModEntityBlocks.DESTROYING_BLOCK_ENTITY.get(), DestroyingBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 当方块被破坏时清除所有正在播放的裂纹动画
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // 发送一个 -1 进度，告诉所有客户端停止播放这个方块的裂纹动画
            serverLevel.destroyBlockProgress(pos.hashCode(), pos, -1);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
