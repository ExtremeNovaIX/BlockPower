package BlockPower.Skills.MinerState.client;

import BlockPower.Main.Main;
import BlockPower.Skills.MinerState.server.ResourceType;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * 仅存在于客户端，用于缓存从服务端同步过来的资源数据。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientResourceData {

    /**
     *  HUD渲染代码调用此方法来获取要显示的数据。
     *
     * @return 当前缓存的资源数据
     */
    @Getter
    private static final Map<ResourceType, Double> resources = new EnumMap<>(ResourceType.class);

    static {
        // 初始化所有资源类型为0.0
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0.0);
        }
    }

    /**
     * 用从数据包接收到的新数据更新缓存。
     *
     * @param newResources 新的资源数据Map
     */
    public static void setResources(Map<ResourceType, Double> newResources) {
        // 在主渲染线程执行，确保线程安全
        if (Minecraft.getInstance().isSameThread()) {
            resources.clear();
            resources.putAll(newResources);
        } else {
            Minecraft.getInstance().execute(() -> {
                resources.clear();
                resources.putAll(newResources);
            });
        }
    }
}