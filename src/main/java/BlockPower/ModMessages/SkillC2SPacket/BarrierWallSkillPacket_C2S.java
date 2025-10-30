package BlockPower.ModMessages.SkillC2SPacket;

import BlockPower.Util.Commons;
import BlockPower.Util.TaskManager;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import BlockPower.ModBlocks.ModBlocks;
import BlockPower.Skills.BarrierWallSkill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class BarrierWallSkillPacket_C2S extends AbstractSkillPacket_C2S {
    private static final Logger DEBUGGER = LoggerFactory.getLogger(BarrierWallSkillPacket_C2S.class);
    public BarrierWallSkillPacket_C2S() {
        super(new BarrierWallSkill());
    }

    public BarrierWallSkillPacket_C2S(FriendlyByteBuf buf) {
        super(new BarrierWallSkill());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        // 不需要传输额外数据
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
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
        TaskManager taskManager = TaskManager.getInstance(false); // false表示服务端实例
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
                        int x = playerPos.getX() + (int)Math.round(radius * Math.cos(centerAngle));
                        int z = playerPos.getZ() + (int)Math.round(radius * Math.sin(centerAngle));
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
                    final double leftAngle = startAngle + leftIndex * angleStep;

                    taskManager.runTaskAfterTicks(delay, () -> {
                        // 计算左侧点的坐标
                        int x = playerPos.getX() + (int)Math.round(radius * Math.cos(leftAngle));
                        int z = playerPos.getZ() + (int)Math.round(radius * Math.sin(leftAngle));
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

                    // 右侧位置
                    final int rightIndex = centerIndex + spread;
                    final double rightAngle = startAngle + rightIndex * angleStep;

                    taskManager.runTaskAfterTicks(delay, () -> {
                        // 计算右侧点的坐标
                        int x = playerPos.getX() + (int)Math.round(radius * Math.cos(rightAngle));
                        int z = playerPos.getZ() + (int)Math.round(radius * Math.sin(rightAngle));
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
                    if(spread % 2 == 0) {
                        delay += 1;
                    }
                }
            }
        }
    }


    @Override
    protected void afterHandleServerSide(ServerPlayer player) {
        // 技能锁逻辑和资源消耗在父类中自动处理
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
}