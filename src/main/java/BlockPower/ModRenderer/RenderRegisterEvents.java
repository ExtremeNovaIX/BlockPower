package BlockPower.ModRenderer;

import BlockPower.ModEntities.DropAnvil.DropAnvilRenderer;
import BlockPower.ModEntities.ModEntities;
import BlockPower.ModEntities.RushMinecart.FakeRailRenderer;
import BlockPower.ModEntities.RushMinecart.RushMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static BlockPower.Main.Main.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderRegisterEvents {
    @SubscribeEvent
    public static void onRenderRegister(final EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(ModEntities.FAKE_RAIL_ENTITY.get(), FakeRailRenderer::new);
        EntityRenderers.register(ModEntities.RUSH_MINECART.get(), RushMinecartRenderer::new);
        EntityRenderers.register(ModEntities.DROP_ANVIL.get(), DropAnvilRenderer::new);
    }
}
