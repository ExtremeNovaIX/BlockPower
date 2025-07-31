package BlockPower.DTO.S2C;

import BlockPower.DTO.ActionData;
import net.minecraft.world.phys.Vec3;

public class CrossStarRenderData extends ActionData {
    int duration;
    double x;
    double y;
    double z;

    public CrossStarRenderData() {
    }

    public CrossStarRenderData(int duration, Vec3 vec3) {
        this.duration = duration;
        this.x = vec3.x;
        this.y = vec3.y;
        this.z = vec3.z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
