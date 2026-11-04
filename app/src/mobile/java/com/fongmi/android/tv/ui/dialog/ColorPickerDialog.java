package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.IntConsumer;

/**
 * 色块选择弹窗：与主题色选择一致，直接显示实际颜色，选中项加白色圆环。
 */
public final class ColorPickerDialog {

    private ColorPickerDialog() {
    }

    public static void show(Activity activity, int titleRes, int[] colors, int current, IntConsumer onPick) {
        int pad = dp(activity, 20);
        int cell = dp(activity, 40);
        int gap = dp(activity, 10);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad / 2, pad, 0);
        AlertDialog[] ref = new AlertDialog[1];
        for (int i = 0; i < colors.length; i += 5) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            if (i > 0) row.setPadding(0, gap, 0, 0);
            for (int j = i; j < Math.min(colors.length, i + 5); j++) {
                int color = colors[j];
                FrameLayout holder = new FrameLayout(activity);
                GradientDrawable ring = new GradientDrawable();
                ring.setShape(GradientDrawable.OVAL);
                ring.setColor(Color.TRANSPARENT);
                if (color == current) ring.setStroke(dp(activity, 2), Color.WHITE);
                holder.setBackground(ring);
                View dot = new View(activity);
                GradientDrawable fill = new GradientDrawable();
                fill.setShape(GradientDrawable.OVAL);
                fill.setColor(color == Color.TRANSPARENT ? 0x22FFFFFF : color);
                fill.setStroke(dp(activity, 1), 0x33FFFFFF);
                dot.setBackground(fill);
                FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(cell, cell);
                dotParams.gravity = Gravity.CENTER;
                holder.addView(dot, dotParams);
                LinearLayout.LayoutParams holderParams = new LinearLayout.LayoutParams(cell + dp(activity, 8), cell + dp(activity, 8));
                holderParams.setMarginStart(gap);
                holderParams.setMarginEnd(gap);
                holder.setLayoutParams(holderParams);
                holder.setClickable(true);
                holder.setOnClickListener(view -> {
                    onPick.accept(color);
                    if (ref[0] != null) ref[0].dismiss();
                });
                row.addView(holder);
            }
            root.addView(row);
        }
        ref[0] = new MaterialAlertDialogBuilder(activity)
                .setTitle(titleRes)
                .setView(root)
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }
}
