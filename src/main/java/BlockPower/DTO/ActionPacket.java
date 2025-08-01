package BlockPower.DTO;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 这个注解用来将一个动作枚举常量，与其对应的数据包类进行自动绑定
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ActionPacket {
    /**
     * @return 与该动作关联的具体ActionData子类
     */
    Class<? extends ActionData> value();

}
