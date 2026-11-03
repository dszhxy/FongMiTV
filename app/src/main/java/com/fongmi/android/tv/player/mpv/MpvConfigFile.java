package com.fongmi.android.tv.player.mpv;

import android.content.Context;
import android.net.Uri;

import com.fongmi.android.tv.utils.FileUtil;
import com.github.catvod.utils.Path;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class MpvConfigFile {

    private static final String MPV_CONF = "mpv.conf";

    public static File file() {
        return Path.mpv(MPV_CONF);
    }

    public static String read() {
        return Path.read(file());
    }

    public static void write(String content) {
        String value = content == null ? "" : content;
        try {
            FileUtil.writeAtomically(value.getBytes(StandardCharsets.UTF_8), file());
        } catch (IOException | SecurityException e) {
            Path.write(file(), value.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void importFrom(Context context, Uri uri) {
        try {
            FileUtil.copyAtomically(uri, file());
        } catch (Exception ignored) {
        }
    }
}
