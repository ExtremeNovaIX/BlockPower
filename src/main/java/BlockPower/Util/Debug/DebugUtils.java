package BlockPower.Util.Debug;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DebugUtils {

    private static final ParticleOptions PARTICLE = ParticleTypes.FLAME;
    private static final double DENSITY = 0.3;

    /**
     * Draws a more accurate representation of the raycast volume for debugging purposes.
     * This should only be called on the server side.
     *
     * @param level    The level to draw the particles in.
     * @param startPos The starting position of the raycast.
     * @param endPos   The ending position of the raycast.
     * @param radius   The radius of the raycast.
     */
    public static void drawRaycastBox(Level level, Vec3 startPos, Vec3 endPos, double radius) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 path = endPos.subtract(startPos);
        double length = path.length();
        Vec3 direction = path.normalize();

        // Draw the central ray
        for (double i = 0; i < length; i += DENSITY) {
            Vec3 point = startPos.add(direction.scale(i));
            serverLevel.sendParticles(PARTICLE, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }

        // Draw cross markers at start, middle, and end
        drawCrossMarker(serverLevel, startPos, radius);
        drawCrossMarker(serverLevel, startPos.add(direction.scale(length / 2.0)), radius);
        drawCrossMarker(serverLevel, endPos, radius);
    }

    /**
     * Draws a cross shape (+) at a specific point to represent the radius.
     *
     * @param level  The server level to draw in.
     * @param center The center of the cross.
     * @param radius The radius (size) of the cross arms.
     */
    private static void drawCrossMarker(ServerLevel level, Vec3 center, double radius) {
        // Draw X-axis arm
        for (double i = -radius; i <= radius; i += DENSITY) {
            level.sendParticles(PARTICLE, center.x + i, center.y, center.z, 1, 0, 0, 0, 0);
        }
        // Draw Y-axis arm
        for (double i = -radius; i <= radius; i += DENSITY) {
            level.sendParticles(PARTICLE, center.x, center.y + i, center.z, 1, 0, 0, 0, 0);
        }
        // Draw Z-axis arm
        for (double i = -radius; i <= radius; i += DENSITY) {
            level.sendParticles(PARTICLE, center.x, center.y, center.z + i, 1, 0, 0, 0, 0);
        }
    }
}
