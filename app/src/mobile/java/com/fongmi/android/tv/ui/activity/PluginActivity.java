package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.bean.Plugin;
import com.fongmi.android.tv.bean.PluginHealth;
import com.fongmi.android.tv.databinding.ActivityPluginBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.ui.adapter.PluginAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.utils.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PluginActivity extends BaseActivity implements PluginAdapter.Listener {

    private static final int REQUEST_IMPORT = 1001;

    private ActivityPluginBinding mBinding;
    private PluginAdapter mAdapter;
    private final List<Plugin> mPlugins = new ArrayList<>();
    private boolean mPy = true;
    private boolean mTesting;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PluginActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPluginBinding.inflate(getLayoutInflater());
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
        mBinding.recycler.setAdapter(mAdapter = new PluginAdapter(this, this::updateCount));
        mBinding.tabPy.setOnClickListener(view -> switchTab(true));
        mBinding.tabJs.setOnClickListener(view -> switchTab(false));
        mBinding.btnImport.setOnClickListener(view -> importPlugins());
        mBinding.btnHealth.setOnClickListener(view -> check());
        mBinding.btnDelete.setOnClickListener(view -> deleteSelected());
        mBinding.selectAll.setOnCheckedChangeListener((view, checked) -> {
            if (mAdapter != null) mAdapter.selectAll(checked);
        });
        switchTab(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mTesting) loadPlugins();
    }

    private void switchTab(boolean py) {
        mPy = py;
        mBinding.tabPy.setBackgroundResource(py ? R.drawable.shape_item : 0);
        mBinding.tabJs.setBackgroundResource(py ? 0 : R.drawable.shape_item);
        mBinding.tabPy.setTextColor(py ? 0xFFFFFFFF : 0xFF8A8F98);
        mBinding.tabJs.setTextColor(py ? 0xFF8A8F98 : 0xFFFFFFFF);
        loadPlugins();
    }

    private void loadPlugins() {
        mPlugins.clear();
        String ext = mPy ? ".py" : ".js";
        Set<String> names = new HashSet<>();
        addFiles(new File(getFilesDir(), mPy ? "plugins/py" : "plugins/js"), ext, names);
        addFiles(mPy ? Path.py() : Path.js(), ext, names);
        mPlugins.sort(Comparator.comparing(Plugin::getName));
        mBinding.selectAll.setOnCheckedChangeListener(null);
        mBinding.selectAll.setChecked(false);
        mBinding.selectAll.setOnCheckedChangeListener((view, checked) -> mAdapter.selectAll(checked));
        mAdapter.setItems(mPlugins);
        mBinding.empty.setVisibility(mPlugins.isEmpty() ? View.VISIBLE : View.GONE);
        updateCount();
    }

    private void addFiles(File dir, String ext, Set<String> names) {
        if (dir == null) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(ext));
        if (files == null) return;
        for (File file : files) {
            if (names.add(file.getName())) mPlugins.add(new Plugin(file.getName(), file.getAbsolutePath(), mPy ? "py" : "js"));
        }
    }

    private void updateCount() {
        mBinding.count.setText(getString(R.string.plugin_count, mAdapter.getItemCount()));
    }

    private void importPlugins() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.plugin_import)), REQUEST_IMPORT);
    }

    private void deleteSelected() {
        List<Plugin> selected = mAdapter.getSelected();
        if (selected.isEmpty()) {
            Notify.show(R.string.plugin_no_select);
            return;
        }
        delete(selected);
    }

    private void delete(List<Plugin> plugins) {
        for (Plugin plugin : plugins) {
            File file = new File(plugin.getPath());
            if (file.delete()) PluginHealth.clear(plugin.getPath());
        }
        invalidate();
        loadPlugins();
    }

    private void invalidate() {
        BaseLoader.get().clearSync();
        RefreshEvent.home();
    }

    private void check() {
        if (mTesting) return;
        if (mPlugins.isEmpty()) {
            Notify.show(R.string.plugin_none);
            return;
        }
        mTesting = true;
        Set<String> paths = new HashSet<>();
        for (Plugin plugin : mPlugins) paths.add(plugin.getPath());
        mAdapter.setTesting(paths);
        mAdapter.notifyDataSetChanged();
        Notify.show(getString(R.string.plugin_checking, mPlugins.size()));
        List<Plugin> snapshot = new ArrayList<>(mPlugins);
        Task.execute(() -> {
            int ok = 0;
            int dead = 0;
            for (Plugin plugin : snapshot) {
                long start = System.currentTimeMillis();
                boolean valid = validate(plugin);
                PluginHealth.save(plugin.getPath(), valid ? PluginHealth.OK : PluginHealth.DEAD, System.currentTimeMillis() - start);
                if (valid) ok++;
                else dead++;
            }
            int totalOk = ok;
            int totalDead = dead;
            App.post(() -> {
                mTesting = false;
                mAdapter.setTesting(null);
                mAdapter.notifyDataSetChanged();
                Notify.show(getString(R.string.plugin_check_done, totalOk, 0, totalDead));
            });
        });
    }

    private boolean validate(Plugin plugin) {
        try {
            String text = new String(Path.readToByte(new File(plugin.getPath())), StandardCharsets.UTF_8);
            if (text.trim().isEmpty()) return false;
            if ("py".equals(plugin.getType())) return text.contains("class Spider") && text.contains("homeContent");
            return text.contains("homeContent") && (text.contains("module.exports") || text.contains("rule"));
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void onDelete(Plugin plugin) {
        List<Plugin> list = new ArrayList<>();
        list.add(plugin);
        delete(list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMPORT || resultCode != RESULT_OK || data == null) return;
        try {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) copy(data.getClipData().getItemAt(i).getUri());
            } else if (data.getData() != null) {
                copy(data.getData());
            }
            invalidate();
            loadPlugins();
        } catch (Exception e) {
            Notify.show(e.getMessage());
        }
    }

    private void copy(Uri uri) throws Exception {
        String name = getFileName(uri);
        if (name == null) return;
        String lower = name.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".py") && !lower.endsWith(".js")) return;
        File dir = new File(getFilesDir(), lower.endsWith(".py") ? "plugins/py" : "plugins/js");
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir, name);
        try (InputStream in = getContentResolver().openInputStream(uri); OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            } catch (Exception ignored) {
            }
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }
}
