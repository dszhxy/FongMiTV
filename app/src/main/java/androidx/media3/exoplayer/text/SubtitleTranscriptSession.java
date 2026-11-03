package androidx.media3.exoplayer.text;

import androidx.annotation.Nullable;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.text.SubtitleContent;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * Compile-compatible stub for the private Media3 fork's subtitle transcript session.
 * Transcripts are never resolved; unsupported content is returned.
 */
public final class SubtitleTranscriptSession {

    @Nullable
    private SubtitleContentLoader loader;
    @Nullable
    private ExoPlayer player;
    @Nullable
    private MediaItem preloadMediaItem;
    private boolean contentEnabled = true;

    public SubtitleTranscriptSession() {
    }

    public void setLoader(SubtitleContentLoader loader) {
        this.loader = loader;
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public void setPreloadMediaItem(@Nullable MediaItem mediaItem) {
        this.preloadMediaItem = mediaItem;
    }

    public SubtitleTranscript forMediaItem(MediaItem mediaItem) {
        return new SubtitleTranscript(mediaItem);
    }

    public SubtitleContent getContent(Format format, long offsetMs) {
        return SubtitleContent.UNSUPPORTED;
    }

    public void setContentEnabled(boolean enabled) {
        this.contentEnabled = enabled;
    }

    public boolean canRetryContent(Format format) {
        return false;
    }

    public void retryContent(Format format) {
        // No-op: transcript loading is not implemented.
    }

    public void close() {
        loader = null;
        player = null;
        preloadMediaItem = null;
    }
}
