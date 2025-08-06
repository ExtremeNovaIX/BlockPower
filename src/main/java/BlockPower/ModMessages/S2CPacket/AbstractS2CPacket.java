package BlockPower.ModMessages.S2CPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 一个抽象的S2C（服务端到客户端）数据包父类。
 * 它封装了所有S2C数据包共有的模板代码，例如线程处理。
 */
abstract class AbstractS2CPacket {

    /**
     * 子类必须实现此方法，以定义如何将它们自己的数据写入字节流。
     * @param buf The buffer to write to.
     */
    public abstract void toBytes(FriendlyByteBuf buf);

    /**
     * 这是所有S2C数据包共有的处理器。
     * 它确保了逻辑总是在客户端的主线程上执行，避免了线程安全问题。
     * 子类不应重写此方法。
     * @param supplier a {@link Supplier} object.
     * @return a boolean.
     */
    public final boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 调用一个抽象方法，让子类去实现具体的客户端逻辑
            handleClientSide();
        });
        return true;
    }

    /**
     * 子类必须实现此方法，以定义当客户端收到该数据包时应执行的具体逻辑。
     */
    protected abstract void handleClientSide();
}
