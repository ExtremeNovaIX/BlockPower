package BlockPower.Events;

import BlockPower.Capability.KBPercent.IKBPercentData;
import BlockPower.Capability.KBPercent.KBPercentProvider;
import BlockPower.Capability.ModCapabilities;
import BlockPower.Main.Main;
import BlockPower.ModMessages.ModMessages;
import BlockPower.ModMessages.S2CPacket.SyncKnockbackValueS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity) {
            if (!entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).isPresent()) {
                // 回调：当服务器端Capability值变化时，更新同步的EntityData
                Runnable onValueUpdate = () -> {
                    entity.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
                        // 通过Mixin接口安全地获取Accessor并设置值
                        if (entity instanceof IKBPercentData data) {
                            entity.getEntityData().set(data.getKBPercentDataAccessor(), (float)cap.getKBPercent());
                        }

                        // 如果是玩家，额外发送一个数据包以确保HUD的实时性
                        if (entity instanceof ServerPlayer serverPlayer) {
                            ModMessages.sendToPlayer(new SyncKnockbackValueS2CPacket(cap.getKBPercent()), serverPlayer);
                        }
                    });
                };

                // 对于非玩家生物，只在服务器端附加带回调的Provider
                if (!(entity instanceof Player)) {
                    if (!entity.level().isClientSide()) {
                        event.addCapability(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "knockback_value"), new KBPercentProvider(onValueUpdate));
                    }
                } else {
                    // 对于玩家，两端都附加，服务器端带回调
                    if (!entity.level().isClientSide()) {
                        event.addCapability(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "knockback_value"), new KBPercentProvider(onValueUpdate));
                    } else {
                        event.addCapability(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "knockback_value"), new KBPercentProvider());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            // 如果玩家是因为其他原因被克隆（例如从末地返回），复制数据以保持状态
            event.getOriginal().getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(newStore -> {
                    newStore.deserializeNBT(oldStore.serializeNBT());
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
                ModMessages.sendToPlayer(new SyncKnockbackValueS2CPacket(cap.getKBPercent()), serverPlayer);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
                ModMessages.sendToPlayer(new SyncKnockbackValueS2CPacket(cap.getKBPercent()), serverPlayer);
            });
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            event.getEntity().getCapability(ModCapabilities.KNOCKBACK_VALUE_CAPABILITY).ifPresent(cap -> {
                cap.tick(event.getEntity().level().getGameTime());
            });
        }
    }
}
