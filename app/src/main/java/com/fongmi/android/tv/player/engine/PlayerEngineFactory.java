package com.fongmi.android.tv.player.engine;

import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.EXO;
import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.IJK;
import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.MPV;
import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.SYSTEM;

import androidx.media3.common.Player;

import com.fongmi.android.tv.player.exo.ExoPlayerEngine;
import com.fongmi.android.tv.player.ijk.IjkPlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.player.mpv.MpvPlayerEngine;
import com.fongmi.android.tv.player.system.SystemPlayerEngine;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.utils.UrlUtil;

public final class PlayerEngineFactory {

    public static PlayerEngine create(int decode, Player.Listener listener) {
        return create(decode, resolve(), listener);
    }

    public static PlayerEngine create(int decode, PlaySpec spec, Player.Listener listener) {
        return create(decode, resolve(spec), listener);
    }

    public static PlayerEngine create(int decode, PlayerEngine.Type type, Player.Listener listener) {
        if (type == MPV && isMpvReady()) return new MpvPlayerEngine(decode, listener);
        if (type == IJK && isIjkReady()) return new IjkPlayerEngine(decode, listener);
        if (type == SYSTEM) return new SystemPlayerEngine(decode, listener);
        return new ExoPlayerEngine(decode, listener);
    }

    public static boolean matches(PlayerEngine engine, PlaySpec spec) {
        return engine != null && engine.getType() == resolve(spec);
    }

    private static PlayerEngine.Type resolve(PlaySpec spec) {
        if (requiresExo(spec)) return EXO;
        return resolve();
    }

    private static PlayerEngine.Type resolve() {
        if (PlayerSetting.isMpv()) return isMpvReady() ? MPV : EXO;
        if (PlayerSetting.isIjk()) return isIjkReady() ? IJK : EXO;
        if (PlayerSetting.isSystem()) return SYSTEM;
        return EXO;
    }

    private static boolean requiresExo(PlaySpec spec) {
        return spec.getDrm() != null || "smb".equals(UrlUtil.scheme(spec.getUrl()));
    }

    private static boolean isMpvReady() {
        return MpvPlayerEngine.isAvailable();
    }

    private static boolean isIjkReady() {
        return IjkPlayerEngine.isAvailable();
    }
}
