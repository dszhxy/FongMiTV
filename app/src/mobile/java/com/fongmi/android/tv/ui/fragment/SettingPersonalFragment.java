package com.fongmi.android.tv.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.FragmentSettingPersonalBinding;
import com.fongmi.android.tv.setting.DanmakuSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseFragment;

public class SettingPersonalFragment extends BaseFragment {

    private FragmentSettingPersonalBinding mBinding;

    public static SettingPersonalFragment newInstance() {
        return new SettingPersonalFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingPersonalBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.danmakuLoadText.setText(Setting.getSwitch(DanmakuSetting.isLoad()));
    }

    @Override
    protected void initEvent() {
        mBinding.danmakuLoad.setOnClickListener(this::setDanmakuLoad);
    }

    private void setDanmakuLoad(View view) {
        DanmakuSetting.putLoad(!DanmakuSetting.isLoad());
        mBinding.danmakuLoadText.setText(Setting.getSwitch(DanmakuSetting.isLoad()));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) initView();
    }
}
