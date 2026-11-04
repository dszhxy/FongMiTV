package com.fongmi.android.tv.ui.lab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;

import com.fongmi.android.tv.ui.custom.CustomWallView;

/**
 * 实验室 UI 设计语言：与 Trae 主 App 一致——深色玻璃卡面＋浅色字。
 * 页面背景与其它页面同源：铺自定义壁纸（CustomWallView），内容根透明；
 * 壁纸没贴上前的兜底底色见 Theme.Lab 的 windowBackground。
 * 独立成套，不依赖其它模块的皮肤。布局/条目/顺序一律不动，只出皮。
 */
public final class LabSkin {

    /** 点缀黄（只做按钮底） */
    public static final int YELLOW = 0xFFF2B33D;
    /** 主字（标题/名称）—— 深底浅字 */
    public static final int TEXT_MAIN = 0xFFFFFFFF;
    /** 正文 */
    public static final int TEXT_BODY = 0xE6FFFFFF;
    /** 次字 */
    public static final int TEXT_SUB = 0xB3FFFFFF;
    /** 暗字：次字 50% 透明 */
    public static final int TEXT_DIM = 0x80FFFFFF;
    /** 绿字（运行中/已装） */
    public static final int GREEN = 0xFF35D884;
    /** 绿点 #22B45E */
    public static final int GREEN_DOT = 0xFF22B45E;
    /** 出错红 #E5484D —— 星辰主 App 弹窗/布局同款红，深卡上可读 */
    public static final int RED = 0xFFE5484D;
    /** 琥珀字（状态/进度文案）—— 深底上看得清 */
    public static final int AMBER = 0xFFF0C040;
    /** 黄钮上的字 #171310 */
    public static final int ON_YELLOW = 0xFF171310;
    /** 深底上的主字 #f7f4ee（稿 a2-dialogs .pt/.opt） */
    public static final int TEXT_ON_DARK = 0xFFF7F4EE;
    /** 深底上的次字 #cfc9bd（稿 .status） */
    public static final int TEXT_ON_DARK_DIM = 0xFFCFC9BD;
    /** 深底输入框占位字 #a8a29a（稿 02 占位色） */
    public static final int HINT_ON_DARK = 0xFFA8A29A;
    /** 深底上的报错红 #ffa49a（稿 .err，报错字跟底拉开对比） */
    public static final int RED_ON_DARK = 0xFFFFA49A;
    /** 壁纸上的标题字 */
    public static final int WALL_TITLE = 0xFFFFFFFF;
    /** 壁纸上的副行字 */
    public static final int WALL_SUB = 0xCCFFFFFF;
    /** 壁纸上的弱字 */
    public static final int WALL_DIM = 0x99FFFFFF;
    /** 金色环境签 */
    public static final int GOLD_TAG = 0xFFFFD27A;
    /** 运行中绿字 #35d884（稿 .chip.run） */
    public static final int GREEN_ON_DARK = 0xFF35D884;
    /** 运行中计数黄 #f0c040（稿 .pill .n） */
    public static final int COUNT_YELLOW = 0xFFF0C040;

    public interface BoolConsumer {
        void accept(boolean on);
    }

    /** 壁纸上的字：加深色投影，壁纸偏亮时也看得清 */
    public static void halo(android.widget.TextView t) {
        t.setShadowLayer(12, 0, 2, 0x99000000);
    }

