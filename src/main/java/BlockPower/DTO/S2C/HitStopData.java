package BlockPower.DTO.S2C;

import BlockPower.DTO.ActionData;

public class HitStopData extends ActionData {
    private int duration;

    public HitStopData() {}

    public HitStopData(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
