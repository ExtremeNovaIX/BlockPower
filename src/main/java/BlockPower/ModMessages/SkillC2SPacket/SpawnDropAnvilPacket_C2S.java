<<<<<<<< HEAD:src/main/java/BlockPower/ModMessages/C2SPacket/SkillPacket/SpawnDropAnvilPacket_C2S.java
package BlockPower.ModMessages.C2SPacket.SkillPacket;
========
package BlockPower.ModMessages.SkillC2SPacket;
>>>>>>>> ExNova:src/main/java/BlockPower/ModMessages/SkillC2SPacket/SpawnDropAnvilPacket_C2S.java

import BlockPower.ModEntities.DropAnvil.DropAnvilEntity;
import BlockPower.Skills.DropAnvilSkill;
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
<<<<<<<< HEAD:src/main/java/BlockPower/ModMessages/C2SPacket/SkillPacket/SpawnDropAnvilPacket_C2S.java
        consumeResource(player, skill);
========
        super.consumeResource(player, skill);
>>>>>>>> ExNova:src/main/java/BlockPower/ModMessages/SkillC2SPacket/SpawnDropAnvilPacket_C2S.java
    }
}
