package BlockPower.Util.PlaySounds;

import BlockPower.DTO.S2C.PlaySoundData;
import BlockPower.ModSounds.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class PlaySounds {

    public static void playSound(PlaySoundData playSoundData) {
        if (Minecraft.getInstance().level != null) {
            if (Minecraft.getInstance().level.isClientSide()) {
                Minecraft.getInstance().level.playSound(null, playSoundData.getX(), playSoundData.getY(), playSoundData.getZ(),
                        ModSounds.MINECART_CRASH.get(), SoundSource.PLAYERS,
                        playSoundData.getVolume(), playSoundData.getPitch());
            }
        }
    }

}

