package androidx.media3.exoplayer.text;

import android.net.Uri;

import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.text.SubtitleContent;
import androidx.media3.datasource.DataSource;

import java.io.IOException;

/**
 * Compile-compatible stub for the private Media3 fork's default subtitle content loader.
 * Loading is not implemented; unsupported content is returned.
 */
public final class DefaultSubtitleContentLoader implements SubtitleContentLoader {

    private final DataSource.Factory dataSourceFactory;

    public DefaultSubtitleContentLoader(DataSource.Factory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public SubtitleContent load(MediaItem mediaItem, Uri uri, Format format) throws IOException {
        return SubtitleContent.UNSUPPORTED;
    }
}
