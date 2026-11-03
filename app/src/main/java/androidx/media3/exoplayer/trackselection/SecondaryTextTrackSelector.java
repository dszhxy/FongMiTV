package androidx.media3.exoplayer.trackselection;

import android.content.Context;

/**
 * Compile-compatible stub for the private Media3 fork's secondary text track selector.
 * Behaves as a regular default track selector.
 */
public final class SecondaryTextTrackSelector extends DefaultTrackSelector {

    public SecondaryTextTrackSelector(Context context) {
        super(context);
    }

    public static final class Factory implements TrackSelector.Factory {

        private final TrackSelector.Factory delegate;

        public Factory(TrackSelector.Factory delegate) {
            this.delegate = delegate;
        }

        @Override
        public TrackSelector createTrackSelector(Context context) {
            return new SecondaryTextTrackSelector(context);
        }
    }
}
