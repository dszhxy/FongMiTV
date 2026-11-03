package androidx.media3.effect;

import androidx.annotation.Nullable;
import androidx.media3.common.Effect;

/**
 * Compile-compatible stub for the private Media3 fork's detail adjustment effect.
 * It is a no-op effect that only carries a {@link VideoAdjustment}.
 */
public final class DetailAdjustEffect implements Effect {

    @Nullable
    private VideoAdjustment adjustment;

    public DetailAdjustEffect() {
    }

    public DetailAdjustEffect(@Nullable VideoAdjustment adjustment) {
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
