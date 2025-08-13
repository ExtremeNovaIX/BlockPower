package BlockPower.ModEntities.RushMinecart;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MinecartRenderer;

public class RushMinecartRenderer extends MinecartRenderer<RushMinecartEntity> {
    public RushMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.MINECART);
    }
}