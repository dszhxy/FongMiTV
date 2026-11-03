package androidx.media3.datasource;

import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;

/**
 * Compile-compatible stub for the private Media3 fork's progressive ISO cache.
 */
public final class ProgressiveIsoCache {

    private ProgressiveIsoCache() {
    }

    /**
     * Stubbed to return the supplied factory unchanged; disk caching is left to the caller.
     */
    public static DataSource.Factory withDiskCache(CacheDataSource.Factory factory, Cache cache) {
        return factory;
    }
}
