package BlockPower.Mixins.client;

import BlockPower.Capability.IPlayerAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ElytraLayer.class)
public class ElytraLayerMixin {

    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"),
            name = "itemstack"
    )
    private ItemStack modifyChestItemStack(ItemStack originalStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            IPlayerAccessor accessor = (IPlayerAccessor) player;
            if (accessor.isFlying() && !originalStack.is(Items.ELYTRA)) {
                return new ItemStack(Items.ELYTRA);
            }
        }
        return originalStack;
    }
}
