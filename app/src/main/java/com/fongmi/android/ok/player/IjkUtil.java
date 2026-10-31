package com.fongmi.android.ok.player;

import android.net.Uri;

import com.fongmi.android.ok.bean.Channel;
import com.fongmi.android.ok.bean.Result;
import com.fongmi.android.ok.server.Server;
import com.fongmi.android.ok.utils.Sniffer;
import com.fongmi.android.ok.utils.UrlUtil;

import java.net.URLEncoder;
import java.util.Map;

import tv.danmaku.ijk.media.player.MediaSource;

public class IjkUtil {

    public static MediaSource getSource(Result result) {
        return getSource(result.getHeaders(), result.getRealUrl());
    }

    public static MediaSource getSource(Channel channel) {
        return getSource(channel.getHeaders(), channel.getUrl());
    }

    public static MediaSource getSource(Map<String, String> headers, String url) {
        Uri uri = UrlUtil.uri(url);
        boolean m3u8Ad = Sniffer.getRegex(uri).size() > 0;
        if (m3u8Ad) uri = Uri.parse(Server.get().getAddress().concat("/m3u8?url=").concat(URLEncoder.encode(uri.toString())));
        return new MediaSource(Players.checkUa(headers), uri);
    }
}
