package BlockPower.ModItems;

import BlockPower.ModMessages.ComboSkillPacket.ComboStandbyPacket_S2C;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModParticles.*;
import BlockPower.Skills.ComboSkills.ComboSkillType;
import BlockPower.Util.Commons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugItem2 extends Item {
    public static final Logger LOGGER = LoggerFactory.getLogger(DebugItem2.class);

    public DebugItem2(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockPos pos = player.getOnPos();

        if (!level.isClientSide) {
            Commons.sendDebugMessage(player, "Server:调试物品使用于位置: " + pos.toShortString());
            testServerMethod(player);
        } else {
            Commons.sendDebugMessage(player, "Client:调试物品使用于位置: " + pos.toShortString());
            testClientMethod(player);
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private void testServerMethod(Player player) {
        LOGGER.info("testServerMethod");
    }

    private void testClientMethod(Player player) {
        LOGGER.info("testClientMethod");
        Level level = player.level();
        Vec3 vec3 = player.getEyePosition();

        spawnTestCometParticle(level, vec3);

    }

    public static void spawnTestCometParticle(Level level, Vec3 pos) {
        // 确保只在客户端执行
        if (!level.isClientSide) return;

        // 1. 拖尾 (彗尾) 的组件 (保持不变)
        // (我们假设 ribbon_particle.json 现在指向 "minecraft:particle/flame")
        ParticleComponent[] ribbonComponents = new ParticleComponent[]{
                new ParticleComponent.PropertyOverLength(
                        ParticleComponent.PropertyOverLength.EnumRibbonProperty.ALPHA,
                        ParticleComponent.KeyTrack.startAndEnd(1.0f, 0.0f) // 尾部淡出
                ),
                new ParticleComponent.PropertyOverLength(
                        ParticleComponent.PropertyOverLength.EnumRibbonProperty.SCALE,
                        ParticleComponent.KeyTrack.startAndEnd(1.0f, 0.5f) // 尾部稍细
                )
        };

        // 2. 头部 (彗星头) 的组件
        ParticleComponent[] headComponents = new ParticleComponent[]{
                // [修复] 让头部粒子可见！
                new ParticleComponent.PropertyControl(
                        ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA,
                        ParticleComponent.KeyTrack.startAndEnd(1.0f, 1.0f), // 1.0f = 完全可见
                        false
                ),
                // [新增] 让头部粒子在 "Roll" 轴上旋转，打破2D广告牌的错觉
                // 6.28f 大约是 2 * PI (360度)
                new ParticleComponent.PropertyControl(
                        ParticleComponent.PropertyControl.EnumParticleProperty.ROLL,
                        ParticleComponent.KeyTrack.startAndEnd(0.0f, 6.28f * 2), // 在生命周期内旋转2圈
                        false
                ),
                // [保留] 附加拖尾生成器
                new RibbonComponent(
                        ModParticles.RIBBON_PARTICLE.get(), // 拖尾粒子类型
                        30,      // 拖尾长度
                        0, 0, 0,
                        0.5,     // [调整] 拖尾基础宽度 (改小一点)
                        1.0, 0.0, 0.0, // 拖尾颜色 (红色)
                        1.0,     // 拖尾透明度
                        true,    // 面向相机 (拖尾仍然面向相机)
                        true,    // 发光
                        ribbonComponents // 应用拖尾组件
                )
        };

        // 3. 创建头部粒子的 ParticleData
        ParticleData headParticleData = new ParticleData(
                ModParticles.ADVANCED_PARTICLE.get(), // 头部粒子类型

                // [修复] 不再使用 FaceCamera，使用 EulerAngles 让粒子可以被旋转
                new ParticleRotation.EulerAngles(0f, 0f, 0f),

                0.5f,    // [调整] 头部粒子大小
                1.0f, 1.0f, 1.0f, 1.0f, // 头部粒子颜色 (白色，让纹理保持原色)
                0.98f,   // 空气阻力
                60,      // 存活时间 (3 秒)
                true,    // 发光
                false,   // 不碰撞
                headComponents
        );

        // 4. [重要] 确保 advanced_particle.json 指向一个好的 "头部" 纹理
        //    例如: "minecraft:particle/flame" 或 "minecraft:particle/spark_7"

        level.addParticle(headParticleData, pos.x(), pos.y(), pos.z(), 0.0, 0.5, 0.0);
    }
}