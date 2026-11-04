package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityAiSettingsBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.github.catvod.utils.Prefers;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AiSettingsActivity extends BaseActivity {

    private ActivityAiSettingsBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, AiSettingsActivity.class));
    }

    public static String getSummary(Context context) {
        String model = Prefers.getString("ai_model");
        String url = Prefers.getString("ai_base_url");
        if (TextUtils.isEmpty(model) && TextUtils.isEmpty(url)) return context.getString(R.string.tmdb_unconfigured);
        return TextUtils.isEmpty(model) ? url : model;
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityAiSettingsBinding.inflate(getLayoutInflater());
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
        mBinding.baseUrl.setText(Prefers.getString("ai_base_url"));
        mBinding.apiKey.setText(Prefers.getString("ai_api_key"));
        mBinding.model.setText(Prefers.getString("ai_model"));
    }

    @Override
    protected void initEvent() {
        mBinding.save.setOnClickListener(view -> onSave());
    }

    private void onSave() {
        Prefers.put("ai_base_url", mBinding.baseUrl.getText().toString().trim());
        Prefers.put("ai_api_key", mBinding.apiKey.getText().toString().trim());
        Prefers.put("ai_model", mBinding.model.getText().toString().trim());
        new MaterialAlertDialogBuilder(this).setMessage(R.string.ai_saved).setPositiveButton(R.string.dialog_positive, null).show();
    }
}
