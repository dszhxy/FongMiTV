package androidx.media3.exoplayer.audio;

import android.content.Context;

import androidx.media3.common.audio.AudioEffectParameters;
import androidx.media3.common.audio.AudioEffectProcessor;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * Compile-compatible stub for the private Media3 fork's audio equalizer controller.
 * Equalization is not applied; the wrapped processor stays inactive.
 */
public final class AudioEqualizerController {

    private final AudioEffectProcessor processor = new AudioEffectProcessor();

    public AudioEqualizerController(Context context) {
    }

    public boolean apply(ExoPlayer player, AudioEffectParameters parameters) {
        processor.setParameters(parameters, false);
        return true;
    }

    public void release() {
        processor.resetParameters();
    }
}
