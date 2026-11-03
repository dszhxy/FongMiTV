package androidx.media3.exoplayer.iso;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.media3.common.DiscMenuController;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;

/**
 * Compile-compatible stub for the private Media3 fork's ISO navigation controller.
 * Navigation playback is never started; media sources pass through unchanged.
 */
public final class IsoNavigationPlayerController implements DiscMenuController {

    private final ExoPlayer player;

    public IsoNavigationPlayerController(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    @Nullable
    public MediaSource prepareMediaSource(Context context, @Nullable MediaSource mediaSource, @Nullable DataSource.Factory dataSourceFactory) {
        return mediaSource;
    }

    public boolean isStarted() {
        return false;
    }

    public void close() {
        // No-op: compatibility layer does not own navigation playback.
    }
}
