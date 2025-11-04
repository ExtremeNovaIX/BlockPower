package BlockPower.ModException;

import BlockPower.Util.ModEffects.ITickBasedEffect;

public class EffectException extends RuntimeException {
    public EffectException(String message) {
        super(message);
    }

     /**
      * 当无法根据 Class 对象找到对应的 ClientEffectEnum 时抛出此异常，代表该效果类未在 ClientEffectEnum 中注册。
      * @param clazz 无法找到对应的 Class 对象
      */
    public EffectException(Class<? extends ITickBasedEffect> clazz) {
        super("No ClientEffectEnum found for class: " + clazz.getName());
    }

     /**
      * 当效果实例返回 null 时抛出此异常，代表子类未实现 getType() 方法或是没有在 ClientEffectEnum 中注册。
      * @param effect 效果实例
      */
    public EffectException(ITickBasedEffect effect) {
        super("Effect instance returned a null ClientEffectEnum from getType(). This effect cannot be synced: " + effect.getClass().getName());
    }
}
