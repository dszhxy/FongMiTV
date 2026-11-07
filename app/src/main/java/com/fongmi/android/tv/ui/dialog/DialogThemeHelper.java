package com.fongmi.android.tv.ui.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.setting.Setting;

/** 统一弹窗主题入口：所有自绘裸 Dialog / DialogFragment 都从这里取主题与语义色。 */
public final class DialogThemeHelper {

    private DialogThemeHelper() {}

    /** 普通弹窗主题（带 5 种风格的语义色与按钮样式） */
    @StyleRes public static int getTheme() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.style.Theme_App_DialogClassic;
            case 1: return R.style.Theme_App_DialogWhite;
            case 2: return R.style.Theme_App_DialogAmoled;
            case 4: return R.style.Theme_App_DialogNeon;
            case 3:
            default: return R.style.Theme_App_DialogIOS;
        }
    }

    /** 纯语义色 overlay：给 BottomSheet / SideSheet 用 ContextThemeWrapper 叠加 */
    @StyleRes public static int getOverlay() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.style.ThemeOverlay_XingChen_GlassDialog_Dark;
            case 1: return R.style.ThemeOverlay_FongMi_Dialog_White;
            case 2: return R.style.ThemeOverlay_FongMi_Dialog_Amoled;
            case 4: return R.style.ThemeOverlay_FongMi_Dialog_Neon;
            case 3:
            default: return R.style.ThemeOverlay_FongMi_Dialog_IOS;
        }
    }

    /** 透明窗口变体：给 loading / 原 Theme_Translucent_NoTitleBar 的自绘弹窗 */
    @StyleRes public static int getTranslucentTheme() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.style.Theme_App_DialogClassic_Translucent;
            case 1: return R.style.Theme_App_DialogWhite_Translucent;
            case 2: return R.style.Theme_App_DialogAmoled_Translucent;
            case 4: return R.style.Theme_App_DialogNeon_Translucent;
            case 3:
            default: return R.style.Theme_App_DialogIOS_Translucent;
        }
    }

    /** 取当前弹窗主题下的语义色（Java 代码里替代 Color.parseColor） */
    @ColorInt public static int resolveColor(Context ctx, @AttrRes int attr) {
        TypedValue tv = new TypedValue();
        if (ctx.getTheme().resolveAttribute(attr, tv, true)) return tv.data;
        return 0xFF888888;
    }

    public static int resolveResId(Context ctx, @AttrRes int attr) {
        TypedValue tv = new TypedValue();
        if (ctx.getTheme().resolveAttribute(attr, tv, true)) return tv.resourceId;
        return 0;
    }

    @ColorInt public static int primaryText(Context c)   { return resolveColor(c, R.attr.dialogTextPrimary); }
    @ColorInt public static int secondaryText(Context c) { return resolveColor(c, R.attr.dialogTextSecondary); }
    @ColorInt public static int hintText(Context c)      { return resolveColor(c, R.attr.dialogTextHint); }
    @ColorInt public static int accent(Context c)        { return resolveColor(c, R.attr.dialogAccent); }
    @ColorInt public static int divider(Context c)       { return resolveColor(c, R.attr.dialogDivider); }
    public static int surfaceRes(Context c)              { return resolveResId(c, R.attr.dialogSurface); }
    public static int inputSurfaceRes(Context c)         { return resolveResId(c, R.attr.dialogInputSurface); }
    public static int cardSurfaceRes(Context c)          { return resolveResId(c, R.attr.dialogCardSurface); }
}
