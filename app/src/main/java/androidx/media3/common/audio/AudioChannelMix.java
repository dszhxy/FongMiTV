package androidx.media3.common.audio;

/**
 * Compile-compatible stub for the private Media3 fork's shared channel mix helpers.
 *
 * <p>Delegates to the mixing policy shipped with the bundled mpvplayer fork.
 */
public final class AudioChannelMix {

    private AudioChannelMix() {
    }

    public static float[][] createStereoMix(int channelCount, boolean reverse) {
        return androidx.media3.mpvplayer.audio.AudioChannelMix.createStereoMix(channelCount, reverse);
    }

    public static float[][] createMonoMix(int channelCount) {
        return androidx.media3.mpvplayer.audio.AudioChannelMix.createMonoMix(channelCount);
    }

    public static float[][] createFrontCenterGainMix(int channelCount, float gain) {
        return androidx.media3.mpvplayer.audio.AudioChannelMix.createFrontCenterGainMix(channelCount, gain);
    }

    public static float[][] createFrontBalanceMix(int channelCount, float balance) {
        return androidx.media3.mpvplayer.audio.AudioChannelMix.createFrontBalanceMix(channelCount, balance);
    }

    public static float[][] compose(float[][] first, float[][] second) {
        return androidx.media3.mpvplayer.audio.AudioChannelMix.compose(first, second);
    }
}
