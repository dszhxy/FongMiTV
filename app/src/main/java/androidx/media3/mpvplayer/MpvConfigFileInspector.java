package androidx.media3.mpvplayer;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * Compile-compatible stub for the private Media3 fork's mpv.conf inspector.
 * Configuration files are never parsed, so no options or font directories are reported.
 */
public final class MpvConfigFileInspector {

    private final File configDirectory;

    public MpvConfigFileInspector(File configDirectory) {
        this.configDirectory = configDirectory;
    }

    @Nullable
    public File readSubtitleFontsDirectory() {
        return null;
    }

    public Set<String> getEffectiveOptionNames(CharSequence content) {
        return Collections.emptySet();
    }
}
