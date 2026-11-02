package com.fongmi.android.tv.player.system;

import androidx.media3.common.Format;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;

import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.media.MediaItemFactory;
import com.fongmi.android.tv.player.media.PlaySpec;

@UnstableApi
public class SystemPlayerEngine implements PlayerEngine {

    private final SystemSimplePlayer player;
    private int decode;

    public SystemPlayerEngine(int decode, Player.Listener listener) {
        this.decode = decode;
        this.player = new SystemSimplePlayer();
        if (listener != null) player.addListener(listener);
    }

    @Override
    public Type getType() {
        return Type.SYSTEM;
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
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> "系统播放器网络错误";
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "系统播放器 HTTP 错误";
            case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "系统播放器文件不存在";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> "系统播放器文件损坏";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "系统播放器格式不支持";
            case PlaybackException.ERROR_CODE_TIMEOUT -> "系统播放器超时";
            default -> "系统播放器错误";
        };
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        return ErrorAction.FATAL;
    }
}
