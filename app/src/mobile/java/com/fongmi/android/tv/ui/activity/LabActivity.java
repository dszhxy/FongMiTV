package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.databinding.ActivityLabBinding;
import com.fongmi.android.tv.setting.DecodeSetting;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.ui.base.BaseActivity;

import java.util.Locale;

public class LabActivity extends BaseActivity {

    private ActivityLabBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, LabActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityLabBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
        mBinding.switchDebug.setChecked(PlayerSetting.isDebug());
        mBinding.switchLibass.setChecked(PlayerSetting.isLibass());
        mBinding.switchTunnel.setChecked(DecodeSetting.isTunnel());
        mBinding.switchRender.setChecked(PlayerSetting.getRender() == PlayerSetting.RENDER_TEXTURE);
        mBinding.info.setText(getInfo());
    }

    @Override
    protected void initEvent() {
        mBinding.switchDebug.setOnCheckedChangeListener((view, checked) -> PlayerSetting.putDebug(checked));
        mBinding.switchLibass.setOnCheckedChangeListener((view, checked) -> PlayerSetting.putLibass(checked));
        mBinding.switchTunnel.setOnCheckedChangeListener((view, checked) -> {
            if (checked && PlayerSetting.getRender() == PlayerSetting.RENDER_TEXTURE) PlayerSetting.putRender(PlayerSetting.RENDER_SURFACE);
            DecodeSetting.putTunnel(checked);
            mBinding.switchRender.setChecked(PlayerSetting.getRender() == PlayerSetting.RENDER_TEXTURE);
        });
        mBinding.switchRender.setOnCheckedChangeListener((view, checked) -> {
            PlayerSetting.putRender(checked ? PlayerSetting.RENDER_TEXTURE : PlayerSetting.RENDER_SURFACE);
            mBinding.switchTunnel.setChecked(DecodeSetting.isTunnel());
        });
    }

    private String getInfo() {
        String abi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown";
        return String.format(Locale.US, "v%s\n%s\n%s %s\nAndroid %s (API %d)",
                BuildConfig.VERSION_NAME, abi, Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
    }
}
