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
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.dialog.ThemeDialog;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SettingAppearanceFragment extends BaseFragment implements ThemeDialog.Listener {

    private static final String[] LANGUAGES = {"", "zh-CN", "zh-TW", "en"};
    private static final String[] LANGUAGE_NAMES = {"跟随系统", "简体中文", "繁體中文", "English"};
    private static final String[] RATIO_NAMES = {"跟随站点", "竖屏 3:4", "横屏 16:9", "方形 1:1"};
    private static final String[] RADIUS_NAMES = {"跟随站点", "直角", "4dp", "8dp", "12dp", "圆形"};

    private FragmentSettingAppearanceBinding mBinding;
    private String[] sizes;

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
        sizes = ResUtil.getStringArray(R.array.select_size);
        mBinding.coverText.setText(sizes[PlayerSetting.getSize()]);
        mBinding.coverRatioText.setText(RATIO_NAMES[PlayerSetting.getCoverRatio()]);
        mBinding.coverRadiusText.setText(RADIUS_NAMES[PlayerSetting.getCoverRadius()]);
    }

    @Override
    protected void initEvent() {
        mBinding.language.setOnClickListener(this::setLanguage);
        mBinding.themeColor.setOnClickListener(this::setThemeColor);
        mBinding.cover.setOnClickListener(this::setCover);
        mBinding.coverRatio.setOnClickListener(this::setCoverRatio);
        mBinding.coverRadius.setOnClickListener(this::setCoverRadius);
    }

    private void setCover(View view) {
        new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.setting_cover).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(sizes, PlayerSetting.getSize(), (dialog, which) -> {
            mBinding.coverText.setText(sizes[which]);
            PlayerSetting.putSize(which);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void setCoverRatio(View view) {
        new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.setting_cover_ratio).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(RATIO_NAMES, PlayerSetting.getCoverRatio(), (dialog, which) -> {
            PlayerSetting.putCoverRatio(which);
            mBinding.coverRatioText.setText(RATIO_NAMES[which]);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void setCoverRadius(View view) {
        new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.setting_cover_radius).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(RADIUS_NAMES, PlayerSetting.getCoverRadius(), (dialog, which) -> {
            PlayerSetting.putCoverRadius(which);
            mBinding.coverRadiusText.setText(RADIUS_NAMES[which]);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
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