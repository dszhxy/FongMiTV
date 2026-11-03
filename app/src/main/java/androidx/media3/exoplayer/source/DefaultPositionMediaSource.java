package androidx.media3.exoplayer.source;

/**
 * Compile-compatible stub for the private Media3 fork's default position source.
 * The default position is retained for callers but does not affect playback.
 */
public final class DefaultPositionMediaSource extends WrappingMediaSource {

    private final long defaultPositionMs;

    public DefaultPositionMediaSource(MediaSource mediaSource, long defaultPositionMs) {
        super(mediaSource);
        this.defaultPositionMs = defaultPositionMs;
    }

    public long getDefaultPositionMs() {
        return defaultPositionMs;
    }
}
