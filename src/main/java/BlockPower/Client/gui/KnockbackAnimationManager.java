package BlockPower.Client.gui;

import BlockPower.Util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class KnockbackAnimationManager {

    private static final int ANIMATION_DURATION_TICKS = 14;
    private static final float SHAKE_THRESHOLD = 0.05f;
    private static final Map<UUID, AnimationState> entityAnimationStates = new WeakHashMap<>();
    private static final AnimationState playerAnimationState = new AnimationState();

    public static AnimationState getAndUpdateEntityState(LivingEntity entity, float currentValue) {
        AnimationState state = entityAnimationStates.computeIfAbsent(entity.getUUID(), k -> new AnimationState());
        state.update(currentValue);
        return state;
    }

    public static AnimationState getAndUpdatePlayerState(float currentValue) {
        playerAnimationState.update(currentValue);
        return playerAnimationState;
    }

    public static class AnimationState {
        float lastValue = 0;
        long animationStartTick = -ANIMATION_DURATION_TICKS - 1;

        private void update(float currentValue) {
            if (currentValue > this.lastValue) {
                long currentGameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
                this.animationStartTick = currentGameTime;
            }
            this.lastValue = currentValue;
        }

        private float getAnimationProgress() {
            long currentGameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
            long ticksSinceStart = currentGameTime - this.animationStartTick;
            if (ticksSinceStart >= ANIMATION_DURATION_TICKS) {
                return 1.0f;
            }
            return (float) ticksSinceStart / ANIMATION_DURATION_TICKS;
        }

        /**
         * 获取抖动强度，这是一个从1.0平滑衰减到0.0的值。
         */
        public float getShakeIntensity() {
            float progress = getAnimationProgress();
            if (progress >= 1.0f) return 0.0f;

            float intensity = 1.0f - MathUtils.easeOutQuart(progress);
            return (intensity < SHAKE_THRESHOLD) ? 0.0f : intensity;
        }

        /**
         * 获取颜色插值强度，这是一个快速达到1.0然后回落到0.0的值。
         * 使用sin函数的前半个周期来实现这个效果。
         */
        public float getColorIntensity() {
            float progress = getAnimationProgress();
            if (progress >= 1.0f) return 0.0f;

            // 使用 sin(progress * PI) 来创建一个 0 -> 1 -> 0 的曲线
            return (float) Math.sin(progress * Math.PI);
        }
    }
}
