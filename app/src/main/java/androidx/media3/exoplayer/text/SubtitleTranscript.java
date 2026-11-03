package androidx.media3.exoplayer.text;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;

/**
 * Compile-compatible stub for the private Media3 fork's subtitle transcript.
 */
public final class SubtitleTranscript {

    @Nullable
    private final MediaItem mediaItem;

    public SubtitleTranscript() {
        this(null);
    }

    public SubtitleTranscript(@Nullable MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }

    @Nullable
    public MediaItem getMediaItem() {
        return mediaItem;
    }
}
