package BlockPower.Skills.MinerState;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * 定义了玩家可以收集的资源类型。
 * 枚举在此处声明的顺序决定了资源在UI资源条上的渲染顺序。
 */
public enum ResourceType {
    DIRT(Items.DIRT),
    WOOD(Items.OAK_PLANKS),
    STONE(Items.COBBLESTONE),
    IRON(Items.RAW_IRON),
    GOLD(Items.RAW_GOLD);

    // 每种资源类型在游戏中对应的物品实例
    private final Item correspondingItem;

    ResourceType(Item item) {
        this.correspondingItem = item;
    }

    /**
     * 获取此资源类型对应的虚拟物品实例。
     * @return 对应的Item对象，用于生成ItemStack。
     */
    public Item getCorrespondingItem() {
        return this.correspondingItem;
    }
}