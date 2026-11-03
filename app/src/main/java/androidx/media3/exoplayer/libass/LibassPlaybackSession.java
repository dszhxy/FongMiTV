package androidx.media3.exoplayer.libass;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.text.SubtitleTranscript;
import androidx.media3.extractor.ExtractorsFactory;
import androidx.media3.extractor.text.SubtitleParser;

/**
 * Compile-compatible stub for the private Media3 fork's libass playback session.
 * Libass is never reported as available, so none of the render or media hooks are used.
 */
public final class LibassPlaybackSession {

    public LibassPlaybackSession(LibassConfiguration configuration, boolean enabled) {
    }

    public boolean isAvailable() {
        return false;
    }

    @Nullable
    public Renderer createClockRenderer() {
        return null;
    }

    public MediaComponents createMediaComponents(MediaItem mediaItem, ExtractorsFactory extractorsFactory, SubtitleTranscript transcript) {
        return new MediaComponents(extractorsFactory, SubtitleParser.Factory.UNSUPPORTED);
    }

    public void setPreloadMediaItem(@Nullable MediaItem mediaItem) {
    }

    public void setBottomPositionFraction(float bottomPositionFraction) {
    }

    public void setSecondaryBottomPositionFraction(float bottomPositionFraction) {
    }

    public void setFontScale(float fontScale, boolean applyToEmbeddedSubtitles) {
    }

    public void close() {
    }

    /** Extractor and subtitle parser pair produced for a libass-aware media source. */
    public static final class MediaComponents {

        public final ExtractorsFactory extractorsFactory;
        public final SubtitleParser.Factory subtitleParserFactory;

        public MediaComponents(ExtractorsFactory extractorsFactory, SubtitleParser.Factory subtitleParserFactory) {
            this.extractorsFactory = extractorsFactory;
            this.subtitleParserFactory = subtitleParserFactory;
        }
    }
}
