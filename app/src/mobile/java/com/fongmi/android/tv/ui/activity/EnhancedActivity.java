package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.databinding.ActivityEnhancedBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.download.DownloadManager;
import com.github.catvod.utils.Path;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EnhancedActivity extends BaseActivity {

    private ActivityEnhancedBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, EnhancedActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityEnhancedBinding.inflate(getLayoutInflater());
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
        refresh();
    }

    @Override
    protected void initEvent() {
        mBinding.plugin.setOnClickListener(view -> PluginActivity.start(this));
        mBinding.download.setOnClickListener(view -> DownloadActivity.start(this));
        mBinding.lab.setOnClickListener(view -> com.fongmi.android.tv.ui.lab.LabHomeActivity.start(this));
        mBinding.panDriveLogin.setOnClickListener(view -> PanDriveLoginActivity.start(this));
        mBinding.panLogin.setOnClickListener(view -> PanLoginActivity.start(this));
        mBinding.miniApp.setOnClickListener(view -> openMiniApp());
        mBinding.miniAppSwitch.setOnClickListener(view -> toggleMiniApp());
        mBinding.incognito.setOnClickListener(this::setIncognito);
        mBinding.drpy.setOnClickListener(this::setDrpy);
        mBinding.adblock.setOnClickListener(this::setAdblock);
        mBinding.aggregatedSearch.setOnClickListener(this::setAggregatedSearch);
        mBinding.ai.setOnClickListener(view -> AiSettingsActivity.start(this));
        mBinding.tmdb.setOnClickListener(view -> TmdbActivity.start(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBinding != null) refresh();
    }

    private void refresh() {
        int plugins = countPlugins(true) + countPlugins(false);
        mBinding.pluginText.setText(plugins == 0 ? getString(R.string.setting_plugin_desc) : getString(R.string.plugin_count, plugins));
        int tasks = DownloadManager.get().getTasks().size();
        mBinding.downloadText.setText(tasks == 0 ? getString(R.string.setting_download_desc) : getString(R.string.download_count, tasks));
        mBinding.labText.setText(R.string.lab_env_summary);
        mBinding.panDriveLoginText.setText(R.string.pan_drive_unsupported);
        mBinding.panLoginText.setText(PanLoginActivity.getSummary(this));
        mBinding.incognitoText.setText(Setting.getSwitch(Setting.isIncognito()));
        mBinding.drpyText.setText(Setting.getSwitch(Setting.isDrpyBuiltin()));
        mBinding.adblockText.setText(Setting.getSwitch(Setting.isAdblock()));
        mBinding.aggregatedSearchText.setText(Setting.getSwitch(Setting.isAggregatedSearch()));
        boolean enabled = MiniAppActivity.isEnabled(this);
        mBinding.miniAppSwitch.setChecked(enabled);
        mBinding.miniAppText.setText(enabled ? R.string.setting_mini_app_on : R.string.setting_mini_app_off);
        mBinding.aiText.setText(AiSettingsActivity.getSummary(this));
        mBinding.tmdbText.setText(TmdbActivity.getSummary(this));
        String abi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown";
        mBinding.versionInfo.setText(String.format(Locale.US, "版本 v%s\nABI %s\n机型 %s %s\n系统 Android %s (API %d)",
                BuildConfig.VERSION_NAME, abi, Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
    }

    private int countPlugins(boolean py) {
        File own = new File(getFilesDir(), py ? "plugins/py" : "plugins/js");
        File cache = py ? Path.py() : Path.js();
        Set<String> names = new HashSet<>();
        collect(own, py, names);
        collect(cache, py, names);
        return names.size();
    }

    private void collect(File dir, boolean py, Set<String> names) {
        String[] files = dir == null ? null : dir.list();
        if (files == null) return;
        String ext = py ? ".py" : ".js";
        for (String name : files) if (name.endsWith(ext)) names.add(name);
    }

    private void setIncognito(View view) {
        Setting.putIncognito(!Setting.isIncognito());
        mBinding.incognitoText.setText(Setting.getSwitch(Setting.isIncognito()));
    }

    private void setDrpy(View view) {
        Setting.putDrpyBuiltin(!Setting.isDrpyBuiltin());
        mBinding.drpyText.setText(Setting.getSwitch(Setting.isDrpyBuiltin()));
        BaseLoader.get().clear();
    }

    private void setAdblock(View view) {
        Setting.putAdblock(!Setting.isAdblock());
        mBinding.adblockText.setText(Setting.getSwitch(Setting.isAdblock()));
    }

    private void setAggregatedSearch(View view) {
        Setting.putAggregatedSearch(!Setting.isAggregatedSearch());
        mBinding.aggregatedSearchText.setText(Setting.getSwitch(Setting.isAggregatedSearch()));
        RefreshEvent.home();
    }

    private void openMiniApp() {
        MiniAppActivity.setEnabled(this, true);
        MiniAppActivity.start(this);
        refresh();
    }

    private void toggleMiniApp() {
        MiniAppActivity.setEnabled(this, !MiniAppActivity.isEnabled(this));
        refresh();
    }
}
