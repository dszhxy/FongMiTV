package androidx.media3.exoplayer.iso;

import androidx.media3.common.MediaItem;

/**
 * Compile-compatible stub for the private Media3 fork's ISO disc preparation.
 * Disc menu preparation is never reported as available.
 */
public final class IsoDiscPreparation {

    private IsoDiscPreparation() {
    }

    public static boolean isIso(MediaItem mediaItem) {
        return false;
    }
}
