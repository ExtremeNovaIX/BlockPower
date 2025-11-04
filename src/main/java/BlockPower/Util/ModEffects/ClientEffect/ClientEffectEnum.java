package BlockPower.Util.ModEffects.ClientEffect;

import BlockPower.ModException.EffectException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public enum ClientEffectEnum {
    PLAYER_SNEAK(PlayerSneakEffect.class, PlayerSneakEffect::new),
    SCREEN_SHAKE(ScreenShakeEffect.class, ScreenShakeEffect::new);

    private final Class<? extends IClientTickBasedEffect> effectClass;
    private final Function<FriendlyByteBuf, IClientTickBasedEffect> factory;

    /**
     * @param effectClass 效果的 Class 对象
     * @param factory     一个函数，接受 FriendlyByteBuf 并返回一个新的 ITickBasedEffect 实例
     */
    ClientEffectEnum(Class<? extends IClientTickBasedEffect> effectClass, Function<FriendlyByteBuf, IClientTickBasedEffect> factory) {
        this.effectClass = effectClass;
        this.factory = factory;
    }

    /**
     * 根据提供的 FriendlyByteBuf 创建一个新的效果实例。
     * @return 新创建的 ITickBasedEffect 实例
     */
    public IClientTickBasedEffect createEffect(FriendlyByteBuf buf) {
        return factory.apply(buf);
    }

    public Class<? extends IClientTickBasedEffect> getEffectClass() {
        return effectClass;
    }

    /**
     * 根据效果的 Class 对象查找对应的枚举值。
     * @param clazz 效果的 Class 对象
     * @return 对应的 ClientEffectEnum 值
     * @throws EffectException 如果未找到对应的枚举值
     */
    public static ClientEffectEnum findFromClass(Class<? extends IClientTickBasedEffect> clazz) {
        for (ClientEffectEnum type : values()) {
            if (type.getEffectClass() == clazz) {
                return type;
            }
        }
        throw new EffectException(clazz);
    }
}
