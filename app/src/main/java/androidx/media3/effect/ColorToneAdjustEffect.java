package androidx.media3.effect;

import androidx.annotation.Nullable;
import androidx.media3.common.Effect;

/**
 * Compile-compatible stub for the private Media3 fork's color tone adjustment effect.
 * It is a no-op effect that only carries a {@link VideoAdjustment}.
 */
public final class ColorToneAdjustEffect implements Effect {

    @Nullable
    private VideoAdjustment adjustment;

    public ColorToneAdjustEffect() {
    }

    public ColorToneAdjustEffect(@Nullable VideoAdjustment adjustment) {
        this.adjustment = adjustment;
    }

    public void setAdjustment(@Nullable VideoAdjustment adjustment) {
        this.adjustment = adjustment;
    }

    @Nullable
    public VideoAdjustment getAdjustment() {
        return adjustment;
    }
}
