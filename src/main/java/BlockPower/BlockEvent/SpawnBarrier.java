package BlockPower.BlockEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SpawnBarrier {
    private static SpawnBarrier instance;
    private Player player;
    private int tick = 0;

    // 墙体建造状态
    private double startAngle;
    private double endAngle;
    private double currentAngle;
    private int currentY;
    private boolean isBuilding;

    // 墙体参数
    private static final int RADIUS = 5;
    private static final int HEIGHT = 3;
    private static final double ANGLE_STEP = Math.PI / 18;


    public static void init(Player player) {
        if (instance == null) {
            instance = new SpawnBarrier();
        }
        instance.player = player;
        instance.tick = 0;
        instance.isBuilding = true;

        // 初始化墙体建造的起始参数
        float yaw = (player.getYRot() % 360 + 360) % 360;
        double centerAngle = Math.toRadians(yaw + 90);
        instance.startAngle = centerAngle - Math.PI / 2.5;
        instance.endAngle = centerAngle + Math.PI / 2.5;
        instance.currentAngle = instance.startAngle;
        instance.currentY = 0;
    }

    @SubscribeEvent
    public static void onPlayerTickStatic(TickEvent.PlayerTickEvent event) {
        if (instance != null) {
            instance.onPlayerTick(event);
        }
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player eventPlayer = event.player;
        // 检查事件是否由技能触发者触发
        if (this.player != null && eventPlayer.getUUID().equals(player.getUUID())) {
            tick++;
            // 当tick模3是0时放置一次方块
            if (tick % 3 == 0 && isBuilding) {
                spawnBarrier(player.level(), player.blockPosition(), Blocks.STONE);
            }
        }
    }

    /**
     * 一次只在计算出的位置放置一个方块，并更新下一次放置的位置。
     * @param level level
     * @param center 玩家位置
     * @param block 方块类型
     */
    public void spawnBarrier(Level level, BlockPos center, Block block) {
        // 如果当前角度没有超过结束角度，则继续建造
        if (currentAngle <= endAngle) {
            // 计算当前角度和高度的方块位置
            int x = center.getX() + (int) (RADIUS * Math.cos(currentAngle));
            int z = center.getZ() + (int) (RADIUS * Math.sin(currentAngle));
            BlockPos pos = new BlockPos(x, center.getY() + currentY, z);

            if (level.isEmptyBlock(pos)) {
                level.setBlock(pos, block.defaultBlockState(), 3);
            }

            // 更新下一个方块的位置
            currentY++;
            // 如果当前柱子已达到指定高度，则移动到下一个角度并重置高度
            if (currentY >= HEIGHT + 2) {
                currentY = -2;
                currentAngle += ANGLE_STEP;
            }
        } else {
            // 建造完成
            isBuilding = false;
        }
    }
}