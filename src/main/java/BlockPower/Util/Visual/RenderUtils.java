package BlockPower.Util.Visual;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

public class RenderUtils {
    public static void drawTexturedQuad(VertexConsumer buffer, Matrix4f pose, float r, float g, float b, float a, int packedLight) {
        // 这个辅助方法正确地提供了我们需要的4项数据
        buffer.vertex(pose, -0.5f, -0.5f, 0).color(r, g, b, a).uv(0, 1).uv2(packedLight).endVertex();
        buffer.vertex(pose, 0.5f, -0.5f, 0).color(r, g, b, a).uv(1, 1).uv2(packedLight).endVertex();
        buffer.vertex(pose, 0.5f, 0.5f, 0).color(r, g, b, a).uv(1, 0).uv2(packedLight).endVertex();
        buffer.vertex(pose, -0.5f, 0.5f, 0).color(r, g, b, a).uv(0, 0).uv2(packedLight).endVertex();
    }
}