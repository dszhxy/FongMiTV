package com.fongmi.android.tv.ui.fragment;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.FragmentSettingSubtitleBinding;
import com.fongmi.android.tv.setting.SubtitleSetting;
import com.fongmi.android.tv.ui.base.BaseFragment;

import java.util.Locale;

public class SettingSubtitleFragment extends BaseFragment {

    private static final int[] TEXT_COLORS = {
            SubtitleSetting.SUBTITLE_COLOR_WHITE,
            SubtitleSetting.SUBTITLE_COLOR_YELLOW,
            SubtitleSetting.SUBTITLE_COLOR_CYAN,
            SubtitleSetting.SUBTITLE_COLOR_GREEN,
            SubtitleSetting.SUBTITLE_COLOR_ORANGE,
            SubtitleSetting.SUBTITLE_COLOR_PINK,
            SubtitleSetting.SUBTITLE_COLOR_RED,
            SubtitleSetting.SUBTITLE_COLOR_BLUE,
            SubtitleSetting.SUBTITLE_COLOR_BLACK,
            SubtitleSetting.SUBTITLE_COLOR_GRAY
    };

    private static final int[] BACKGROUND_COLORS = {
            Color.TRANSPARENT,
            SubtitleSetting.SUBTITLE_BACKGROUND_DIM,
            SubtitleSetting.SUBTITLE_BACKGROUND_BLACK,
            SubtitleSetting.SUBTITLE_BACKGROUND_GRAY
    };

