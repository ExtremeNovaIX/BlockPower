package BlockPower.ModMessages.C2SPacket.SkillPacket;

import BlockPower.ModEntities.DropAnvil.DropAnvilEntity;
import BlockPower.ModMessages.C2SPacket.AbstractC2SPacket;
import BlockPower.Skills.SkillTrigger.DropAnvilSkill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnDropAnvilPacket_C2S extends AbstractSkillPacket_C2S {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnDropAnvilPacket_C2S.class);

    public SpawnDropAnvilPacket_C2S() {
        super(new DropAnvilSkill());
    }

    public SpawnDropAnvilPacket_C2S(FriendlyByteBuf buf) {
        super(new DropAnvilSkill());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        LOGGER.info("createDropAnvil by {}", player.getGameProfile().getName());
        DropAnvilEntity.createDropAnvil(player);
        consumeResource(player, skill);
    }
}
