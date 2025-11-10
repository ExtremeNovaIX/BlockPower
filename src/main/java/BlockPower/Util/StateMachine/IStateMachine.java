package BlockPower.Util.StateMachine;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface IStateMachine<E extends Enum<E>> {

    /**
     * 实现类必须提供用于同步状态的DataAccessor。
     */
    @NotNull
    EntityDataAccessor<Integer> getStateDataAccessor();

    /**
     * 实现类必须提供其状态枚举的所有值。
     */
    @NotNull
    E[] getStateEnumValues();


    /**
     * 每次状态改变时调用
     */
    void onStateChange(E newState, E oldState);

    /**
     * 处理状态改变的时机，当满足条件时改变状态
     */
    void handleStateChange();

    /**
     * 处理每个状态每tick需要执行的操作
     */
    void handleStateAction();



    default E getState() {
        //this实现了IStateMachine接口的实体对象
        Entity entity = (Entity) this;
        return getStateEnumValues()[entity.getEntityData().get(getStateDataAccessor())];
    }

    default void setState(E newState) {
        Entity entity = (Entity) this;
        E oldState = getState();
        // 只有当状态改变时才调用onStateChange
        if (oldState != newState) {
            onStateChange(newState, oldState);
            entity.getEntityData().set(getStateDataAccessor(), newState.ordinal());
        }
    }
}