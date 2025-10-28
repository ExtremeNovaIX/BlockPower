package BlockPower.Skills.NormalSkills;

import BlockPower.ModBlocks.ModBlocks;
import BlockPower.ModException.SkillException;
import BlockPower.Skills.MinerState.server.AllResourceType;
import BlockPower.Skills.SkillExecutionResult;
import BlockPower.Util.Commons;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;

public class PlaceBlockSkill implements IPacketSerializableSkill {
    @Override
    public @NotNull String getSkillName() {
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
    public SkillExecutionResult triggerSkill(ServerPlayer player) {
        Level level = player.level();

        // 获取玩家的方块放置范围属性值
        double reachDistance = player.getAttributeValue(ForgeMod.BLOCK_REACH.get());
        HitResult rayTraceResult = player.pick(reachDistance, 0, false);

        BlockPos placementPos;
        // 根据玩家指向的位置确定放置方块的位置
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            // 玩家指向了一个方块，则使用原版方块放置逻辑
            BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
            placementPos = blockResult.getBlockPos().relative(blockResult.getDirection());
        } else {
            // 玩家未指向方块，则在玩家脚下放置方块
            placementPos = player.blockPosition().below();
        }

        // 检查目标位置是否为空气或液体方块
        BlockState stateAtPlacementPos = level.getBlockState(placementPos);
        Block block = stateAtPlacementPos.getBlock();
        boolean canBeReplaced = stateAtPlacementPos.isAir() || block instanceof LiquidBlock;

        // 检查目标位置是否有实体
        boolean isUnobstructed = level.getEntitiesOfClass(
                Entity.class,
                new AABB(placementPos),
                (entity) -> !entity.isSpectator() // 忽略观察者
        ).isEmpty();

        // 检查目标位置是否被阻塞或不可以替换，如果不可替换则返回false
        if (!isUnobstructed || !canBeReplaced) {
            return SkillExecutionResult.fail("Target position is obstructed or not replaceable.");
        }

        // 如果目标位置为空气或液体方块，则可以替换为 decaying_dirt
        level.setBlockAndUpdate(placementPos, ModBlocks.DECAYING_DIRT.get().defaultBlockState());
        // 更改玩家像素核心为泥土材质
        Commons.changePixelCoreNBT(player, 4.0F, 1.0F, -1.0F);

        // 播放放置音效
        SoundEvent placeSound = level.getBlockState(placementPos).getSoundType().getPlaceSound();
        player.level().playSound(null, player, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);

        player.swing(InteractionHand.MAIN_HAND, true);

        return SkillExecutionResult.success();
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
    public void writeParams(FriendlyByteBuf buf) {

    }

    @Override
    public void readParams(FriendlyByteBuf buf) {

    }
}
