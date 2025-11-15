package BlockPower.Skills.MinerState.server;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * 定义了玩家可以收集的资源类型。
 * 枚举在此处声明的顺序决定了资源在UI资源条上的渲染顺序。
 */
public enum ResourceType {
    DIRT(Items.DIRT, 100, 0),
    WOOD(Items.OAK_PLANKS, 20, 1),
    STONE(Items.COBBLESTONE, 70, 2),
    GOLD(Items.RAW_GOLD, 16, 3),
    IRON(Items.RAW_IRON, 100, 4),
    DIAMOND(Items.DIAMOND, 8, 5),
    NETHERITE(Items.NETHERITE_SCRAP, 4, 6);

    private final int maxAmount;
    private final Item correspondingItem;
    private final int level;

    ResourceType(Item item, int maxAmount, int level) {
        this.correspondingItem = item;
        this.maxAmount = maxAmount;
        this.level = level;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public Item getCorrespondingItem() {
        return this.correspondingItem;
    }

    public int getLevel() {
        return this.level;
    }

    public static List<ResourceType> getNormalResourceType() {
        return List.of(DIRT, WOOD, STONE, IRON);
    }

    public static List<ResourceType> getPreciousResourceType() {
        return List.of(GOLD, DIAMOND, NETHERITE);
    }
}
