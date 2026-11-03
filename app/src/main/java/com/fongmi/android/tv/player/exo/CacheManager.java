package com.fongmi.android.tv.player.exo;

import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.setting.PreloadSetting;
import com.fongmi.android.tv.utils.FileUtil;
import com.github.catvod.utils.Path;

import java.io.File;

/**
 * Ported from FusionTV-KitKat's player.exo.CacheManager.
 * The KitKat build uses NoOpCacheEvictor; here the eviction policy is kept bounded so the
 * cache cannot grow without limit on long-running devices.
 */
public class CacheManager {

    private static final int CACHE_SPACE_PERCENT = 80;

    private static Cache cache;
    private static StandaloneDatabaseProvider databaseProvider;

    private CacheManager() {
    }

    public static synchronized Cache getCache() {
        if (cache != null) return cache;
        File dir = Path.exoCache();
        return cache = new SimpleCache(dir, new LeastRecentlyUsedCacheEvictor(getMaxCacheSize(dir)), getDatabaseProvider());
    }

    private static StandaloneDatabaseProvider getDatabaseProvider() {
        if (databaseProvider == null) databaseProvider = new StandaloneDatabaseProvider(App.get());
        return databaseProvider;
    }

    private static long getMaxCacheSize(File dir) {
        long usedBytes = FileUtil.getDirectorySize(dir);
        long availableBytes = Math.max(0, FileUtil.getAvailableStorageSpace(dir));
        long storageBudget = (usedBytes + availableBytes) * CACHE_SPACE_PERCENT / 100;
        return Math.min(PreloadSetting.getSizeBytes(), storageBudget);
    }
}
