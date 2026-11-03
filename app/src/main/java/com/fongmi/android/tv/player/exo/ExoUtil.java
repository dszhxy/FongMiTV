package com.fongmi.android.tv.player.exo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.audio.AudioTrackAudioOutputProvider;
import androidx.media3.exoplayer.audio.DefaultAudioSink;
import androidx.media3.exoplayer.trackselection.DecodeTrackSelector;
import androidx.media3.exoplayer.util.EventLogger;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.track.LangUtil;
import com.fongmi.android.tv.setting.DecodeSetting;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.SpeedSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ported from FusionTV-KitKat's player.exo.ExoUtil, adapted to the media3 1.11 APIs used here.
 */
public class ExoUtil {

    public static ExoPlayer buildPlayer(int decode, Player.Listener listener) {
        return buildPlayer(decode, listener, null);
    }

    public static ExoPlayer buildPlayer(int decode, Player.Listener listener, AudioProcessor audioProcessor) {
        ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder(App.get()).setTrackSelector(buildTrackSelector(decode)).setLoadControl(buildLoadControl()).setRenderersFactory(buildRenderersFactory(audioProcessor)).setMediaSourceFactory(new MediaSourceFactory()).setSkipSilenceEnabled(SpeedSetting.isSkipSilence());
        ExoPlayer player = playerBuilder.build();
        if (BuildConfig.DEBUG) player.addAnalyticsListener(new EventLogger());
        player.setAudioAttributes(AudioAttributes.DEFAULT, true);
        player.setHandleAudioBecomingNoisy(true);
        player.setPlayWhenReady(true);
        player.addListener(listener);
        return player;
    }

    public static LoadControl buildLoadControl() {
        int min = Math.max(PlayerSetting.getBuffer(), 1) * 1000;
        int playback = Math.min(1000, min);
        int rebuffer = Math.min(2000, min);
        return new DefaultLoadControl.Builder().setBufferDurationsMs(min, min * 2, playback, rebuffer).build();
    }

    static DecodeTrackSelector buildTrackSelector(int decode) {
        DecodeTrackSelector trackSelector = new DecodeTrackSelector(App.get());
        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguages(LangUtil.getPreferredTextLanguages()).setForceHighestSupportedBitrate(true).setTunnelingEnabled(DecodeSetting.isTunnelingEnabled()).build());
        setDecodePreferences(trackSelector, decode);
        return trackSelector;
    }

    static void setDecodePreferences(DecodeTrackSelector trackSelector, int decode) {
        int audioDecode = isAudioSoftwareDecode(decode) ? PlayerEngine.SOFT : PlayerEngine.HARD;
        int videoDecode = isVideoSoftwareDecode(decode) ? PlayerEngine.SOFT : PlayerEngine.HARD;
        trackSelector.setRendererDecodePreferences(audioDecode, videoDecode);
    }

    private static boolean isAudioSoftwareDecode(int decode) {
        return decode == PlayerEngine.SOFT && DecodeSetting.isAudioPrefer();
    }

    private static boolean isVideoSoftwareDecode(int decode) {
        return decode == PlayerEngine.SOFT && DecodeSetting.isVideoPrefer();
    }

    static RenderersFactory buildRenderersFactory(AudioProcessor audioProcessor) {
        DefaultRenderersFactory factory = new DefaultRenderersFactory(App.get()) {
            @Override
            protected AudioSink buildAudioSink(@NonNull Context context, boolean enableFloatOutput, boolean enableAudioOutputPlaybackParams) {
                return ExoUtil.buildAudioSink(context, enableFloatOutput, enableAudioOutputPlaybackParams, audioProcessor);
            }
        };
        return factory.setEnableDecoderFallback(true).setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
    }

    private static AudioSink buildAudioSink(Context context, boolean enableFloatOutput, boolean enableAudioOutputPlaybackParams, AudioProcessor audioProcessor) {
        DefaultAudioSink.Builder builder = new DefaultAudioSink.Builder(context).setEnableFloatOutput(enableFloatOutput).setEnableAudioOutputPlaybackParameters(enableAudioOutputPlaybackParams);
        if (!DecodeSetting.isAudioPassThrough()) builder.setAudioOutputProvider(new AudioTrackAudioOutputProvider.Builder(null).build());
        if (audioProcessor != null) builder.setAudioProcessors(new AudioProcessor[]{audioProcessor});
        return builder.build();
    }

    public static Map<String, String> extractHeaders(MediaItem item) {
        Bundle extras = item.requestMetadata.extras;
        if (extras == null) return new HashMap<>();
        return extras.keySet().stream().filter(key -> extras.getString(key) != null).collect(Collectors.toMap(key -> key, extras::getString));
    }

    public static String getMimeType(int errorCode) {
        if (errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED || errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED || errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED) return MimeTypes.APPLICATION_M3U8;
        if (errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED || errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED) return MimeTypes.APPLICATION_OCTET_STREAM;
        return null;
    }

    public static int getRetry(int errorCode) {
        if (errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED) return 2;
        if (errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED) return 2;
        if (errorCode >= PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED && errorCode <= PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED) return 2;
        if (errorCode >= PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED && errorCode <= PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED) return 2;
        return 1;
    }
}
