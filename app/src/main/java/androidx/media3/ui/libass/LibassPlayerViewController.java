package androidx.media3.ui.libass;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.libass.LibassPlaybackSession;
import androidx.media3.exoplayer.libass.LibassSubtitleController;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;

import java.util.function.Consumer;

/**
 * Compile-compatible stub for the private Media3 fork's libass player view controller.
 * Bindings and style overrides are accepted but never applied because libass is unavailable.
 */
public final class LibassPlayerViewController {

    private final Player player;
    private final LibassPlaybackSession session;
    private final LibassSubtitleController subtitleController;

    @Nullable private PlayerView playerView;
    @Nullable private Consumer<SubtitleView> secondarySubtitleViewConfigurator;

    public LibassPlayerViewController(Player player, LibassPlaybackSession session, LibassSubtitleController subtitleController) {
        this.player = player;
        this.session = session;
        this.subtitleController = subtitleController;
    }

    public void bind(@Nullable PlayerView playerView) {
        this.playerView = playerView;
    }

    public void setStyleOverride(@Nullable CaptionStyleCompat style, @Nullable String fontFamily) {
    }

    public void setSecondarySubtitleViewConfigurator(@Nullable Consumer<SubtitleView> configurator) {
        this.secondarySubtitleViewConfigurator = configurator;
    }

    public void close() {
        playerView = null;
        secondarySubtitleViewConfigurator = null;
    }
}
