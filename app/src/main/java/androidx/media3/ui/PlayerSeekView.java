package androidx.media3.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;

/**
 * Seek bar used by the VOD/Live control layouts.
 *
 * <p>Wraps a styled {@link DefaultTimeBar} (id {@code exo_progress}) and keeps it in sync with the
 * bound {@link Player}. Updates are driven by {@link Player.Listener} events plus a timer while
 * playing, so the bar reflects timeline, duration, position and buffered position changes made by
 * the playback service.
 */
public class PlayerSeekView extends FrameLayout implements Player.Listener {

    private static final long UPDATE_INTERVAL_MS = 500L;

    private final DefaultTimeBar timeBar;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private @Nullable Player player;

    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            if (player != null && player.isPlaying()) handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    public PlayerSeekView(Context context) {
        this(context, null);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        timeBar = new DefaultTimeBar(context, null, 0, attrs, R.style.ExoStyledControls_TimeBar);
        timeBar.setId(R.id.exo_progress);
        addView(timeBar, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setPlayer(@Nullable Player player) {
        if (this.player == player) return;
        if (this.player != null) this.player.removeListener(this);
        this.player = player;
        if (player != null) player.addListener(this);
        restartUpdates();
    }

    public TimeBar getTimeBar() {
        return timeBar;
    }

    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        restartUpdates();
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        restartUpdates();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        restartUpdates();
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(updater);
        super.onDetachedFromWindow();
    }

    private void restartUpdates() {
        handler.removeCallbacks(updater);
        updateProgress();
        if (player != null && player.isPlaying()) handler.postDelayed(updater, UPDATE_INTERVAL_MS);
    }

    private void updateProgress() {
        Player p = player;
        if (p == null) {
            timeBar.setDuration(0);
            timeBar.setPosition(0);
            timeBar.setBufferedPosition(0);
            timeBar.setEnabled(false);
            return;
        }
        long duration = p.getDuration();
        boolean hasDuration = duration != C.TIME_UNSET && duration > 0;
        long position = Math.max(0L, p.getCurrentPosition());
        long buffered = Math.max(position, p.getBufferedPosition());
        timeBar.setDuration(hasDuration ? duration : 0);
        timeBar.setPosition(hasDuration ? Math.min(position, duration) : 0);
        timeBar.setBufferedPosition(hasDuration ? Math.min(buffered, duration) : 0);
        timeBar.setEnabled(hasDuration && p.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM));
    }
}
