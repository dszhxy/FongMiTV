package com.fongmi.android.tv.player.ijk;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.SimpleBasePlayer;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.media.MediaItemFactory;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.setting.IjkPerformanceSetting;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

@UnstableApi
public class IjkPlayerEngine implements PlayerEngine {

    private final IjkPlayer player;
    private int decode;

    public IjkPlayerEngine(int decode, Player.Listener listener) {
        this.decode = decode;
        this.player = new IjkPlayer(App.get(), decode);
        if (listener != null) player.addListener(listener);
    }

    public static boolean isAvailable() {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public Type getType() {
        return Type.IJK;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getAudioChannelCount() {
        return Format.NO_VALUE;
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public void setDecode(int decode) {
        this.decode = decode;
        player.setDecode(decode);
    }

    @Override
    public void start(PlaySpec spec, long startPositionMs) {
        player.setMediaItem(MediaItemFactory.from(spec), Math.max(0L, startPositionMs));
        player.prepare();
        player.play();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public String getErrorMessage(PlaybackException e) {
        return switch (e.errorCode) {
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> "IJK 网络错误";
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "IJK HTTP 错误";
            case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "IJK 文件不存在";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> "IJK 文件损坏";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "IJK 格式不支持";
            case PlaybackException.ERROR_CODE_TIMEOUT -> "IJK 播放超时";
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "IJK 解码器初始化失败";
            case PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> "IJK 解码器查询失败";
            case PlaybackException.ERROR_CODE_DECODING_FAILED -> "IJK 解码失败";
            default -> "IJK 播放错误";
        };
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        return switch (e.errorCode) {
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                    PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FAILED -> ErrorAction.DECODE;
            default -> ErrorAction.FATAL;
        };
    }

    @UnstableApi
    private static final class IjkPlayer extends SimpleBasePlayer {

        private static final long POSITION_INTERVAL_MS = 500;
        private static final int COMMON_BUFFER_BYTES = 1024 * 1024;

        private static final Commands COMMANDS = new Commands.Builder()
                .add(COMMAND_PLAY_PAUSE)
                .add(COMMAND_PREPARE)
                .add(COMMAND_STOP)
                .add(COMMAND_RELEASE)
                .add(COMMAND_SET_REPEAT_MODE)
                .add(COMMAND_GET_CURRENT_MEDIA_ITEM)
                .add(COMMAND_GET_TIMELINE)
                .add(COMMAND_GET_METADATA)
                .add(COMMAND_SET_MEDIA_ITEM)
                .add(COMMAND_CHANGE_MEDIA_ITEMS)
                .add(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                .add(COMMAND_SEEK_TO_DEFAULT_POSITION)
                .add(COMMAND_GET_VOLUME)
                .add(COMMAND_SET_VOLUME)
                .add(COMMAND_SET_SPEED_AND_PITCH)
                .add(COMMAND_GET_TRACKS)
                .add(COMMAND_SET_VIDEO_SURFACE)
                .build();

        private final Context context;
        private final Handler handler;
        private final Runnable positionRunnable = this::refreshPosition;
        @Nullable
        private IjkMediaPlayer mediaPlayer;
        @Nullable
        private MediaItem mediaItem;
        @Nullable
        private PlaybackException playerError;
        @Nullable
        private Object videoOutput;
        @Nullable
        private SurfaceHolder surfaceHolder;
        @Nullable
        private Surface ownedSurface;
        private VideoSize videoSize = VideoSize.UNKNOWN;
        private PlaybackParameters playbackParameters = PlaybackParameters.DEFAULT;
        private Tracks currentTracks = Tracks.EMPTY;
        private boolean textureListenerAttached;
        private boolean prepared;
        private boolean playWhenReady = true;
        private boolean repeatOne;
        private boolean released;
        private int playbackState = Player.STATE_IDLE;
        private int decode;
        private long durationMs = C.TIME_UNSET;
        private long positionMs;
        private long bufferedMs;
        private long pendingSeekMs = C.TIME_UNSET;
        private float volume = 1f;

        private IjkPlayer(Context context, int decode) {
            super(Looper.getMainLooper());
            this.context = context.getApplicationContext();
            this.handler = new Handler(Looper.getMainLooper());
            this.decode = decode;
        }

        void setDecode(int decode) {
            this.decode = decode;
        }

        long getCachedPosition() {
            return Math.max(0, positionMs);
        }

        @Override
        protected State getState() {
            State.Builder builder = new State.Builder()
                    .setAvailableCommands(COMMANDS)
                    .setPlayWhenReady(playWhenReady, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
                    .setPlaybackState(mediaItem == null && playbackState != Player.STATE_ENDED ? Player.STATE_IDLE : playbackState)
                    .setIsLoading(playbackState == Player.STATE_BUFFERING)
                    .setPlayerError(playerError)
                    .setRepeatMode(repeatOne ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF)
                    .setPlaybackParameters(playbackParameters)
                    .setVideoSize(videoSize)
                    .setVolume(volume)
                    .setPlaylist(mediaItem == null ? ImmutableList.of() : ImmutableList.of(mediaItemData(mediaItem)))
                    .setCurrentMediaItemIndex(mediaItem == null ? C.INDEX_UNSET : 0);
            if (mediaItem != null) {
                long position = Math.max(0, positionMs);
                long buffered = Math.max(position, bufferedMs);
                if (durationMs > 0) buffered = Math.min(buffered, durationMs);
                builder.setContentPositionMs(PositionSupplier.getConstant(position));
                builder.setContentBufferedPositionMs(PositionSupplier.getConstant(buffered));
                builder.setTotalBufferedDurationMs(PositionSupplier.getConstant(Math.max(0, buffered - position)));
            }
            return builder.build();
        }

        private MediaItemData mediaItemData(MediaItem item) {
            long duration = durationMs > 0 ? durationMs : C.TIME_UNSET;
            return new MediaItemData.Builder(item.mediaId)
                    .setMediaItem(item)
                    .setMediaMetadata(item.mediaMetadata)
                    .setDurationUs(duration == C.TIME_UNSET ? C.TIME_UNSET : duration * 1000)
                    .setIsSeekable(duration > 0)
                    .setIsDynamic(duration == C.TIME_UNSET)
                    .setTracks(currentTracks)
                    .build();
        }

        @Override
        protected ListenableFuture<?> handleSetMediaItems(List<MediaItem> mediaItems, int startIndex, long startPositionMs) {
            releasePlayer();
            mediaItem = mediaItems.isEmpty() ? null : mediaItems.get(Math.max(0, Math.min(startIndex, mediaItems.size() - 1)));
            positionMs = startPositionMs == C.TIME_UNSET ? 0 : Math.max(0, startPositionMs);
            pendingSeekMs = startPositionMs == C.TIME_UNSET ? C.TIME_UNSET : Math.max(0, startPositionMs);
            durationMs = C.TIME_UNSET;
            bufferedMs = 0;
            videoSize = VideoSize.UNKNOWN;
            currentTracks = Tracks.EMPTY;
            playerError = null;
            playbackState = Player.STATE_IDLE;
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handlePrepare() {
            if (mediaItem == null) {
                playbackState = Player.STATE_ENDED;
                invalidateState();
                return Futures.immediateVoidFuture();
            }
            startInternal(positionMs);
            return Futures.immediateVoidFuture();
        }

        void startInternal(long startPositionMs) {
            releasePlayer();
            if (mediaItem == null) return;
            Uri uri = mediaItem.requestMetadata.mediaUri;
            if (uri == null && mediaItem.localConfiguration != null) uri = mediaItem.localConfiguration.uri;
            if (uri == null) {
                playerError = new PlaybackException("IJK empty uri", null, PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND);
                playbackState = Player.STATE_IDLE;
                invalidateState();
                return;
            }
            try {
                IjkMediaPlayer player = new IjkMediaPlayer();
                mediaPlayer = player;
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setVolume(volume, volume);
                player.setScreenOnWhilePlaying(true);
                player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
                player.setOnPreparedListener(this::onPrepared);
                player.setOnCompletionListener(this::onCompletion);
                player.setOnErrorListener(this::onError);
                player.setOnInfoListener(this::onInfo);
                player.setOnVideoSizeChangedListener(this::onVideoSizeChanged);
                player.setOnBufferingUpdateListener(this::onBufferingUpdate);
                applyOptions(player);
                player.setDataSource(context, uri, headers(mediaItem.requestMetadata.extras));
                positionMs = Math.max(0, startPositionMs);
                pendingSeekMs = startPositionMs > 0 ? startPositionMs : C.TIME_UNSET;
                playbackState = Player.STATE_BUFFERING;
                attachVideoOutput();
                player.prepareAsync();
            } catch (Throwable e) {
                releasePlayer();
                playerError = new PlaybackException(e.getMessage(), e, PlaybackException.ERROR_CODE_IO_UNSPECIFIED);
                playbackState = Player.STATE_IDLE;
            }
            invalidateState();
        }

        private void applyOptions(IjkMediaPlayer player) {
            boolean hard = decode == PlayerEngine.HARD;
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", hard ? 1L : 0L);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1L);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1L);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0L);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", IjkPerformanceSetting.getFrameDropValue());
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", IjkPerformanceSetting.isPacketBuffering() ? 1L : 0L);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", IjkPerformanceSetting.isReconnect() ? 1L : 0L);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "loop", repeatOne ? 1L : 0L);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", (long) IjkPerformanceSetting.getBufferMb() * COMMON_BUFFER_BYTES);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "first-high-water-mark-ms", IjkPerformanceSetting.getFirstWaterMs());
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "next-high-water-mark-ms", IjkPerformanceSetting.getNextWaterMs());
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "last-high-water-mark-ms", IjkPerformanceSetting.getLastWaterMs());
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "video-pictq-size", IjkPerformanceSetting.getPictureQueue());
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", IjkPerformanceSetting.isAccurateSeek() ? 1L : 0L);
            int probe = IjkPerformanceSetting.getProbeMode();
            if (probe == IjkPerformanceSetting.PROBE_FAST) {
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 512 * 1024L);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1000 * 1000L);
            } else if (probe == IjkPerformanceSetting.PROBE_FULL) {
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10 * 1024 * 1024L);
                player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 5 * 1000 * 1000L);
            }
            int transport = IjkPerformanceSetting.getRtspTransport();
            if (transport == IjkPerformanceSetting.RTSP_TCP) player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
            else if (transport == IjkPerformanceSetting.RTSP_UDP) player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "udp");
        }

        private Map<String, String> headers(@Nullable Bundle extras) {
            Map<String, String> headers = new HashMap<>();
            if (extras == null) return headers;
            for (String key : extras.keySet()) {
                String value = extras.getString(key);
                if (value != null) headers.put(key, value);
            }
            return headers;
        }

        private void onPrepared(IMediaPlayer mp) {
            if (released || mp != mediaPlayer) return;
            prepared = true;
            long duration = mp.getDuration();
            durationMs = duration > 0 ? duration : C.TIME_UNSET;
            refreshTracks();
            if (pendingSeekMs != C.TIME_UNSET && pendingSeekMs > 0) {
                try {
                    mp.seekTo(pendingSeekMs);
                    positionMs = pendingSeekMs;
                } catch (Throwable ignored) {
                }
            }
            pendingSeekMs = C.TIME_UNSET;
            if (playbackParameters.speed != 1f) {
                try {
                    mp.setSpeed(playbackParameters.speed);
                } catch (Throwable ignored) {
                }
            }
            playbackState = Player.STATE_READY;
            if (playWhenReady) {
                try {
                    mp.start();
                } catch (Throwable ignored) {
                }
                startPolling();
            }
            invalidateState();
        }

        private void onCompletion(IMediaPlayer mp) {
            if (released || mp != mediaPlayer) return;
            playbackState = Player.STATE_ENDED;
            if (durationMs > 0) positionMs = durationMs;
            stopPolling();
            if (repeatOne) {
                try {
                    mp.seekTo(0);
                    positionMs = 0;
                    mp.start();
                    playbackState = Player.STATE_READY;
                    startPolling();
                } catch (Throwable ignored) {
                }
            }
            invalidateState();
        }

        private boolean onError(IMediaPlayer mp, int what, int extra) {
            if (released || mp != mediaPlayer) return false;
            prepared = false;
            playbackState = Player.STATE_IDLE;
            playerError = new PlaybackException(what + "/" + extra, null, mapError(what));
            stopPolling();
            invalidateState();
            return true;
        }

        private boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (released || mp != mediaPlayer) return false;
            if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                if (playbackState != Player.STATE_ENDED) {
                    playbackState = Player.STATE_BUFFERING;
                    invalidateState();
                }
                return true;
            }
            if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                if (playbackState == Player.STATE_BUFFERING) playbackState = Player.STATE_READY;
                invalidateState();
                return true;
            }
            if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                if (playbackState == Player.STATE_BUFFERING) playbackState = Player.STATE_READY;
                invalidateState();
                return true;
            }
            return false;
        }

        private void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
            if (width <= 0 || height <= 0) return;
            float ratio = sarNum > 0 && sarDen > 0 ? (float) sarNum / sarDen : 1f;
            videoSize = new VideoSize(width, height, ratio);
            refreshTracks();
            invalidateState();
        }

        private void onBufferingUpdate(IMediaPlayer mp, int percent) {
            if (durationMs <= 0) return;
            bufferedMs = durationMs * Math.max(0, Math.min(100, percent)) / 100;
        }

        private void refreshTracks() {
            if (mediaPlayer == null) {
                currentTracks = Tracks.EMPTY;
                return;
            }
            try {
                ITrackInfo[] infos = mediaPlayer.getTrackInfo();
                if (infos == null || infos.length == 0) {
                    currentTracks = Tracks.EMPTY;
                    return;
                }
                List<Tracks.Group> groups = new ArrayList<>();
                for (int i = 0; i < infos.length; i++) {
                    ITrackInfo info = infos[i];
                    int type = toMedia3TrackType(info.getTrackType());
                    if (type == C.TRACK_TYPE_UNKNOWN) continue;
                    Format format = toFormat(info, type);
                    if (format == null) continue;
                    TrackGroup group = new TrackGroup("ijk:" + type + ":" + i, format);
                    groups.add(new Tracks.Group(group, false, new int[]{C.FORMAT_HANDLED}, new boolean[]{true}));
                }
                currentTracks = groups.isEmpty() ? Tracks.EMPTY : new Tracks(groups);
            } catch (Throwable ignored) {
                currentTracks = Tracks.EMPTY;
            }
        }

        private static int toMedia3TrackType(int ijkType) {
            return switch (ijkType) {
                case ITrackInfo.MEDIA_TRACK_TYPE_VIDEO -> C.TRACK_TYPE_VIDEO;
                case ITrackInfo.MEDIA_TRACK_TYPE_AUDIO -> C.TRACK_TYPE_AUDIO;
                case ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE, ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT -> C.TRACK_TYPE_TEXT;
                default -> C.TRACK_TYPE_UNKNOWN;
            };
        }

        private static Format toFormat(ITrackInfo info, int type) {
            try {
                if (info.getFormat() == null) return null;
                Format.Builder builder = new Format.Builder();
                String mime = info.getFormat().getString(ITrackInfo.IMediaFormat.KEY_MIME);
                if (mime != null) builder.setSampleMimeType(mime);
                if (type == C.TRACK_TYPE_VIDEO) {
                    int w = info.getFormat().getInteger(ITrackInfo.IMediaFormat.KEY_WIDTH);
                    int h = info.getFormat().getInteger(ITrackInfo.IMediaFormat.KEY_HEIGHT);
                    if (w > 0) builder.setWidth(w);
                    if (h > 0) builder.setHeight(h);
                }
                String language = info.getLanguage();
                if (language != null && !language.isEmpty()) builder.setLanguage(language);
                return builder.build();
            } catch (Throwable ignored) {
                return null;
            }
        }

        private int mapError(int what) {
            return switch (what) {
                case IMediaPlayer.MEDIA_ERROR_IO -> PlaybackException.ERROR_CODE_IO_UNSPECIFIED;
                case IMediaPlayer.MEDIA_ERROR_MALFORMED -> PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED;
                case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED -> PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED;
                case IMediaPlayer.MEDIA_ERROR_TIMED_OUT -> PlaybackException.ERROR_CODE_TIMEOUT;
                default -> PlaybackException.ERROR_CODE_UNSPECIFIED;
            };
        }

        private void refreshPosition() {
            if (released || mediaPlayer == null || !prepared || !playWhenReady) return;
            if (playbackState != Player.STATE_READY && playbackState != Player.STATE_BUFFERING) return;
            try {
                positionMs = Math.max(0, mediaPlayer.getCurrentPosition());
                long duration = mediaPlayer.getDuration();
                if (duration > 0) durationMs = duration;
                invalidateState();
            } catch (Throwable ignored) {
            }
            handler.postDelayed(positionRunnable, POSITION_INTERVAL_MS);
        }

        private void startPolling() {
            stopPolling();
            handler.postDelayed(positionRunnable, POSITION_INTERVAL_MS);
        }

        private void stopPolling() {
            handler.removeCallbacks(positionRunnable);
        }

        @Override
        protected ListenableFuture<?> handleSetPlayWhenReady(boolean playWhenReady) {
            this.playWhenReady = playWhenReady;
            if (prepared && mediaPlayer != null) {
                try {
                    if (playWhenReady) {
                        if (playbackState == Player.STATE_ENDED && durationMs > 0) {
                            mediaPlayer.seekTo(0);
                            positionMs = 0;
                            playbackState = Player.STATE_READY;
                        }
                        mediaPlayer.start();
                        if (playbackState == Player.STATE_ENDED) playbackState = Player.STATE_READY;
                        startPolling();
                    } else {
                        mediaPlayer.pause();
                        positionMs = Math.max(0, mediaPlayer.getCurrentPosition());
                        stopPolling();
                    }
                } catch (Throwable ignored) {
                }
            }
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleStop() {
            prepared = false;
            stopPolling();
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                } catch (Throwable ignored) {
                }
            }
            playbackState = Player.STATE_IDLE;
            playerError = null;
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleRelease() {
            released = true;
            stopPolling();
            releasePlayer();
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleSetRepeatMode(int repeatMode) {
            repeatOne = repeatMode == Player.REPEAT_MODE_ONE;
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.setLooping(repeatOne);
                    mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "loop", repeatOne ? 1L : 0L);
                } catch (Throwable ignored) {
                }
            }
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleSetPlaybackParameters(PlaybackParameters playbackParameters) {
            this.playbackParameters = playbackParameters;
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.setSpeed(playbackParameters.speed);
                } catch (Throwable ignored) {
                }
            }
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleSetVolume(float volume) {
            this.volume = Math.max(0f, Math.min(1f, volume));
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.setVolume(this.volume, this.volume);
                } catch (Throwable ignored) {
                }
            }
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleSeek(int mediaItemIndex, long positionMs, int seekCommand) {
            long target = positionMs == C.TIME_UNSET ? 0 : Math.max(0, positionMs);
            this.positionMs = target;
            if (prepared && mediaPlayer != null) {
                try {
                    mediaPlayer.seekTo(target);
                    if (playbackState == Player.STATE_ENDED) {
                        playbackState = Player.STATE_READY;
                        if (playWhenReady) mediaPlayer.start();
                    }
                } catch (Throwable ignored) {
                }
            } else {
                pendingSeekMs = target;
            }
            invalidateState();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleSetVideoOutput(Object videoOutput) {
            detachVideoOutput();
            this.videoOutput = videoOutput;
            attachVideoOutput();
            return Futures.immediateVoidFuture();
        }

        @Override
        protected ListenableFuture<?> handleClearVideoOutput(@Nullable Object videoOutput) {
            if (videoOutput == null || videoOutput == this.videoOutput) {
                this.videoOutput = null;
                detachVideoOutput();
            }
            return Futures.immediateVoidFuture();
        }

        private void attachVideoOutput() {
            Object output = videoOutput;
            if (output == null) return;
            if (output instanceof SurfaceView view) {
                surfaceHolder = view.getHolder();
                surfaceHolder.addCallback(surfaceCallback);
                if (mediaPlayer != null) mediaPlayer.setDisplay(surfaceHolder);
            } else if (output instanceof SurfaceHolder holder) {
                surfaceHolder = holder;
                if (mediaPlayer != null) mediaPlayer.setDisplay(holder);
            } else if (output instanceof TextureView view) {
                if (view.getSurfaceTexture() != null) {
                    ownedSurface = new Surface(view.getSurfaceTexture());
                    if (mediaPlayer != null) mediaPlayer.setSurface(ownedSurface);
                } else if (!textureListenerAttached) {
                    view.setSurfaceTextureListener(textureListener);
                    textureListenerAttached = true;
                }
            } else if (output instanceof Surface surface) {
                if (mediaPlayer != null) mediaPlayer.setSurface(surface);
            }
        }

        private void detachVideoOutput() {
            if (surfaceHolder != null) {
                try {
                    surfaceHolder.removeCallback(surfaceCallback);
                } catch (Throwable ignored) {
                }
                surfaceHolder = null;
            }
            if (videoOutput instanceof TextureView view && textureListenerAttached) {
                view.setSurfaceTextureListener(null);
                textureListenerAttached = false;
            }
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.setDisplay(null);
                } catch (Throwable ignored) {
                }
                try {
                    mediaPlayer.setSurface(null);
                } catch (Throwable ignored) {
                }
            }
            if (ownedSurface != null) {
                ownedSurface.release();
                ownedSurface = null;
            }
        }

        private void releasePlayer() {
            detachVideoOutput();
            prepared = false;
            stopPolling();
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                } catch (Throwable ignored) {
                }
                try {
                    mediaPlayer.release();
                } catch (Throwable ignored) {
                }
                mediaPlayer = null;
            }
        }

        private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mediaPlayer != null) mediaPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mediaPlayer != null) mediaPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mediaPlayer != null) mediaPlayer.setDisplay(null);
            }
        };

        private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                if (mediaPlayer == null) return;
                if (ownedSurface != null) ownedSurface.release();
                ownedSurface = new Surface(texture);
                mediaPlayer.setSurface(ownedSurface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                if (mediaPlayer != null) mediaPlayer.setSurface(null);
                if (ownedSurface != null) {
                    ownedSurface.release();
                    ownedSurface = null;
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            }
        };
    }
}
