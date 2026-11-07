package com.fongmi.android.tv.ui.dialog;
import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.ui.adapter.ScriptAdapter;
import com.github.catvod.utils.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 源脚本选择器（移植自 xingchen 分支）：搜索框 + PY/JS 双列。
 */
public class SourceScriptsDialog extends Dialog {

    private final OnScriptSelectListener listener;
    private File currentFile;
    private final List<File> allPyFiles = new ArrayList<>();
    private final List<File> allJsFiles = new ArrayList<>();
    private ScriptAdapter pyAdapter;
    private ScriptAdapter jsAdapter;

    public interface OnScriptSelectListener {
        void onScriptSelect(File file, boolean isPy);
    }

    public SourceScriptsDialog(@NonNull Context context, OnScriptSelectListener listener) {
        super(context, DialogThemeHelper.getTheme());
        this.listener = listener;
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.view.LayoutInflater.from(com.fongmi.android.tv.ui.dialog.DialogThemeHelper.wrap(getContext())).inflate(R.layout.dialog_source_scripts, null));
        if (getWindow() != null) getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        try {
            if (getWindow() != null) {
                // 窗口居中、宽度留白、高度自适应；透明底避免整屏刷白
                getWindow().setGravity(android.view.Gravity.CENTER);
                getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.92f);
                lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                lp.softInputMode = android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
                getWindow().setAttributes(lp);
                View content = findViewById(android.R.id.content);
                if (content != null) {
                    float d = getContext().getResources().getDisplayMetrics().density;
                    content.setPadding((int) (12 * d), (int) (8 * d), (int) (12 * d), (int) (8 * d));
                }
            }
        } catch (Exception ignored) {
        }

        RecyclerView rvPy = findViewById(R.id.rv_py);
        RecyclerView rvJs = findViewById(R.id.rv_js);
        LinearLayout colPy = findViewById(R.id.column_py);
        LinearLayout colJs = findViewById(R.id.column_js);
        LinearLayout layoutColumns = findViewById(R.id.layout_columns);
        TextView tvEmpty = findViewById(R.id.tv_empty);

        allPyFiles.addAll(loadScripts(true));
        allJsFiles.addAll(loadScripts(false));

        boolean hasPy = !allPyFiles.isEmpty();
        boolean hasJs = !allJsFiles.isEmpty();

        if (!hasPy && !hasJs) {
            layoutColumns.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutColumns.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            if (hasPy && hasJs) {
                colPy.setVisibility(View.VISIBLE);
                colJs.setVisibility(View.VISIBLE);
            } else if (hasPy) {
                colPy.setVisibility(View.VISIBLE);
                colJs.setVisibility(View.GONE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) colPy.getLayoutParams();
                lp.weight = 1;
                lp.setMarginEnd(0);
                colPy.setLayoutParams(lp);
            } else {
                colPy.setVisibility(View.GONE);
                colJs.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) colJs.getLayoutParams();
                lp.weight = 1;
                lp.setMarginStart(0);
                colJs.setLayoutParams(lp);
            }

            rvPy.setLayoutManager(new LinearLayoutManager(getContext()));
            pyAdapter = new ScriptAdapter(allPyFiles, f -> {
                if (listener != null) listener.onScriptSelect(f, true);
                dismiss();
            });
            pyAdapter.setOnItemLongClickListener(this::showPreview);
            rvPy.setAdapter(pyAdapter);

            rvJs.setLayoutManager(new LinearLayoutManager(getContext()));
            jsAdapter = new ScriptAdapter(allJsFiles, f -> {
                if (listener != null) listener.onScriptSelect(f, false);
                dismiss();
            });
            jsAdapter.setOnItemLongClickListener(this::showPreview);
            rvJs.setAdapter(jsAdapter);

            scrollToCurrent(rvPy, allPyFiles);
            scrollToCurrent(rvJs, allJsFiles);
        }

        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter(s.toString());
            }
        });

        findViewById(android.R.id.content).setOnClickListener(v -> dismiss());
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        if (pyAdapter != null) pyAdapter.updateData(filterFiles(allPyFiles, kw));
        if (jsAdapter != null) jsAdapter.updateData(filterFiles(allJsFiles, kw));
    }

    private List<File> filterFiles(List<File> files, String kw) {
        if (kw.isEmpty()) return new ArrayList<>(files);
        List<File> result = new ArrayList<>();
        for (File f : files) if (f.getName().toLowerCase().contains(kw)) result.add(f);
        return result;
    }

    private List<File> loadScripts(boolean py) {
        Map<String, File> map = new LinkedHashMap<>();
        // 只列用户真正导入的脚本（filesDir/plugins/{py,js}）。
        // 不再扫 Path.py()/Path.js()：那是 getCacheDir() 下的运行期缓存，
        // 里面是一堆 MD5 命名的临时脚本，不是用户导入的源。
        addDir(map, new File(getContext().getFilesDir(), py ? "plugins/py" : "plugins/js"), py);
        List<File> list = new ArrayList<>(map.values());
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return list;
    }

    private void addDir(Map<String, File> map, File dir, boolean py) {
        if (dir == null || !dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return py ? lower.endsWith(".py") : lower.endsWith(".js") || lower.endsWith(".wv");
        });
        if (files == null) return;
        for (File file : files) map.put(file.getAbsolutePath(), file);
    }

    private void scrollToCurrent(RecyclerView rv, List<File> files) {
        if (currentFile == null || files.isEmpty()) return;
        String cur = currentFile.getAbsolutePath();
        for (int i = 0; i < files.size(); i++) {
            if (cur.equals(files.get(i).getAbsolutePath())) {
                rv.scrollToPosition(i);
                break;
            }
        }
    }

    /** 长按脚本条目：预览文件内容。纯代码弹窗，颜色全部走当前弹窗风格的语义色。 */
    private void showPreview(File file) {
        final Context ctx = getContext();
        final float d = ctx.getResources().getDisplayMetrics().density;
        Dialog dlg = new Dialog(ctx, DialogThemeHelper.getTheme());
        dlg.setCancelable(true);
        dlg.setCanceledOnTouchOutside(true);

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (14 * d);
        root.setPadding(pad, pad, pad, pad);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(DialogThemeHelper.resolveColor(ctx, R.attr.dialogCardColor));
        bg.setCornerRadius(16 * d);
        root.setBackground(bg);

        TextView title = new TextView(ctx);
        title.setText(file.getName());
        title.setTextColor(DialogThemeHelper.primaryText(ctx));
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView sub = new TextView(ctx);
        sub.setText(ctx.getString(R.string.script_preview_path, file.getAbsolutePath()));
        sub.setTextColor(DialogThemeHelper.secondaryText(ctx));
        sub.setTextSize(11f);
        sub.setSingleLine(true);
        sub.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        sub.setPadding(0, (int) (4 * d), 0, (int) (10 * d));
        root.addView(sub, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        android.widget.ScrollView sv = new android.widget.ScrollView(ctx);
        sv.setFillViewport(true);
        TextView body = new TextView(ctx);
        body.setText(readPreview(file));
        body.setTextColor(DialogThemeHelper.primaryText(ctx));
        body.setTextSize(12f);
        body.setTypeface(android.graphics.Typeface.MONOSPACE);
        body.setPadding(0, 0, 0, (int) (8 * d));
        sv.addView(body, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT));
        root.addView(sv, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // 按钮规范：底色 = dialogAccent，字色 = dialogOnAccent（跟随当前风格）
        TextView close = new TextView(ctx);
        close.setText(R.string.script_preview_close);
        close.setTextColor(DialogThemeHelper.resolveColor(ctx, R.attr.dialogOnAccent));
        close.setTextSize(14f);
        close.setGravity(android.view.Gravity.CENTER);
        close.setPadding((int) (16 * d), (int) (10 * d), (int) (16 * d), (int) (10 * d));
        android.graphics.drawable.GradientDrawable btn = new android.graphics.drawable.GradientDrawable();
        btn.setColor(DialogThemeHelper.accent(ctx));
        btn.setCornerRadius(10 * d);
        close.setBackground(btn);
        close.setOnClickListener(v -> dlg.dismiss());
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.topMargin = (int) (12 * d);
        root.addView(close, clp);

        dlg.setContentView(root);
        if (dlg.getWindow() != null) {
            dlg.getWindow().setGravity(android.view.Gravity.CENTER);
            dlg.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            android.view.WindowManager.LayoutParams lp = dlg.getWindow().getAttributes();
            lp.width = (int) (ctx.getResources().getDisplayMetrics().widthPixels * 0.92f);
            lp.height = (int) (ctx.getResources().getDisplayMetrics().heightPixels * 0.75f);
            dlg.getWindow().setAttributes(lp);
        }
        dlg.show();
    }

    /** 读脚本内容做文本预览，限制 256KB 防大文件卡顿；用传统流 API 保证 minSdk 24 兼容。 */
    private String readPreview(File f) {
        final int cap = 256 * 1024;
        java.io.FileInputStream in = null;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            in = new java.io.FileInputStream(f);
            byte[] buf = new byte[8192];
            int n;
            boolean truncated = false;
            while ((n = in.read(buf)) > 0) {
                if (bos.size() + n > cap) {
                    bos.write(buf, 0, cap - bos.size());
                    truncated = true;
                    break;
                }
                bos.write(buf, 0, n);
            }
            String text = new String(bos.toByteArray(), "UTF-8");
            if (truncated) text = text + getContext().getString(R.string.script_preview_truncated);
            return text;
        } catch (Exception e) {
            return getContext().getString(R.string.script_preview_error, String.valueOf(e.getMessage()));
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
        }
    }
}
