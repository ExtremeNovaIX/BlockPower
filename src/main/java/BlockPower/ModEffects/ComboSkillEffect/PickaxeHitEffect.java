package BlockPower.ModEffects.ComboSkillEffect;

import BlockPower.ModEffects.ITickBasedEffect;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PickaxeHitEffect implements ITickBasedEffect {
    private Entity target;
    private Player player;

    public PickaxeHitEffect(Entity target, Player player) {
        this.target = target;
        this.player = player;
    }

    @Override
    public void tick() {
        if (target instanceof ServerPlayer serverPlayer) {
            // 计算从玩家当前位置指向目标位置往上一格的矢量
            Vec3 desiredVelocity = serverPlayer.position().add(0, 1, 0).subtract(player.position());
            // 让玩家被目标吸引
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), desiredVelocity));
        }
    }

    @Override
    public boolean isFinished() {
        return player.distanceTo(target) <= 3;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void onEnd() {
        //播放声音
    }
}
