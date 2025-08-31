package BlockPower.Skills.MinerState.server;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个单例管理器，用于在全局范围内存放和管理所有玩家的资源数据。
 * 使用玩家的UUID作为键，可以避免因玩家下线重登或维度穿梭导致Player对象重新创建而造成的数据丢失问题。
 */
public class PlayerResourceManager {
    private static final PlayerResourceManager INSTANCE = new PlayerResourceManager();

    private final Map<UUID, PlayerResourceData> playerData = new ConcurrentHashMap<>();

    private PlayerResourceManager() {}

    /**
     * 获取PlayerResourceManager的唯一公共实例。
     * @return 管理器实例。
     */
    public static PlayerResourceManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取指定玩家的资源数据对象。
     * 如果该玩家的数据在Map中不存在，则会为其自动创建一个新的实例并存入Map中，然后返回。
     * @param player 目标玩家。
     * @return 玩家对应的PlayerResourceData实例，永不为null。
     */
    public PlayerResourceData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUUID(), k -> new PlayerResourceData());
    }

    public void clear() {
        playerData.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        playerData.forEach((uuid, data) -> {
            sb.append(uuid).append(":").append(data.toString()).append("\n");
        });
        return sb.toString();
    }
}