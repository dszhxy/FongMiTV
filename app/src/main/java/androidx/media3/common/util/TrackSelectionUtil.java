package androidx.media3.common.util;

import androidx.media3.common.Player;

import java.util.List;

/**
 * Compile-compatible stub for the private Media3 fork's track selection helpers.
 * The operations are accepted but intentionally not applied by this compatibility layer.
 */
public final class TrackSelectionUtil {

    private TrackSelectionUtil() {
    }

    public static boolean cycleTrack(Player player, int trackType, boolean forward, boolean includeDisabled) {
        return false;
    }

    public static boolean cycleTrack(Player player, int trackType, List<String> trackIds, boolean forward, boolean includeDisabled) {
        return false;
    }

    public static boolean setTrackTypeDisabled(Player player, int trackType, boolean disabled) {
        return false;
    }

    public static boolean resetTrackSelection(Player player, int trackType) {
        return false;
    }

    public static boolean selectTrack(Player player, int trackType, String trackId) {
        return false;
    }
}
