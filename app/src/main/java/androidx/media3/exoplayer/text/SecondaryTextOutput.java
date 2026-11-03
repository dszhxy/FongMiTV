package androidx.media3.exoplayer.text;

import androidx.annotation.NonNull;
import androidx.media3.common.text.CueGroup;

/**
 * Compile-compatible stub for the private Media3 fork's secondary text output.
 * Received cues are discarded.
 */
public final class SecondaryTextOutput implements TextOutput {

    @Override
    public void onCues(@NonNull CueGroup cueGroup) {
        // No-op: secondary subtitle rendering is not implemented.
    }
}
