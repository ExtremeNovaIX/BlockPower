package BlockPower.Skills.NormalSkills;

import BlockPower.ModBlocks.ModBlocks;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import BlockPower.Skills.SkillLock.LockPriority;
import BlockPower.Skills.SkillLock.SkillLockManager;
import BlockPower.Util.TaskManager;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BarrierWallSkill implements IPacketSerializableSkill {
    private static final TaskManager taskManager = TaskManager.getInstance(false);

    private static final String LOCK_ID = "barrier_wall";

    @Override
    public void writeParams(FriendlyByteBuf buf) {

    }

    @Override
    public void readParams(FriendlyByteBuf buf) {

    }

    @Override
    public @NotNull String getSkillName() {
        return "";
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
    public SkillExecutionResult triggerSkill(ServerPlayer player) {
        SkillLockManager.lock(player, "barrier_wall", LockPriority.LOWEST);
        Level level = player.level();
        // 获取玩家的视线方向向量
        Vec3 lookVec = player.getLookAngle();
        BlockPos playerPos = player.blockPosition();

        // 圆半径设为4格
        int radius = 4;
        // 墙的高度设为3格
        int wallHeight = 3;

        // 使用玩家的视线方向计算墙的朝向角度
        // 计算视线方向在水平面上的角度
        double horizontalAngle = Math.atan2(lookVec.z, lookVec.x);

        double arcHalfAngle = Math.toRadians(35); // 左右各35度

        double startAngle = horizontalAngle - arcHalfAngle;
        double endAngle = horizontalAngle + arcHalfAngle;

        int segments = 12;
        double angleStep = (endAngle - startAngle) / segments;

        // 生成圆弧形墙 - 从中心向两边扩散
        int delay = 0;

        // 先计算中心位置
        int centerIndex = segments / 2;

        for (int height = -1; height < wallHeight; height++) {
            // 从中心开始，向两边扩散
            for (int spread = 0; spread <= centerIndex; spread++) {
                // 处理中心位置（如果spread为0）
                if (spread == 0) {
                    final int finalHeight = height;
                    final double centerAngle = horizontalAngle;

                    taskManager.runTaskAfterTicks(delay, () -> {
                        // 计算中心点的坐标
                        int x = playerPos.getX() + (int) Math.round(radius * Math.cos(centerAngle));
                        int z = playerPos.getZ() + (int) Math.round(radius * Math.sin(centerAngle));
                        int y = playerPos.getY() + finalHeight;

                        BlockPos wallPos = new BlockPos(x, y, z);

                        // 检查位置是否可放置方块
                        if (canPlaceBlock(level, wallPos)) {
                            level.setBlockAndUpdate(wallPos, ModBlocks.DECAYING_DIRT.get().defaultBlockState());
                            // 播放放置方块的声音
                            BlockState newState = level.getBlockState(wallPos);
                            SoundType soundType = newState.getSoundType();
                            SoundEvent placeSound = soundType.getPlaceSound();
                            level.playSound(null, wallPos, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    });
                    delay += 1;
                }

                // 处理左右两侧的位置（如果spread > 0）
                if (spread > 0) {
                    // 左侧位置
                    final int finalHeight = height;
                    final int leftIndex = centerIndex - spread;
                    barrierSpawn(level, playerPos, radius, startAngle, angleStep, delay, finalHeight, leftIndex);

                    // 右侧位置
                    final int rightIndex = centerIndex + spread;
                    barrierSpawn(level, playerPos, radius, startAngle, angleStep, delay, finalHeight, rightIndex);
                    if (spread % 2 == 0) {
                        delay += 1;
                    }
                }
            }
        }
        SkillLockManager.unlock(player, LOCK_ID);
        return SkillExecutionResult.success();
    }

    private void barrierSpawn(Level level, BlockPos playerPos, int radius, double startAngle, double angleStep, int delay, int finalHeight, int leftIndex) {
        final double leftAngle = startAngle + leftIndex * angleStep;

        taskManager.runTaskAfterTicks(delay, () -> {
            // 计算左侧点的坐标
            int x = playerPos.getX() + (int) Math.round(radius * Math.cos(leftAngle));
            int z = playerPos.getZ() + (int) Math.round(radius * Math.sin(leftAngle));
            int y = playerPos.getY() + finalHeight;

            BlockPos wallPos = new BlockPos(x, y, z);

            // 检查位置是否可放置方块
            if (canPlaceBlock(level, wallPos)) {
                level.setBlockAndUpdate(wallPos, ModBlocks.DECAYING_DIRT.get().defaultBlockState());
                // 播放放置方块的声音
                BlockState newState = level.getBlockState(wallPos);
                SoundType soundType = newState.getSoundType();
                SoundEvent placeSound = soundType.getPlaceSound();
                level.playSound(null, wallPos, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        });
    }

    private boolean canPlaceBlock(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        boolean canPlace = block == Blocks.AIR || block instanceof LiquidBlock ||
                block instanceof net.minecraft.world.level.block.SaplingBlock ||
                block instanceof net.minecraft.world.level.block.FlowerBlock ||
                block instanceof net.minecraft.world.level.block.TallGrassBlock ||
                block instanceof net.minecraft.world.level.block.BushBlock;
        return canPlace;
    }

    @Override
    public AllResourceType getSkillCostType() {
        return null;
    }

    @Override
    public double getSkillCostAmount() {
        return 0;
    }

    @Override
    public boolean isSkillConsumeResource() {
        return false;
    }

    @Override
    public void recordCombo(ServerPlayer player) {

    }
}
