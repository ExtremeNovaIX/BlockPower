package BlockPower.ModItems.PixelCore;

public enum PixelCoreSkillState {
    DEFAULT(0.0f),
    ANVIL(1.0f),
    RAIL(2.0f),
    TOOL(3.0f),
    BLOCK(4.0f),
    CROSSBOW(5.0f);

    private final float id;

    PixelCoreSkillState(float id) {
        this.id = id;
    }

    public float getId() {
        return this.id;
    }
}
