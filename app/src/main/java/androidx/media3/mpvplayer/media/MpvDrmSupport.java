package androidx.media3.mpvplayer.media;

import androidx.media3.common.MediaItem;

/**
 * Compile-compatible stub for the private Media3 fork's mpv DRM capability check.
 * mpv has no DRM support in this stub, so protected media always falls back to ExoPlayer.
 */
public final class MpvDrmSupport {

    private MpvDrmSupport() {
    }

    public static boolean supports(MediaItem mediaItem) {
        return false;
    }
}
