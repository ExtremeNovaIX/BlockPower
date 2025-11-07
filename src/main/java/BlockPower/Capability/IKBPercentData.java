package BlockPower.Capability;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface IKBPercentData {
    /**
     *  通过Mixin注入到LivingEntity中的击退值数据访问器。
     */
    EntityDataAccessor<Float> getKBPercentDataAccessor();
}
