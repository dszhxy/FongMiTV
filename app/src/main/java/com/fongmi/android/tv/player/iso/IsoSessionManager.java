package com.fongmi.android.tv.player.iso;

import android.util.Log;

import com.github.catvod.crawler.SpiderDebug;

import is.xyz.mpv.MPVLib;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class IsoSessionManager {

    private static final String TAG = "TV-iso-native";
    private static final String URI_PREFIX = "media3iso://";
    private static final String URI_SUFFIX = "/longest";

    private static final AtomicLong NEXT_ID = new AtomicLong(1000);
    private static final Map<Long, IsoPlaybackSession> SESSIONS = new ConcurrentHashMap<>();
    private static final ExecutorService PROBE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "iso-probe");
        thread.setDaemon(true);
        return thread;
    });
    private static final ExecutorService METADATA_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "iso-track-metadata");
        thread.setDaemon(true);
        return thread;
    });

    private IsoSessionManager() {
    }

    public static String create(String url, Map<String, String> headers) throws Exception {
        long id = NEXT_ID.incrementAndGet();
        IsoPlaybackSession session = new IsoPlaybackSession(id, url, headers);
        SESSIONS.put(id, session);
        registerStream(id);
        SpiderDebug.log("iso-native", "session create id=%d", id);
        return uriFor(id);
    }

    /** Probe opaque local pan-proxy URLs without relying on filename or MIME. */
    public static String probeAndCreate(String url, Map<String, String> headers) {
        long id = NEXT_ID.incrementAndGet();
        IsoPlaybackSession session = new IsoPlaybackSession(id, url, headers);
        try {
            if (!session.hasDiscImageSignature()) {
                session.close();
                Log.i(TAG, "opaque source probe: not an ISO image");
                return null;
            }
            SESSIONS.put(id, session);
            registerStream(id);
            Log.i(TAG, "opaque source probe: ISO signature found, session=" + id);
            return uriFor(id);
        } catch (Throwable e) {
            session.close();
            Log.w(TAG, "opaque source probe failed: " + e.getClass().getSimpleName());
            return null;
        }
    }

    public static void probeAndCreateAsync(String url, Map<String, String> headers, Consumer<String> callback) {
        PROBE_EXECUTOR.execute(() -> callback.accept(probeAndCreate(url, headers)));
    }

    public static void close(long id) {
        MPVLib.unregisterStream(uriFor(id));
        IsoPlaybackSession session = SESSIONS.remove(id);
        if (session != null) {
            session.close();
            SpiderDebug.log("iso-native", "session close id=%d", id);
        }
    }

    public static void closeUri(String uri) {
        close(parseId(uri));
    }

    public static long length(long id) {
        IsoPlaybackSession session = SESSIONS.get(id);
        if (session == null) return -1;
        try {
            return session.length();
        } catch (Throwable e) {
            SpiderDebug.log("iso-source", "length failed id=%d error=%s", id, e.getMessage());
            return -1;
        }
    }

    public static int readAt(long id, long offset, ByteBuffer target, int length) {
        IsoPlaybackSession session = SESSIONS.get(id);
        if (session == null || target == null) return -1;
        try {
            return session.readAt(offset, target, length);
        } catch (Throwable e) {
            SpiderDebug.log("iso-source", "read failed id=%d offset=%d length=%d error=%s", id, offset, length, e.getMessage());
            return -1;
        }
    }

    /** Called by the native Blu-ray stream after libbluray selects the exact playlist. */
    public static void prepareTrackMetadata(long id, int playlist) {
        IsoPlaybackSession session = SESSIONS.get(id);
        if (session != null) session.prepareTrackMetadata(playlist, METADATA_EXECUTOR);
    }

    public static String getTrackLanguage(long id, int trackType, int demuxId, int ordinal) {
        IsoPlaybackSession session = SESSIONS.get(id);
        return session == null ? null : session.trackLanguage(trackType, demuxId, ordinal);
    }

    public static void addTrackMetadataListener(long id, Runnable listener) {
        IsoPlaybackSession session = SESSIONS.get(id);
        if (session != null) session.addTrackMetadataListener(listener);
    }

    public static void removeTrackMetadataListener(long id, Runnable listener) {
        IsoPlaybackSession session = SESSIONS.get(id);
        if (session != null) session.removeTrackMetadataListener(listener);
    }

    public static long parseId(String uri) {
        if (uri == null) return -1;
        int scheme = uri.indexOf("://");
        int start = scheme < 0 ? 0 : scheme + 3;
        int end = uri.indexOf('/', start);
        if (end < 0) end = uri.length();
        try {
            return Long.parseLong(uri.substring(start, end));
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static String uriFor(long id) {
        return URI_PREFIX + id + URI_SUFFIX;
    }

    private static void registerStream(long id) {
        String uri = uriFor(id);
        MPVLib.registerStream(uri, () -> new IsoStream(id));
    }

    private static final class IsoStream implements MPVLib.Stream {

        private final long id;
        private long position;

        private IsoStream(long id) {
            this.id = id;
        }

        @Override
        public int read(ByteBuffer buffer) {
            if (buffer == null) return -1;
            int read = IsoSessionManager.readAt(id, position, buffer, buffer.remaining());
            if (read > 0) position += read;
            return read;
        }

        @Override
        public long seek(long target) {
            position = Math.max(0, target);
            return position;
        }

        @Override
        public long size() {
            return IsoSessionManager.length(id);
        }

        @Override
        public void cancel() {
        }

        @Override
        public void close() {
        }
    }
}
