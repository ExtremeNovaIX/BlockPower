package BlockPower.Util.PlaySounds;

import BlockPower.DTO.S2C.PlaySoundData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class PlaySounds {

    public static void playSound(PlaySoundData playSoundData) {
        if (Minecraft.getInstance().level != null) {
            ServerPlayer player = (ServerPlayer) Minecraft.getInstance().level.getEntity(playSoundData.getEntityId());
            if (player != null) {
                player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                        Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(playSoundData.getSoundEventLocation()))), SoundSource.PLAYERS,
                        playSoundData.getVolume(), playSoundData.getPitch());
            }
        }
    }

}

