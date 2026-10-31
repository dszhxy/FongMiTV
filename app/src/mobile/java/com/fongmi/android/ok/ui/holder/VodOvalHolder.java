package com.fongmi.android.ok.ui.holder;

import androidx.annotation.NonNull;

import com.fongmi.android.ok.bean.Vod;
import com.fongmi.android.ok.databinding.AdapterVodOvalBinding;
import com.fongmi.android.ok.ui.adapter.VodAdapter;
import com.fongmi.android.ok.ui.base.BaseVodHolder;
import com.fongmi.android.ok.utils.ImgUtil;

public class VodOvalHolder extends BaseVodHolder {

    private final VodAdapter.OnClickListener listener;
    private final AdapterVodOvalBinding binding;

    public VodOvalHolder(@NonNull AdapterVodOvalBinding binding, VodAdapter.OnClickListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
    }

    public VodOvalHolder size(int[] size) {
        binding.image.getLayoutParams().width = size[0];
        binding.image.getLayoutParams().height = size[1];
        return this;
    }

    @Override
    public void initView(Vod item) {
        binding.name.setText(item.getVodName());
        binding.name.setVisibility(item.getNameVisible());
        binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        binding.getRoot().setOnLongClickListener(v -> listener.onLongClick(item));
        ImgUtil.oval(item.getVodName(), item.getVodPic(), binding.image);
    }
}
