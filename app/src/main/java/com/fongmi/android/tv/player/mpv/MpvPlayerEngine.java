package com.fongmi.android.tv.player.mpv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.mpvplayer.MpvPlayer;

import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.player.effect.PlayerEffect;
import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.media.MediaItemFactory;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.setting.SubtitleSetting;

import java.util.ArrayList;
import java.util.List;

public class MpvPlayerEngine implements PlayerEngine, Player.Listener {

    private final MpvErrorMessageProvider provider;
    private final MpvPlayerEffect effect;
    private final Player.Listener externalListener;
    private final MpvPlayer player;
    private PlaySpec spec;
    private int generation;
    private boolean released;
    @Nullable
    private TrackSelectionOverride secondarySelection;

    public MpvPlayerEngine(int decode, Player.Listener listener) {
        this.externalListener = listener;
        this.provider = new MpvErrorMessageProvider();
        this.player = MpvUtil.buildPlayer(decode, listener);
        this.effect = new MpvPlayerEffect(player);
        this.player.setAudioOutputListener(effect::applyAudioEffect);
        this.player.addListener(this);
    }

    public static boolean isAvailable() {
        return MpvUtil.isAvailable();
    }

    @Override
    public Type getType() {
        return Type.MPV;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getAudioChannelCount() {
        return player.getAudioChannelCount();
    }

    @Override
    public PlayerEffect getEffect() {
        return effect;
    }

    @Override
    public void release() {
        released = true;
        generation++;
        try {
            player.removeListener(this);
            if (externalListener != null) player.removeListener(externalListener);
            player.setAudioOutputListener(null);
        } catch (Throwable ignored) {
        }
        player.release();
    }

    @Override
    public void setSubtitleStyle() {
        applySubtitleStyle();
    }

    @Override
    public void applySubtitleStyle() {
        Integer id = null;
        if (secondarySelection != null) {
            try {
                id = parseMpvTrackId(secondarySelection.mediaTrackGroup.getFormat(secondarySelection.trackIndices.get(0)).id);
            } catch (RuntimeException invalid) {
                secondarySelection = null;
            }
        }
        MpvUtil.setSubtitleStyle(player, id);
    }

    @Override
    public SecondarySubtitleState getSecondarySubtitleState() {
        TrackSelectionOverride primary = null;
        List<TrackSelectionOverride> candidates = new ArrayList<>();
        for (Tracks.Group group : player.getCurrentTracks().getGroups()) {
            if (group.getType() != C.TRACK_TYPE_TEXT) continue;
            for (int i = 0; i < group.length; i++) {
                if (!group.isTrackSupported(i)) continue;
                var selection = new TrackSelectionOverride(group.getMediaTrackGroup(), i);
                if (group.isTrackSelected(i)) primary = selection;
                else candidates.add(selection);
            }
        }
        var selected = candidates.contains(secondarySelection) ? secondarySelection : null;
        return new SecondarySubtitleState(primary, selected, candidates, false);
    }

    @Override
    public void setSecondarySubtitleSelection(@Nullable TrackSelectionOverride selection) {
        if (selection != null && !getSecondarySubtitleState().secondaryCandidates().contains(selection)) return;
        secondarySelection = selection;
        applySubtitleStyle();
    }

    @Override
    public boolean addSubtitle(Sub sub) {
        if (sub == null || sub.isEmpty() || player.getCurrentMediaItem() == null) return false;
        if (player.getPlaybackState() == Player.STATE_IDLE || player.getPlaybackState() == Player.STATE_ENDED) return false;
        player.addSubtitle(MediaItemFactory.buildSubConfig(sub));
        return true;
    }

    @Override
    public void setDecode(int decode) {
        player.setDecode(decode);
    }

    @Override
    public void onTracksChanged(@NonNull Tracks tracks) {
        effect.applyVideoEffect();
        if (secondarySelection != null && getSecondarySubtitleState().explicitSelection() == null) {
            secondarySelection = null;
            applySubtitleStyle();
        }
    }

    @Nullable
    private static Integer parseMpvTrackId(@Nullable String id) {
        if (id == null) return null;
        int colon = id.indexOf(':');
        String value = colon >= 0 ? id.substring(colon + 1) : id;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void start(PlaySpec spec, long startPositionMs) {
        this.spec = spec;
        this.secondarySelection = null;
        applySubtitleStyle();
        int request = ++generation;
        if (released || request != generation) return;
        startInternal(startPositionMs);
    }

    private void startInternal(long startPositionMs) {
        effect.applyVideoEffect();
        effect.clearAudioEffect();
        player.setMediaItem(MediaItemFactory.from(spec), Math.max(0L, startPositionMs));
        player.prepare();
        player.play();
    }

    @Override
    public void stop() {
        generation++;
        player.stop();
    }

    @Override
    public String getErrorMessage(PlaybackException e) {
        return provider.get(e);
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        return switch (e.errorCode) {
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                    PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FAILED -> ErrorAction.DECODE;
            case PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> retryHls();
            default -> ErrorAction.FATAL;
        };
    }

    private ErrorAction retryHls() {
        if (spec == null || MimeTypes.APPLICATION_M3U8.equals(spec.getFormat())) return ErrorAction.FATAL;
        spec.setFormat(MimeTypes.APPLICATION_M3U8);
        startInternal(player.getCurrentPosition());
        return ErrorAction.RECOVERED;
    }
}
