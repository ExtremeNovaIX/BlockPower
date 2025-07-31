package BlockPower.ModMessages;

import BlockPower.DTO.ActionData;
import BlockPower.DTO.S2C.CrossStarRenderData;
import BlockPower.DTO.S2C.HitStopData;
import BlockPower.DTO.S2C.ShakeData;
import BlockPower.Util.Gson.ModGson;
import BlockPower.Util.HitStop.HitStopHandler;
import BlockPower.Util.ScreenShake.ScreenShakeHandler;
import BlockPower.Util.Visual.ClientEffectManager;
import BlockPower.Util.Visual.VisualEffect;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class PlayerActionPacket_S2C {
    private static final Gson GSON = ModGson.getInstance();
    private final ActionData data;
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerActionPacket_S2C");

    // 构造函数：创建信件时，必须指明要执行哪个动作
    public PlayerActionPacket_S2C(ActionData data) {
        this.data = data;
    }

    // 解码器 (接收时使用): 从字节流中恢复出 ActionData 对象
    public PlayerActionPacket_S2C(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        String json = buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
        // GSON会根据json中的"actionType"字段，自动创建出正确的子类对象
        this.data = GSON.fromJson(json, ActionData.class);
    }

    // 编码器 (发送时使用): 将 ActionData 对象打包成字节流
    public void toBytes(FriendlyByteBuf buf) {
        String json = GSON.toJson(this.data, ActionData.class);
        buf.writeUtf(json);
    }

    /**
     * 处理器：客户端接收后，根据动作执行对应逻辑
     *
     * @param supplier 网络上下文
     * @return 是否成功处理
     */
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 首先判断data是否为null
            if (this.data == null) {
                LOGGER.warn("收到了异常S2C数据包");
                return;
            }

            // 根据对象的真实类型来执行逻辑
            if (this.data instanceof ShakeData shakeData) {
                LOGGER.info("客户端收到指令：SHAKE (时长: {}, 强度: {})", shakeData.getDuration(), shakeData.getStrength());
                ScreenShakeHandler.shakeTrigger(shakeData.getDuration(), shakeData.getStrength());
            } else if (this.data instanceof HitStopData hitStopData) {
                LOGGER.info("客户端收到指令：HIT_STOP (时长: {})", hitStopData.getDuration());
                HitStopHandler.start(hitStopData.getDuration());
            } else if (this.data instanceof CrossStarRenderData crossStarRenderData) {
                LOGGER.info("客户端收到指令：CROSS_STAR (时长: {}, 位置: {}, {}, {})", crossStarRenderData.getDuration(), crossStarRenderData.getX(), crossStarRenderData.getY(), crossStarRenderData.getZ());
                ClientEffectManager.spawnEffect(
                        VisualEffect.EffectType.CROSS_STAR,
                        new Vec3(crossStarRenderData.getX(), crossStarRenderData.getY(), crossStarRenderData.getZ()),
                        crossStarRenderData.getDuration()
                );
            } else {
                LOGGER.warn("收到了未知的S2C动作类型: " + this.data.getClass().getName());
            }
        });
        return true;
    }
}