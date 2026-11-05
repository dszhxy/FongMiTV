package com.fongmi.android.tv.ui.dialog;

import androidx.fragment.app.Fragment;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.setting.Setting;

/**
 * 主题色选择：与字幕文字色等颜色弹窗共用同一套圆形色块（每行 5 个、白色圆环选中）。
 */
public class ThemeDialog {

    private static final int[] COLORS = {-1, 0, 0xFF000000, 0xFF1B1B1B, 0xFF263238, 0xFF455A64, 0xFF6D4C41, 0xFF6750A4, 0xFF3949AB, 0xFF1E88E5, 0xFF039BE5, 0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFC0CA33, 0xFFFB8C00, 0xFFF4511E, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA};

    public static void show(Fragment fragment) {
        int current = Setting.getThemeColor();
        ColorPickerDialog.showTheme(fragment.requireActivity(), R.string.setting_theme_color, COLORS, current, color -> {
            if (fragment instanceof Listener listener) listener.setTheme(color);
        });
    }

    public interface Listener {

        void setTheme(int color);
    }
}
