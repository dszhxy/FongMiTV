package androidx.media3.common.audio;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Compile-compatible stub for the private Media3 fork's audio channel mixing modes.
 */
public final class AudioChannelMode {

    public static final int AUTO = 0;
    public static final int STEREO = 1;
    public static final int MONO = 2;
    public static final int REVERSE = 3;

    @IntDef({AUTO, STEREO, MONO, REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    private AudioChannelMode() {
    }

    public static int clamp(int mode) {
        return mode < AUTO || mode > REVERSE ? AUTO : mode;
    }

    public static boolean isAuto(int mode) {
        return mode == AUTO;
    }

    public static boolean isAvailable(int channelCount) {
        return channelCount >= 2;
    }

    public static boolean isAvailable(int mode, int channelCount) {
        return isAvailable(channelCount);
    }

    public static boolean canBalance(int mode) {
        return mode == AUTO || mode == STEREO;
    }

    public static int resolve(int mode, int channelCount) {
        mode = clamp(mode);
        return isAvailable(channelCount) ? mode : AUTO;
    }
}
