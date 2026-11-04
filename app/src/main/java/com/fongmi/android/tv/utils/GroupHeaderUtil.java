package com.fongmi.android.tv.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fongmi.android.tv.setting.Setting;

/**
 * 统一设置分组标题颜色：默认加深，选中主题色后跟随主题色。
 */
public final class GroupHeaderUtil {

    public static final String TAG = "group_header";

    private GroupHeaderUtil() {
    }

    public static void apply(View root) {
        int theme = Setting.getDynamicColor();
        int color = theme != 0 ? theme : 0xFFFFFFFF;
        apply(root, color);
    }

    private static void apply(View view, int color) {
        if (view instanceof TextView text && TAG.equals(text.getTag())) {
            text.setTextColor(color);
        }
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) apply(group.getChildAt(i), color);
        }
    }
}
