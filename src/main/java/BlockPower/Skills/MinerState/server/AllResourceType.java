package BlockPower.Skills.MinerState.server;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * 定义了玩家可以收集的资源类型。
 * 枚举在此处声明的顺序决定了资源在UI资源条上的渲染顺序。
 */
public enum AllResourceType {
    DIRT(Items.DIRT, 50),
    WOOD(Items.OAK_PLANKS, 30),
    STONE(Items.COBBLESTONE, 30),
    IRON(Items.RAW_IRON, 80),
    GOLD(Items.RAW_GOLD, 16),
    DIAMOND(Items.DIAMOND, 8),
    NETHERITE(Items.NETHERITE_SCRAP, 4);

    private final int maxAmount;//每种资源的最大数量

    private final Item correspondingItem;// 每种资源类型在游戏中对应的物品实例

    AllResourceType(Item item, int maxAmount) {
        this.correspondingItem = item;
        this.maxAmount = maxAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    /**
     * 获取此资源类型对应的虚拟物品实例。
     *
     * @return 对应的Item对象，用于生成ItemStack。
     */
    public Item getCorrespondingItem() {
        return this.correspondingItem;
    }

    public static List<AllResourceType> getNormalResourceType() {
        return List.of(DIRT, WOOD, STONE, IRON);
    }

    public static List<AllResourceType> getPreciousResourceType() {
        return List.of(GOLD, DIAMOND, NETHERITE);
    }

    public static List<AllResourceType> getNoCompressionResourceType() {
        return List.of(IRON, GOLD, DIAMOND, NETHERITE);
    }

}