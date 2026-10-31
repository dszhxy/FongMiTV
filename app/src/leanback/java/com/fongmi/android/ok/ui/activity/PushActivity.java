package com.fongmi.android.ok.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.ok.R;
import com.fongmi.android.ok.databinding.ActivityPushBinding;
import com.fongmi.android.ok.server.Server;
import com.fongmi.android.ok.ui.base.BaseActivity;
import com.fongmi.android.ok.utils.QRCode;
import com.fongmi.android.ok.utils.ResUtil;
import com.fongmi.android.ok.utils.Sniffer;
import com.fongmi.android.ok.utils.Util;

public class PushActivity extends BaseActivity {

    private ActivityPushBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PushActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPushBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        String address = Server.get().getAddress();
        mBinding.code.setImageBitmap(QRCode.getBitmap(address, 250, 1));
        mBinding.info.setText(ResUtil.getString(R.string.push_info, address));
        mBinding.clip.setOnClickListener(this::onClip);
    }

    private void onClip(View view) {
        CharSequence text = Util.getClipText();
        if (!TextUtils.isEmpty(text)) VideoActivity.start(this, Sniffer.getUrl(text.toString()), false);
    }
}
