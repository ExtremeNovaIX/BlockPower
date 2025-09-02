package BlockPower.ModMessages.C2SPacket.SkillPacket;


import BlockPower.ModEntities.RushMinecart.RushMinecartEntity;

import BlockPower.Skills.RushMinecartSkill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnRushMinecartPacket_C2S extends AbstractSkillPacket_C2S {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnRushMinecartPacket_C2S.class);

    public SpawnRushMinecartPacket_C2S() {
        super(new RushMinecartSkill());
    }

    public SpawnRushMinecartPacket_C2S(FriendlyByteBuf buf) {
        super(new RushMinecartSkill());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        LOGGER.info("createRushMinecart by player: {}", player.getGameProfile().getName());
        RushMinecartEntity.createRushMinecart(player);
        consumeResource(player, skill);
    }
}
