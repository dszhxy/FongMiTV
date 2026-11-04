package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityDownloadBinding;
import com.fongmi.android.tv.setting.DownloadSetting;
import com.fongmi.android.tv.ui.adapter.DownloadTaskAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.download.DownloadManager;
import com.fongmi.android.tv.utils.download.DownloadTask;
import com.fongmi.android.tv.utils.Notify;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DownloadActivity extends BaseActivity implements DownloadTaskAdapter.Listener, DownloadManager.Listener {

    private ActivityDownloadBinding mBinding;
    private DownloadTaskAdapter mAdapter;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, DownloadActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityDownloadBinding.inflate(getLayoutInflater());
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
        mBinding.recycler.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recycler.setAdapter(mAdapter = new DownloadTaskAdapter(this));
        mBinding.clear.setOnClickListener(view -> onClear());
        mBinding.threads.setOnClickListener(this::setThreads);
        mBinding.dir.setOnClickListener(this::copyDir);
        mBinding.threadsText.setText(String.valueOf(DownloadSetting.getThreads()));
        mBinding.dirText.setText(DownloadSetting.getDir().getAbsolutePath());
        DownloadManager.get().addListener(this);
        refresh();
    }

    private void setThreads(View view) {
        String[] values = new String[DownloadSetting.MAX_THREADS];
        for (int i = 0; i < values.length; i++) values[i] = String.valueOf(i + 1);
        new MaterialAlertDialogBuilder(this).setTitle(R.string.download_threads).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(values, DownloadSetting.getThreads() - 1, (dialog, which) -> {
            DownloadSetting.putThreads(which + 1);
            mBinding.threadsText.setText(String.valueOf(DownloadSetting.getThreads()));
            dialog.dismiss();
        }).show();
    }

    private void copyDir(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("download_dir", DownloadSetting.getDir().getAbsolutePath()));
        Notify.show(R.string.download_dir_copied);
    }

    private void onClear() {
        new MaterialAlertDialogBuilder(this).setTitle(R.string.download_clear).setMessage(R.string.download_clear_confirm).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> DownloadManager.get().clearFinished()).show();
    }

    private void refresh() {
        mAdapter.setItems(DownloadManager.get().getTasks());
        boolean empty = mAdapter.getItemCount() == 0;
        mBinding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        mBinding.count.setText(getString(R.string.download_count, mAdapter.getItemCount()));
    }

    @Override
    public void onChanged() {
        refresh();
    }

    @Override
    protected void onDestroy() {
        DownloadManager.get().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onCancel(DownloadTask task) {
        DownloadManager.get().cancel(task.getId());
    }

    @Override
    public void onRetry(DownloadTask task) {
        DownloadManager.get().retry(task.getId());
    }

    @Override
    public void onDelete(DownloadTask task) {
        DownloadManager.get().remove(task.getId());
    }
}
