package BlockPower.ModMessages.C2SPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 一个抽象的C2S（客户端到服务端）数据包父类。
 * 它封装了所有C2S数据包共有的模板代码，例如线程处理。
 */
abstract class AbstractC2SPacket {

    /**
     * 子类必须实现此方法，以定义如何将它们自己的数据写入字节流。
     * @param buf The buffer to write to.
     */
    public abstract void toBytes(FriendlyByteBuf buf);

    /**
     * 这是所有数据包共有的处理器。
     * 它确保了逻辑总是在服务端的主线程上执行，避免了线程安全问题。
     * 子类不应重写此方法。
     * @param supplier a {@link Supplier} object.
     * @return a boolean.
     */
    public final boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // 调用一个抽象方法，让子类去实现具体的服务端逻辑
            handleServerSide(player);
        });
        return true;
    }

    /**
     * 子类必须实现此方法，以定义当服务端收到该数据包时应执行的具体逻辑。
     * @param player 发送该数据包的玩家。
     */
    protected abstract void handleServerSide(ServerPlayer player);

}
