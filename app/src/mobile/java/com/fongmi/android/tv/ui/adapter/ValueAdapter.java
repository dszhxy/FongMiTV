package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.databinding.AdapterValueBinding;
import com.fongmi.android.tv.impl.FilterListener;

import java.util.List;

public class ValueAdapter extends RecyclerView.Adapter<ValueAdapter.ViewHolder> {

    private final FilterListener listener;
    private final List<Value> mItems;
    private final String mKey;

    public ValueAdapter(FilterListener listener, Filter filter) {
        this.listener = listener;
        this.mItems = filter.getValue();
        this.mKey = filter.getKey();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterValueBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Value item = mItems.get(position);
        holder.binding.text.setText(item.getN());
        holder.binding.text.setSelected(item.isSelected());
        applyStyle(holder.binding.text, item.isSelected());
        holder.binding.text.setOnClickListener(v -> onItemClick(item));
    }

    /** 选中=风格强调色实心 + dialogOnAccent 字色；未选=卡片底色 + 主字色。
     *  用代码构造而非 selector 里写 ?attr，避免 AAPT 对 state-list 主题属性解析失败。 */
    private void applyStyle(android.widget.TextView tv, boolean selected) {
        android.content.Context ctx = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.wrap(tv.getContext());
        int accent = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.resolveColor(ctx, com.fongmi.android.tv.R.attr.dialogAccent);
        int onAccent = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.resolveColor(ctx, com.fongmi.android.tv.R.attr.dialogOnAccent);
        int card = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.resolveColor(ctx, com.fongmi.android.tv.R.attr.dialogCardColor);
        int divider = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.resolveColor(ctx, com.fongmi.android.tv.R.attr.dialogDivider);
        int primary = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.resolveColor(ctx, com.fongmi.android.tv.R.attr.dialogTextPrimary);
        float d = tv.getResources().getDisplayMetrics().density;
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(selected ? accent : card);
        g.setCornerRadius(16 * d);
        if (!selected) g.setStroke(Math.max(1, (int) d), divider);
        tv.setBackground(g);
        tv.setTextColor(selected ? onAccent : primary);
    }

    private void onItemClick(Value value) {
        for (Value item : mItems) item.setSelected(value);
        notifyItemRangeChanged(0, getItemCount());
        listener.setFilter(mKey, value);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterValueBinding binding;

        ViewHolder(@NonNull AdapterValueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}