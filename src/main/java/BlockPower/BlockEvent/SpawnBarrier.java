package BlockPower.BlockEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber
public class SpawnBarrier {
    // 使用列表管理多个 SpawnBarrier 实例
    private static final List<SpawnBarrier> instances = new ArrayList<>();
    private Player player;
    private int tick = 0;

    // 墙体建造状态
    private double startAngle;
    private double endAngle;
    private double currentAngle;
    private int currentY;
    private boolean isBuilding;

    // 新增：记录玩家初始位置
    private BlockPos initialPosition;

    // 墙体参数
    private static final int RADIUS = 5;
    private static final int HEIGHT = 3;
    private static final double ANGLE_STEP = Math.PI / 18;
    private boolean isLeft = true; // 标记当前是否向左延伸
    private int currentColumn = 0; // 当前处理的列数

    public static void init(Player player) {
        SpawnBarrier instance = new SpawnBarrier();
        instance.player = player;
        // 记录玩家初始位置
        instance.initialPosition = player.blockPosition();
        instance.tick = 0;
        instance.isBuilding = true;

        // 初始化墙体建造的起始参数
        float yaw = (player.getYRot() % 360 + 360) % 360;
        double centerAngle = Math.toRadians(yaw + 90);
        instance.startAngle = centerAngle;
        instance.endAngle = centerAngle;
        instance.currentAngle = centerAngle;
        instance.currentY = 0;
        instance.isLeft = true;
        instance.currentColumn = 0;

        instances.add(instance);
    }

    @SubscribeEvent
    public static void onPlayerTickStatic(TickEvent.PlayerTickEvent event) {
        // 使用迭代器遍历实例列表，避免并发修改异常
        instances.removeIf(instance -> {
            Player eventPlayer = event.player;
            Level level = eventPlayer.level();
            // 使用初始位置
            BlockPos blockPos = instance.initialPosition;
            instance.onPlayerTick(event, eventPlayer, level, blockPos);
            // 如果墙体已经移除，从列表中移除该实例
            return !instance.isBuilding && instance.tick > 600;
        });
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent event, Player player, Level level, BlockPos blockPos) {
        Player eventPlayer = event.player;
        // 检查事件是否由技能触发者触发
        if (this.player != null && eventPlayer.getUUID().equals(player.getUUID())) {
            tick++;

            // 当tick模3是0时放置一次方块
            if (tick % 1 == 0 && isBuilding) {
                spawnBarrier(level, blockPos);
            }

            // 当 tick 达到 600 时，移除生成的方块
            if (tick == 300) {
                removeBarrier(level, blockPos);
                isBuilding = false;
            }
        }
    }

    /**
     * 一次只在计算出的位置放置一个方块，并更新下一次放置的位置。
     *
     * @param level  level
     * @param center 玩家初始位置
     */
    private void spawnBarrier(Level level, BlockPos center) {
        // 计算当前角度
        double currentColumnAngle = currentColumn * ANGLE_STEP;
        currentAngle = startAngle + (isLeft ? -currentColumnAngle : currentColumnAngle);

        // 如果当前角度没有超过最大角度范围，则继续建造
        if (Math.abs(currentAngle - startAngle) <= Math.PI / 4) {
            // 计算当前角度和高度的方块位置
            int x = center.getX() + (int) (RADIUS * Math.cos(currentAngle));
            int z = center.getZ() + (int) (RADIUS * Math.sin(currentAngle));
            BlockPos pos = new BlockPos(x, center.getY() + currentY, z);

            if (level.isEmptyBlock(pos)) {
                level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
            }

            // 更新下一个方块的位置
            currentY++;
            // 如果当前柱子已达到指定高度，则移动到下一个角度并重置高度
            if (currentY >= HEIGHT + 1) {
                currentY = -1;
                isLeft = !isLeft; // 切换左右方向
                if (!isLeft) {
                    currentColumn++; // 完成左右一次放置后，列数加1
                }
                currentAngle += ANGLE_STEP;
            }
        } else {
            // 建造完成
            isBuilding = false;
        }
    }

    private void removeBarrier(Level level, BlockPos center) {
        double currentColumn = 0;
        boolean isLeft = true;
        double currentAngle;


        do {
            // 计算当前角度
            double currentColumnAngle = currentColumn * ANGLE_STEP;
            currentAngle = startAngle + (isLeft ? -currentColumnAngle : currentColumnAngle);

            // 遍历柱子高度
            for (int y = -1; y <= HEIGHT; y++) {
                int x = center.getX() + (int) (RADIUS * Math.cos(currentAngle));
                int z = center.getZ() + (int) (RADIUS * Math.sin(currentAngle));
                BlockPos pos = new BlockPos(x, center.getY() + y, z);
                // 移除方块

                level.removeBlock(pos, false);
                level.blockUpdated(pos, Blocks.AIR);

            }

            isLeft = !isLeft; // 切换左右方向
            if (!isLeft) {
                currentColumn++; // 完成左右一次放置后，列数加1
            }
        } while (Math.abs(currentAngle - startAngle) <= Math.PI / 3);
    }
}