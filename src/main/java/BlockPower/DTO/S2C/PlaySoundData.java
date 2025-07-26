package BlockPower.DTO.S2C;

import BlockPower.DTO.ActionData;
import BlockPower.ModMessages.ClientAction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public class PlaySoundData extends ActionData {
    private double x;
    private double y;
    private double z;
    private String soundEventLocation;
    private float volume;
    private float pitch;

    public PlaySoundData(Entity entity, SoundEvent soundEvent, float volume, float pitch) {
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.soundEventLocation = soundEvent.getLocation().toString();
        this.volume = volume;
        this.pitch = pitch;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
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
