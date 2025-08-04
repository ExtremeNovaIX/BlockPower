package BlockPower.Util.StateMachine;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

/**
 * 这是一个给实体快捷添加状态机的类
 * @param <E> 状态枚举类型
 */
public final class StateMachine<E extends Enum<E>> {
    private final Entity owner;//状态机的拥有者
    private final EntityDataAccessor<Integer> dataAccessor;//状态机的数据访问器
    private final E[] enumValues;//状态机的状态枚举数组

    /**
     * 创建一个状态机管理器.
     * @param owner 拥有此状态机的实体.
     * @param entityClass 实体的Class对象.
     * @param enumClass 状态枚举的Class对象.
     * @param initialState 初始状态.
     */
    public StateMachine(Entity owner, Class<? extends Entity> entityClass, Class<E> enumClass, E initialState) {
        this.owner = owner;
        this.enumValues = enumClass.getEnumConstants();
        // 自动创建一个唯一的DataAccessor
        this.dataAccessor = SynchedEntityData.defineId(entityClass, EntityDataSerializers.INT);
        // 在构造时注册DataAccessor并设置默认State
        this.owner.getEntityData().define(dataAccessor, initialState.ordinal());
    }

    public void setState(E newState) {
        this.owner.getEntityData().set(this.dataAccessor, newState.ordinal());
    }

    public E getState() {
        return this.enumValues[this.owner.getEntityData().get(this.dataAccessor)];
    }
}