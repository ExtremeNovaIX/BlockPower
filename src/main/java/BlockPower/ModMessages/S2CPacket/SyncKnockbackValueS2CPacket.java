package BlockPower.ModMessages.S2CPacket;

import BlockPower.Capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

// 用于将玩家的击退值从服务器同步到客户端
public class SyncKnockbackValueS2CPacket extends AbstractS2CPacket {

    private final double knockbackValue;

    public SyncKnockbackValueS2CPacket(double knockbackValue) {
        this.knockbackValue = knockbackValue;
    }

    public SyncKnockbackValueS2CPacket(FriendlyByteBuf buf) {
        this.knockbackValue = buf.readDouble();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(knockbackValue);
    }

    @Override
    protected void handleClientSide() {
        // 获取当前玩家并更新其Capability数据
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
                cap.setKBPercent(this.knockbackValue);
            });
        }
    }
}
