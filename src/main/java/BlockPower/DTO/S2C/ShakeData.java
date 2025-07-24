package BlockPower.DTO.S2C;

import BlockPower.DTO.ActionData;

public class ShakeData extends ActionData {
    private int duration;
    private float strength;

    public ShakeData() {}

    public ShakeData(int duration,float strength) {
        this.duration = duration;
        this.strength = strength;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }
}
