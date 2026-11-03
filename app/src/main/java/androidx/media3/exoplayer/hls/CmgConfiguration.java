package androidx.media3.exoplayer.hls;

import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * Compile-compatible stub for the private Media3 fork's CMG configuration.
 * CMG media is never detected, so callers fall back to normal engine selection.
 */
public final class CmgConfiguration {

    public static final CmgConfiguration DEFAULT = new CmgConfiguration();

    public CmgConfiguration() {
    }

    @Nullable
    public static CmgConfiguration parse(String value) {
        return null;
    }

    @Nullable
    public static CmgConfiguration fromMediaUri(Uri uri) {
        return null;
    }
}