    private static final float[] SCALES = {0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
    private static final float[] POSITIONS = {0f, 5f, 10f, 15f, 20f, 30f};

    private FragmentSettingSubtitleBinding mBinding;

    public static SettingSubtitleFragment newInstance() {
        return new SettingSubtitleFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingSubtitleBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        updateView();
    }

    @Override
    protected void initEvent() {
        mBinding.subtitleStyle.setOnClickListener(this::setStyle);
        mBinding.subtitleSize.setOnClickListener(this::setSize);
        mBinding.subtitlePosition.setOnClickListener(this::setPosition);
        mBinding.subtitleTextColor.setOnClickListener(this::setTextColor);
        mBinding.subtitleEdgeColor.setOnClickListener(this::setEdgeColor);
        mBinding.subtitleBackground.setOnClickListener(this::setBackground);
        mBinding.subtitleSecondary.setOnClickListener(this::setSecondary);
        mBinding.subtitleReset.setOnClickListener(this::onReset);
    }

    private void updateView() {
        mBinding.subtitleStyleText.setText(getStyleName(SubtitleSetting.getStyleSource()));
        mBinding.subtitleSizeText.setText(formatScale(SubtitleSetting.getScale()));
        mBinding.subtitlePositionText.setText(formatPosition(SubtitleSetting.getPosition()));
        mBinding.subtitleTextColorText.setText(getTextColorName(SubtitleSetting.getTextBaseColor()));
        mBinding.subtitleEdgeColorText.setText(getTextColorName(SubtitleSetting.getEdgeBaseColor()));
        mBinding.subtitleBackgroundText.setText(getBackgroundColorName(SubtitleSetting.getBackgroundBaseColor()));
        mBinding.subtitleSecondaryText.setText(SubtitleSetting.getSecondaryTrackId() == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? getString(R.string.subtitle_secondary_auto) : getString(R.string.subtitle_secondary_off));
    }

    private void setStyle(View view) {
        SubtitleSetting.putStyleSource((SubtitleSetting.getStyleSource() + 1) % 3);
        mBinding.subtitleStyleText.setText(getStyleName(SubtitleSetting.getStyleSource()));
    }

    private void setSize(View view) {
        int index = (findNearest(SCALES, SubtitleSetting.getScale()) + 1) % SCALES.length;
        SubtitleSetting.putScale(SCALES[index]);
        mBinding.subtitleSizeText.setText(formatScale(SCALES[index]));
    }

    private void setPosition(View view) {
        int index = (findNearest(POSITIONS, SubtitleSetting.getPosition()) + 1) % POSITIONS.length;
        SubtitleSetting.putPosition(POSITIONS[index]);
        mBinding.subtitlePositionText.setText(formatPosition(POSITIONS[index]));
    }

    private void setTextColor(View view) {
        int index = (findIndex(TEXT_COLORS, SubtitleSetting.getTextBaseColor()) + 1) % TEXT_COLORS.length;
        SubtitleSetting.putTextColor(TEXT_COLORS[index]);
        mBinding.subtitleTextColorText.setText(getTextColorName(TEXT_COLORS[index]));
    }

    private void setEdgeColor(View view) {
        int index = (findIndex(TEXT_COLORS, SubtitleSetting.getEdgeBaseColor()) + 1) % TEXT_COLORS.length;
        SubtitleSetting.putEdgeColor(TEXT_COLORS[index]);
        mBinding.subtitleEdgeColorText.setText(getTextColorName(TEXT_COLORS[index]));
    }

    private void setBackground(View view) {
        int index = (findIndex(BACKGROUND_COLORS, SubtitleSetting.getBackgroundBaseColor()) + 1) % BACKGROUND_COLORS.length;
        SubtitleSetting.putBackgroundColor(BACKGROUND_COLORS[index]);
        mBinding.subtitleBackgroundText.setText(getBackgroundColorName(BACKGROUND_COLORS[index]));
    }

    private void setSecondary(View view) {
        int current = SubtitleSetting.getSecondaryTrackId();
        int next = current == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? SubtitleSetting.SECONDARY_SUBTITLE_OFF : SubtitleSetting.SECONDARY_SUBTITLE_AUTO;
        SubtitleSetting.putSecondaryTrackId(next);
        mBinding.subtitleSecondaryText.setText(next == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? getString(R.string.subtitle_secondary_auto) : getString(R.string.subtitle_secondary_off));
    }

    private void onReset(View view) {
        SubtitleSetting.reset();
        updateView();
    }

    private int findIndex(int[] array, int value) {
        for (int i = 0; i < array.length; i++) if (array[i] == value) return i;
        return 0;
    }

    private int findNearest(float[] array, float value) {
        int index = 0;
        float diff = Math.abs(array[0] - value);
        for (int i = 1; i < array.length; i++) {
            float d = Math.abs(array[i] - value);
            if (d < diff) {
                diff = d;
                index = i;
            }
        }
        return index;
    }

    private int getStyleName(int source) {
        if (source == SubtitleSetting.STYLE_SOURCE_SYSTEM) return R.string.subtitle_style_system;
        if (source == SubtitleSetting.STYLE_SOURCE_CUSTOM) return R.string.subtitle_style_custom;
        return R.string.subtitle_style_original;
    }

    private int getTextColorName(int color) {
        if (color == SubtitleSetting.SUBTITLE_COLOR_WHITE) return R.string.subtitle_color_white;
        if (color == SubtitleSetting.SUBTITLE_COLOR_YELLOW) return R.string.subtitle_color_yellow;
        if (color == SubtitleSetting.SUBTITLE_COLOR_CYAN) return R.string.subtitle_color_cyan;
        if (color == SubtitleSetting.SUBTITLE_COLOR_GREEN) return R.string.subtitle_color_green;
        if (color == SubtitleSetting.SUBTITLE_COLOR_ORANGE) return R.string.subtitle_color_orange;
        if (color == SubtitleSetting.SUBTITLE_COLOR_PINK) return R.string.subtitle_color_pink;
        if (color == SubtitleSetting.SUBTITLE_COLOR_RED) return R.string.subtitle_color_red;
        if (color == SubtitleSetting.SUBTITLE_COLOR_BLUE) return R.string.subtitle_color_blue;
        if (color == SubtitleSetting.SUBTITLE_COLOR_BLACK) return R.string.subtitle_color_black;
        if (color == SubtitleSetting.SUBTITLE_COLOR_GRAY) return R.string.subtitle_color_gray;
        return R.string.subtitle_color_white;
    }

    private int getBackgroundColorName(int color) {
        if (color == Color.TRANSPARENT) return R.string.subtitle_color_transparent;
        if (color == SubtitleSetting.SUBTITLE_BACKGROUND_DIM) return R.string.subtitle_background_dim;
        if (color == SubtitleSetting.SUBTITLE_BACKGROUND_BLACK) return R.string.subtitle_color_black;
        if (color == SubtitleSetting.SUBTITLE_BACKGROUND_GRAY) return R.string.subtitle_color_gray;
        return R.string.subtitle_color_transparent;
    }

    private String formatScale(float value) {
        return String.format(Locale.US, "%.0f%%", value * 100);
    }

    private String formatPosition(float value) {
        return String.format(Locale.US, "%.0f%%", value);
    }
}