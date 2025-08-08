package BlockPower.ModSounds;

import BlockPower.Main.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MOD_ID);

    public static final RegistryObject<SoundEvent> ANVIL_SOUND = registerSoundEvent("anvil_sound");
    public static final RegistryObject<SoundEvent> FLY_SOUND = registerSoundEvent("fly_sound");
    public static final RegistryObject<SoundEvent> HIT_SOUND = registerSoundEvent("hit_sound");
    public static final RegistryObject<SoundEvent> KILL_FINISH_SOUND = registerSoundEvent("kill_finish_sound");
    public static final RegistryObject<SoundEvent> MAGMA_BLOCK_SOUND = registerSoundEvent("magma_block_sound");
    public static final RegistryObject<SoundEvent> MINECART_CRASH_SOUND = registerSoundEvent("minecart_crash_sound");
    public static final RegistryObject<SoundEvent> SWEEPING_KNOCKBACK_SOUND = registerSoundEvent("sweeping_knockback_sound");


    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}

