package androidx.media3.exoplayer.source;

import androidx.media3.common.MediaItem;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider;
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy;

/**
 * Compile-compatible stub for the private Media3 fork's content type resolving source.
 * Content type resolution is delegated to the wrapped source.
 */
public final class ContentTypeResolvingMediaSource extends WrappingMediaSource {

    private ContentTypeResolvingMediaSource(MediaSource mediaSource) {
        super(mediaSource);
    }

    public static final class Factory implements MediaSource.Factory {

        private final MediaSource.Factory factory;
        private final DataSource.Factory dataSourceFactory;

        public Factory(MediaSource.Factory factory, DataSource.Factory dataSourceFactory) {
            this.factory = factory;
            this.dataSourceFactory = dataSourceFactory;
        }

        @Override
        public MediaSource.Factory setDrmSessionManagerProvider(DrmSessionManagerProvider drmSessionManagerProvider) {
            factory.setDrmSessionManagerProvider(drmSessionManagerProvider);
            return this;
        }

        @Override
        public MediaSource.Factory setLoadErrorHandlingPolicy(LoadErrorHandlingPolicy loadErrorHandlingPolicy) {
            factory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy);
            return this;
        }

        @Override
        public int[] getSupportedTypes() {
            return factory.getSupportedTypes();
        }

        @Override
        public MediaSource createMediaSource(MediaItem mediaItem) {
            return new ContentTypeResolvingMediaSource(factory.createMediaSource(mediaItem));
        }
    }
}
