package com.fongmi.android.tv.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.FragmentSettingAppearanceBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseFragment;

import java.util.Locale;

public class SettingAppearanceFragment extends BaseFragment {

    private static final String[] LANGUAGES = {"", "zh-CN", "zh-TW", "en"};
    private static final String[] LANGUAGE_NAMES = {"跟随系统", "简体中文", "繁體中文", "English"};
    private static final int[] THEME_COLORS = {-1, 0, 0xFF6750A4, 0xFF3949AB, 0xFF1E88E5, 0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFFB8C00, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF6D4C41};

    private FragmentSettingAppearanceBinding mBinding;

    public static SettingAppearanceFragment newInstance() {
        return new SettingAppearanceFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingAppearanceBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.languageText.setText(getLanguageName());
        mBinding.themeColorText.setText(getThemeColorName(Setting.getThemeColor()));
    }

    @Override
    protected void initEvent() {
        mBinding.language.setOnClickListener(this::setLanguage);
        mBinding.themeColor.setOnClickListener(this::setThemeColor);
    }

    private void setLanguage(View view) {
        int index = indexOf(LANGUAGES, Setting.getLanguage());
        Setting.putLanguage(LANGUAGES[(index + 1) % LANGUAGES.length]);
        requireActivity().recreate();
    }

    private void setThemeColor(View view) {
        int index = findIndex(THEME_COLORS, Setting.getThemeColor());
        int next = THEME_COLORS[(index + 1) % THEME_COLORS.length];
        Setting.putThemeColor(next);
        mBinding.themeColorText.setText(getThemeColorName(next));
        RefreshEvent.theme();
    }

    private String getLanguageName() {
        return LANGUAGE_NAMES[indexOf(LANGUAGES, Setting.getLanguage())];
    }

    private String getThemeColorName(int color) {
        if (color == -1) return getString(R.string.setting_off);
        if (color == 0) return getString(R.string.setting_auto);
        return String.format(Locale.US, "#%06X", 0xFFFFFF & color);
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) if (array[i].equals(value)) return i;
        return 0;
    }

    private int findIndex(int[] array, int value) {
        for (int i = 0; i < array.length; i++) if (array[i] == value) return i;
        return 0;
    }
}