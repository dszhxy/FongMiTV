package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Plugin;
import com.fongmi.android.tv.bean.PluginHealth;
import com.fongmi.android.tv.databinding.ItemPluginBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginAdapter extends RecyclerView.Adapter<PluginAdapter.ViewHolder> {

    public interface Listener {

        void onDelete(Plugin plugin);
    }

    private final List<Plugin> items = new ArrayList<>();
    private final Set<String> testing = new HashSet<>();
    private final Listener listener;
    private final Runnable onChanged;

    public PluginAdapter(Listener listener, Runnable onChanged) {
        this.listener = listener;
        this.onChanged = onChanged;
    }

    public void setItems(List<Plugin> values) {
        items.clear();
        items.addAll(values);
        notifyDataSetChanged();
    }

    public void setTesting(Set<String> paths) {
        testing.clear();
        if (paths != null) testing.addAll(paths);
    }

    public void selectAll(boolean checked) {
        for (Plugin item : items) item.setSelected(checked);
        notifyDataSetChanged();
        if (onChanged != null) onChanged.run();
    }

    public List<Plugin> getSelected() {
        List<Plugin> result = new ArrayList<>();
        for (Plugin item : items) if (item.isSelected()) result.add(item);
        return result;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPluginBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plugin item = items.get(position);
        holder.binding.name.setText(item.getName());
        bindHealth(holder, item);
        holder.binding.checkbox.setOnCheckedChangeListener(null);
        holder.binding.checkbox.setChecked(item.isSelected());
        holder.binding.checkbox.setOnCheckedChangeListener((view, checked) -> item.setSelected(checked));
        holder.binding.delete.setOnClickListener(view -> listener.onDelete(item));
        // 长按预览源文件内容（可全选复制），与「源脚本」弹窗行为一致
        holder.itemView.setOnLongClickListener(view -> {
            showPreview(holder.itemView, item);
            return true;
        });
    }

    private void bindHealth(ViewHolder holder, Plugin item) {
        int grade;
        long ms = 0;
        if (testing.contains(item.getPath())) {
            grade = PluginHealth.TESTING;
        } else {
            PluginHealth.Result result = PluginHealth.get(item.getPath());
            grade = result.grade;
            ms = result.ms;
        }
        if (grade == PluginHealth.UNKNOWN) {
            holder.binding.health.setVisibility(android.view.View.GONE);
            return;
        }
        holder.binding.health.setVisibility(android.view.View.VISIBLE);
        holder.binding.health.setText(PluginHealth.label(grade, ms));
        holder.binding.health.setTextColor(PluginHealth.color(grade));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPluginBinding binding;

        ViewHolder(ItemPluginBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /** 长按预览：最多读 256KB，弹窗内可全选复制 */
    private void showPreview(android.view.View anchor, Plugin item) {
        java.io.File f = new java.io.File(item.getPath());
        if (f == null || !f.exists()) {
            android.widget.Toast.makeText(anchor.getContext(), "文件不存在", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        String text;
        try {
            byte[] all = com.github.catvod.utils.Path.readToByte(f);
            int max = 256 * 1024;
            if (all.length > max) {
                text = new String(all, 0, max, "UTF-8") + "\n\n… 仅预览前 256 KB";
            } else {
                text = new String(all, "UTF-8");
            }
        } catch (Exception e) {
            text = "无法读取文件：" + e.getMessage();
        }
        final String content = text;
        android.widget.TextView tv = new android.widget.TextView(anchor.getContext());
        tv.setText(content);
        tv.setTextIsSelectable(true);
        tv.setTextSize(12);
        int p = (int) (12 * anchor.getResources().getDisplayMetrics().density);
        tv.setPadding(p, p, p, p);
        android.widget.ScrollView sv = new android.widget.ScrollView(anchor.getContext());
        sv.addView(tv);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(
                anchor.getContext(), com.fongmi.android.tv.ui.dialog.DialogThemeHelper.getTheme())
                .setTitle(f.getName())
                .setView(sv)
                .setPositiveButton("复制全部", (d, w) -> {
                    android.content.ClipboardManager cm =
                            (android.content.ClipboardManager) anchor.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    if (cm != null) cm.setPrimaryClip(android.content.ClipData.newPlainText("script", content));
                    android.widget.Toast.makeText(anchor.getContext(), "已复制", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(com.fongmi.android.tv.R.string.dialog_negative, null)
                .show();
    }
}
