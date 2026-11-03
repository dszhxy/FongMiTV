package com.fongmi.android.tv.player.engine;

import androidx.media3.common.Player;
import androidx.media3.exoplayer.hls.CmgConfiguration;

import com.fongmi.android.tv.player.exo.ExoPlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;

public final class PlayerEngineFactory {

    public static PlayerEngine create(Player.Listener listener) {
        return new ExoPlayerEngine(listener);
    }

    public static PlayerEngine create(PlaySpec spec, Player.Listener listener) {
        return new ExoPlayerEngine(listener);
    }

    public static PlayerEngine create(PlayerEngine.Type type, Player.Listener listener) {
        return new ExoPlayerEngine(listener);
    }

    public static boolean matches(PlayerEngine engine, PlaySpec spec) {
        return engine != null && engine.getType() == PlayerEngine.Type.EXO && !engine.needsRebuild();
    }

    public static boolean canPreload(PlaySpec spec) {
        return !isCmg(spec);
    }

    private static boolean isCmg(PlaySpec spec) {
        return CmgConfiguration.fromMediaUri(spec.getUri()) != null;
    }
}
