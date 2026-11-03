package androidx.media3.exoplayer.libass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Compile-compatible stub for the private Media3 fork's libass fontconfig writer.
 * The generated file is empty because no font discovery is implemented here.
 */
public final class LibassFontConfig {

    private LibassFontConfig() {
    }

    public static void write(File output, File cacheDirectory, List<File> fontDirectories) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(output)) {
            stream.flush();
        }
    }
}
