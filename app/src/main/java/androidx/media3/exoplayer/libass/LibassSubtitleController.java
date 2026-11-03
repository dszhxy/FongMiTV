package androidx.media3.exoplayer.libass;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.text.SubtitleSelectionState;
import androidx.media3.exoplayer.text.SecondaryTextOutput;
import androidx.media3.exoplayer.trackselection.SecondaryTextTrackSelector;

/**
 * Compile-compatible stub for the private Media3 fork's libass subtitle controller.
 * Secondary track selection is ignored and no selection state is tracked.
 */
public final class LibassSubtitleController {

    public LibassSubtitleController(Player player, LibassPlaybackSession session, SecondaryTextTrackSelector.Factory secondaryTextTrackSelectorFactory, SecondaryTextOutput secondaryTextOutput) {
    }

    public SubtitleSelectionState getSubtitleSelectionState() {
        return SubtitleSelectionState.EMPTY;
    }

    public void setSecondaryTextTrackSelectionOverride(@Nullable TrackSelectionOverride selection) {
    }

    public void setSecondaryTextTrackAutoSelectionEnabled(boolean enabled) {
    }

    public void close() {
    }
}
