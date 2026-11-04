package com.fongmi.android.tv.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.utils.Notify;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 首页侧边「脚本」入口：搜索、选择 py/js 脚本并直接作为当前站点使用。
 */
public final class ScriptPickerDialog {

    private ScriptPickerDialog() {
    }

    public static void show(FragmentActivity activity) {
        View view = View.inflate(activity, R.layout.dialog_script_picker, null);
        EditText search = view.findViewById(R.id.search);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        TextView empty = view.findViewById(R.id.empty);
        List<Script> scripts = load(activity);
        recycler.setLayoutManager(new LinearLayoutManager(activity));
        ScriptAdapter adapter = new ScriptAdapter(scripts, activity, script -> select(activity, script));
        recycler.setAdapter(adapter);
        empty.setVisibility(scripts.isEmpty() ? View.VISIBLE : View.GONE);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                List<Script> filtered = filter(scripts, s == null ? "" : s.toString().trim());
                adapter.setItems(filtered);
                empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.setting_plugin)
                .setView(view)
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    private static List<Script> load(Context context) {
        Map<String, Script> map = new LinkedHashMap<>();
        addDir(map, new File(context.getFilesDir(), "plugins/py"), true);
        addDir(map, new File(context.getFilesDir(), "plugins/js"), false);
        addDir(map, Path.py(), true);
        addDir(map, Path.js(), false);
        List<Script> list = new ArrayList<>(map.values());
        list.sort(Comparator.comparing(Script::getName));
        return list;
    }

    private static void addDir(Map<String, Script> map, File dir, boolean py) {
        if (dir == null) return;
        File[] files = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(py ? ".py" : ".js"));
        if (files == null) return;
        for (File file : files) map.put(file.getAbsolutePath(), new Script(file, py));
    }

    private static List<Script> filter(List<Script> scripts, String keyword) {
        if (keyword.isEmpty()) return new ArrayList<>(scripts);
        String kw = keyword.toLowerCase(Locale.ROOT);
        List<Script> result = new ArrayList<>();
        for (Script script : scripts) if (script.getName().toLowerCase(Locale.ROOT).contains(kw)) result.add(script);
        return result;
    }

    private static void select(FragmentActivity activity, Script script) {
        try {
            String name = script.getName();
            String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            Site site = new Site();
            site.setKey((script.py ? "py_" : "js_") + base);
            site.setName(base);
            site.setApi(script.file.getAbsolutePath());
            site.setType(3);
            site.setExt("{}");
            site.setJar("");
            VodConfig.get().setHome(site);
            Notify.show(activity.getString(R.string.script_picker_selected, name));
        } catch (Exception e) {
            Notify.show(e.getMessage());
        }
    }

    private static final class Script {

        private final File file;
        private final boolean py;

        private Script(File file, boolean py) {
            this.file = file;
            this.py = py;
        }

        private String getName() {
            return file.getName();
        }
    }

    private interface OnSelect {
        void onSelect(Script script);
    }

    private static final class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.Holder> {

        private final List<Script> items = new ArrayList<>();
        private final FragmentActivity activity;
        private final OnSelect listener;

        private ScriptAdapter(List<Script> scripts, FragmentActivity activity, OnSelect listener) {
            this.items.addAll(scripts);
            this.activity = activity;
            this.listener = listener;
        }

        private void setItems(List<Script> scripts) {
            items.clear();
            items.addAll(scripts);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView text = new TextView(parent.getContext());
            text.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            text.setBackgroundResource(R.drawable.shape_item);
            text.setGravity(Gravity.CENTER_VERTICAL);
            text.setPadding(20, 28, 20, 28);
            text.setTextColor(Color.WHITE);
            text.setTextSize(15);
            text.setTypeface(Typeface.DEFAULT);
            return new Holder(text);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Script script = items.get(position);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 8;
            holder.text.setLayoutParams(params);
            holder.text.setText(script.getName());
            holder.text.setOnClickListener(v -> listener.onSelect(script));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private static final class Holder extends RecyclerView.ViewHolder {

            private final TextView text;

            private Holder(TextView text) {
                super(text);
                this.text = text;
            }
        }
    }
}
