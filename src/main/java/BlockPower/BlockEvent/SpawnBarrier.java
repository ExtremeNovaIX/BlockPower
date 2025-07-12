package BlockPower.BlockEvent;

import net.minecraft.core.BlockPos;
// 修正导入正确的Level类
import net.minecraft.world.level.Level;

public class SpawnBarrier {
    public static void spawnBarrier(Level level, BlockPos center, float yaw, int radius, int height, net.minecraft.world.level.block.Block block) {
        yaw = (yaw % 360 + 360) % 360;
        // 调整角度计算：Minecraft中0度指向正南，需要+90度转换为数学坐标系
        double centerAngle = Math.toRadians(yaw + 90); // 新增90度偏移
        double startAngle = centerAngle - Math.PI/2.5;
        double endAngle = centerAngle + Math.PI/2.5;


        // 保持原有角度步长
        double angleStep = Math.PI / 18;

        for (double angle = startAngle; angle <= endAngle; angle += angleStep) {
            // 使用修正后的三角函数计算坐标
            int x = center.getX() + (int) (radius * Math.cos(angle));
            int z = center.getZ() + (int) (radius * Math.sin(angle));
            for (int y = -2; y < height; y++) {
                BlockPos pos = new BlockPos(x, center.getY() + y, z);
                level.setBlock(pos, block.defaultBlockState(), 3);
            }
        }
    }
}