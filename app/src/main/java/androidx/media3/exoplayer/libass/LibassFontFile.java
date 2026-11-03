package androidx.media3.exoplayer.libass;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Compile-compatible stub for the private Media3 fork's font file metadata reader.
 * No font parsing is performed, so the family name is always unknown.
 */
public final class LibassFontFile {

    private LibassFontFile() {
    }

    @Nullable
    public static String getFamilyName(File file) throws IOException {
        return null;
    }
}
