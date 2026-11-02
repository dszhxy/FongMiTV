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
import com.fongmi.android.tv.ui.dialog.ThemeDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SettingAppearanceFragment extends BaseFragment implements ThemeDialog.Listener {

    private static final String[] LANGUAGES = {"", "zh-CN", "zh-TW", "en"};
    private static final String[] LANGUAGE_NAMES = {"跟随系统", "简体中文", "繁體中文", "English"};

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
        int checked = indexOf(LANGUAGES, Setting.getLanguage());
        new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.setting_language).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(LANGUAGE_NAMES, checked, (dialog, which) -> {
            Setting.putLanguage(LANGUAGES[which]);
            dialog.dismiss();
            requireActivity().recreate();
        }).show();
    }

    private void setThemeColor(View view) {
        ThemeDialog.show(this);
    }

    @Override
    public void setTheme(int color) {
        Setting.putThemeColor(color);
        mBinding.themeColorText.setText(getThemeColorName(color));
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
}