    /** 深玻璃格（稿 .tile）：深渐变＋1dp 浅描边＋16dp 圆角 */
    public static GradientDrawable glassDark(Context c) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xCC26201A, 0xC7181410});
        d.setCornerRadius(dp(c, 16));
        d.setStroke(dp(c, 1), 0x24FFFFFF);
        return d;
    }

    /** 深底弹窗面板（稿 .panel）：rgba(26,23,20,.97)、圆角 26dp */
    public static GradientDrawable dialogPanel(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0xF71A1714);
        d.setCornerRadius(dp(c, 26));
        d.setStroke(dp(c, 1), 0x24FFFFFF);
        return d;
    }

    /** 弹窗选项卡（稿 .opt）：独立一张 */
    public static GradientDrawable dialogOption(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x14FFFFFF);
        d.setCornerRadius(dp(c, 16));
        d.setStroke(dp(c, 1), 0x12FFFFFF);
        return d;
    }

    /** 弹窗中性钮（稿 .btn-n） */
    public static GradientDrawable dialogNeutral(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1AFFFFFF);
        d.setCornerRadius(dp(c, 18));
        return d;
    }

    /** 进度轨道（稿 .track） */
    public static GradientDrawable progressTrack(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x24FFFFFF);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 进度黄条（稿 .fill） */
    public static GradientDrawable progressFill(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(YELLOW);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 状态徽：运行中（稿 .chip.run） */
    public static GradientDrawable chipRun(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x292FD47E);
        d.setCornerRadius(dp(c, 999));
        d.setStroke(dp(c, 1), 0x5935D884);
        return d;
    }

    /** 状态徽：已停止（稿 .chip.stop） */
    public static GradientDrawable chipStop(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x14FFFFFF);
        d.setCornerRadius(dp(c, 999));
        d.setStroke(dp(c, 1), 0x24FFFFFF);
        return d;
    }

    /** 中性小胶囊（稿 .uninstall：卸载钮） */
    public static GradientDrawable capsuleNeutral(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1FFFFFFF);
        d.setCornerRadius(dp(c, 999));
        d.setStroke(dp(c, 1), 0x33FFFFFF);
        return d;
    }

    /** 黄色小胶囊（稿 env .install：环境名后面的「安装」钮） */
    public static GradientDrawable capsuleYellow(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(YELLOW);
        d.setCornerRadius(dp(c, 13));
        return d;
    }

    /** 深底大钮（稿 service-detail .bigbtn：「停止/启动」全宽钮，灰底白字） */
    public static GradientDrawable btnBigDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1FFFFFFF);
        d.setCornerRadius(dp(c, 15));
        return d;
    }

    /** 运行记录行（稿 service-detail .logrow：深底＋浅描边） */
    public static GradientDrawable logRowDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x80080C14);
        d.setCornerRadius(dp(c, 14));
        d.setStroke(dp(c, 1), 0x1AFFFFFF);
        return d;
    }

    /** 深底行卡（服务详情页下半区功能行：重启/分享/高级等，稿同语言） */
    public static GradientDrawable rowDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x14FFFFFF);
        d.setCornerRadius(dp(c, 12));
        d.setStroke(dp(c, 1), 0x12FFFFFF);
        return d;
    }

    /** 深色胶囊钮（稿 .pill：全部停止/运行中） */
    public static GradientDrawable pillDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0xC71E1814);
        d.setCornerRadius(dp(c, 18));
        d.setStroke(dp(c, 1), 0x29FFFFFF);
        return d;
    }

    /** 地址盒（稿 .addr） */
    public static GradientDrawable addrBox(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x12FFFFFF);
        d.setCornerRadius(dp(c, 12));
        d.setStroke(dp(c, 1), 0x1AFFFFFF);
        return d;
    }

    /** 深底输入框（稿 02 .box） */
    public static GradientDrawable darkInput(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x61000000);
        d.setCornerRadius(dp(c, 14));
        d.setStroke(dp(c, 2), 0x38FFFFFF);
        return d;
    }

    /**
     * 迷你自启开关（稿 .mini）：36×21dp，开态黄底白钮，关态半透明白底。
     * 纯 View 自绘，不用系统 Switch（系统 Switch 在各 ROM 上皮不一）。
     */
    public static View miniSwitch(Context c, boolean on, BoolConsumer cb) {
        final boolean[] state = {on};
        final android.widget.FrameLayout sw = new android.widget.FrameLayout(c);
        int w = dp(c, 36), h = dp(c, 21);
        sw.setLayoutParams(new ViewGroup.LayoutParams(w, h));
        final GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(on ? YELLOW : 0x2EFFFFFF);
        sw.setBackground(bg);
        final View knob = new View(c);
        int ks = dp(c, 16);
        final android.widget.FrameLayout.LayoutParams klp =
                new android.widget.FrameLayout.LayoutParams(ks, ks);
        klp.gravity = android.view.Gravity.CENTER_VERTICAL;
        klp.leftMargin = dp(c, on ? 18 : 3);
        klp.rightMargin = dp(c, on ? 3 : 18);
        knob.setLayoutParams(klp);
        GradientDrawable kd = new GradientDrawable();
        kd.setShape(GradientDrawable.OVAL);
        kd.setColor(0xFFFFFFFF);
        knob.setBackground(kd);
        sw.addView(knob);
        sw.setClickable(true);
        sw.setFocusable(true);
        sw.setOnClickListener(v -> {
            state[0] = !state[0];
            bg.setColor(state[0] ? YELLOW : 0x2EFFFFFF);
            android.widget.FrameLayout.LayoutParams p =
                    (android.widget.FrameLayout.LayoutParams) knob.getLayoutParams();
            p.leftMargin = dp(c, state[0] ? 18 : 3);
            p.rightMargin = dp(c, state[0] ? 3 : 18);
            knob.setLayoutParams(p);
            if (cb != null) cb.accept(state[0]);
        });
        return sw;
    }

    private LabSkin() {
    }

    public static int dp(Context c, int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    /** 状态栏融合：透明栏＋浅色图标，内容延伸到栏下（与 BaseActivity 同一写法） */
    public static void edgeToEdge(AppCompatActivity a) {
        EdgeToEdge.enable(a,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            a.getWindow().setStatusBarContrastEnforced(false);
            a.getWindow().setNavigationBarContrastEnforced(false);
        }
    }

    /**
     * 与 BaseActivity 一致：内容之下铺全局壁纸，页面根透明，背景跟主 App 其它页同源。
     */
    public static void setContentView(AppCompatActivity a, View content) {
        a.setContentView(content);
        ViewGroup root = a.findViewById(android.R.id.content);
        if (root == null) return;
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof CustomWallView) return;
        }
        root.addView(new CustomWallView(a, null), 0,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public static int statusBarHeight(Context c) {
        int id = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? c.getResources().getDimensionPixelSize(id) : dp(c, 24);
    }

    public static int navBarHeight(Context c) {
        int id = c.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return id > 0 ? c.getResources().getDimensionPixelSize(id) : 0;
    }

    /** 卡面：深色玻璃卡（与主 App 条目卡同风格：深渐变、1dp 浅描边、10dp 圆角） */
    public static GradientDrawable cardBackground(Context c, boolean running) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xCC26201A, 0xC7181410});
        d.setCornerRadius(dp(c, 10));
        d.setStroke(dp(c, 1), running ? 0x804ADE80 : 0x33FFFFFF);
        return d;
    }

    /** 状态徽：灰（未装/已停止） */
    public static GradientDrawable tagGray(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x26FFFFFF);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 状态徽：绿（运行中） */
    public static GradientDrawable tagGreen(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x334ADE80); // rgba(74,222,128,.20)
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 黄色主钮（只做点缀） */
    public static GradientDrawable buttonYellow(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(YELLOW);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 灰色次钮 */
    public static GradientDrawable buttonGray(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1FFFFFFF);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 小圆点（运行中） */
    public static GradientDrawable dot(Context c, int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        int s = dp(c, 8);
        d.setSize(s, s);
        return d;
    }
}
