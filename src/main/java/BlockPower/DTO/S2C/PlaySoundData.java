package BlockPower.DTO.S2C;

import BlockPower.DTO.ActionData;
import BlockPower.ModMessages.ClientAction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public class PlaySoundData extends ActionData {
    private int entityId;
    private String soundEventLocation;
    private float volume;
    private float pitch;

    public PlaySoundData(Entity entity, SoundEvent soundEvent, float volume, float pitch) {
        this.entityId = entity.getId();
        this.soundEventLocation = soundEvent.getLocation().toString();
        this.volume = volume;
        this.pitch = pitch;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getSoundEventLocation() {
        return soundEventLocation;
    }

    public void setSoundEventLocation(String soundEventLocation) {
        this.soundEventLocation = soundEventLocation;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
