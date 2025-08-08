package BlockPower.Entities;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;

public interface IStateMachine<E extends Enum<E>> {

    /**
     * 实现类必须提供用于同步状态的DataAccessor。
     */
    EntityDataAccessor<Integer> getStateDataAccessor();

    /**
     * 实现类必须提供其状态枚举的所有值。
     */
    E[] getStateEnumValues();

    default E getState() {
        //this实现了IStateMachine接口的实体对象
        Entity entity = (Entity) this;
        return getStateEnumValues()[entity.getEntityData().get(getStateDataAccessor())];
    }

    default void setState(E newState) {
        Entity entity = (Entity) this;
        entity.getEntityData().set(getStateDataAccessor(), newState.ordinal());
    }
}