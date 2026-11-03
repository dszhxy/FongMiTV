package androidx.media3.exoplayer.source;

import androidx.media3.common.C;

/**
 * Compile-compatible stub for the private Media3 fork's end trim source.
 * The trim duration is retained for callers but playback is not trimmed.
 */
public final class EndTrimMediaSource extends WrappingMediaSource {

    private long endTrimDurationMs;

    public EndTrimMediaSource(MediaSource mediaSource, long endTrimDurationMs) {
        super(mediaSource);
        this.endTrimDurationMs = endTrimDurationMs;
    }

    public void setEndTrimDurationMs(long endTrimDurationMs) {
        this.endTrimDurationMs = endTrimDurationMs;
    }

    public long getEndTrimDurationMs() {
        return endTrimDurationMs;
    }

    public boolean isEndTrimActive() {
        return endTrimDurationMs > 0;
    }

    public long getOriginalDurationMs() {
        return C.TIME_UNSET;
    }
}
