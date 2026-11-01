package tv.cleanmytv.prayer;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** User-enabled playback only. A focusable overlay covers apps but never changes device-owner/Home policy. */
public class AzanService extends Service {
    static final String STOP = "tv.cleanmytv.prayer.STOP";
    private MediaPlayer player; private AudioFocusRequest focus;
    private WindowManager wm; private View overlay; private TextView remaining; private ProgressBar progress;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable watchdog = this::stopSelf;
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (player == null) return;
            try {
                int duration = player.getDuration(), position = player.getCurrentPosition();
                if (remaining != null) remaining.setText("Baki " + String.format(java.util.Locale.US, "%d:%02d", (duration-position)/60000, ((duration-position)/1000)%60));
                if (progress != null) { progress.setMax(duration); progress.setProgress(position); }
                handler.postDelayed(this, 500);
            } catch (RuntimeException stopped) { stopSelf(); }
        }
    };
    @Override public IBinder onBind(Intent i) { return null; }
    @Override public int onStartCommand(Intent i, int flags, int id) {
        if (i == null || STOP.equals(i.getAction())) { stopSelf(); return START_NOT_STICKY; }
        int prayer = i.getIntExtra("prayer", -1);
        boolean test = i.getBooleanExtra("test", false);
        if (!PrayerClock.validPrayer(prayer) || !PrayerStore.added(this) || (!test && !PrayerStore.enabled(this))) { stopSelf(); return START_NOT_STICKY; }
        if (!test) {
            long at = i.getLongExtra("at",0); String zone = PrayerStore.zone(this);
            boolean known = PrayerStore.cached(this,zone).stream().anyMatch(e -> e.at == at && e.prayer == prayer);
            if (!known || !PrayerClock.due(true,true,zone,i.getStringExtra("zone"),at,System.currentTimeMillis(),prayer)) { stopSelf(); return START_NOT_STICKY; }
        }
        if (player != null) return START_NOT_STICKY;
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("azan", "Azan", NotificationManager.IMPORTANCE_LOW));
        PendingIntent stop = PendingIntent.getService(this, 0, new Intent(this, AzanService.class).setAction(STOP), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent settings = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        startForeground(12, new Notification.Builder(this, "azan").setSmallIcon(R.drawable.icon)
            .setContentTitle("Azan " + PrayerClock.NAMES[prayer]).setContentText("cleanMYTV • " + PrayerStore.zone(this))
            .setContentIntent(settings).setOngoing(true).addAction(new Notification.Action.Builder(null, "Hentikan azan", stop).build()).build());
        handler.postDelayed(watchdog, 10 * 60_000L); // Always release the screen if a decoder never finishes.
        if (PrayerStore.prefs(this).getBoolean("fullscreen", false) && Settings.canDrawOverlays(this)) showOverlay(prayer, test);
        AudioAttributes attrs = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener(change -> { if (change == AudioManager.AUDIOFOCUS_LOSS) stopSelf(); }).build();
        AudioManager am = getSystemService(AudioManager.class);
        if (am.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { fail("Audio sedang digunakan oleh app lain."); return START_NOT_STICKY; }
        try {
            player = new MediaPlayer(); player.setAudioAttributes(attrs);
            int audio = prayer == 0 ? R.raw.azan_fajr : R.raw.azan_regular;
            try (android.content.res.AssetFileDescriptor fd = getResources().openRawResourceFd(audio)) { player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength()); }
            player.setOnCompletionListener(mp -> stopSelf());
            player.setOnErrorListener((mp, what, extra) -> { fail("Audio azan tidak dapat dimainkan."); return true; });
            player.setOnPreparedListener(mp -> { mp.start(); handler.post(tick); }); player.prepareAsync();
        } catch (Exception e) { fail("Audio azan tidak dapat dibuka."); }
        return START_NOT_STICKY;
    }
    private void fail(String message) { PrayerStore.prefs(this).edit().putString("problem", message).apply(); Toast.makeText(this, message, Toast.LENGTH_LONG).show(); stopSelf(); }
    private void showOverlay(int prayer, boolean test) {
        LinearLayout panel = new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setGravity(Gravity.CENTER); panel.setPadding(48, 32, 48, 32);
        panel.setBackgroundColor(0xFF090D0B);
        TextView brand = label("cleanMYTV  /  WAKTU SOLAT", 18, 0xFFD6FF38);
        TextView arabic = label("حَيَّ عَلَى الصَّلَاةِ", 46, 0xFFD6FF38);
        TextView name = label((test ? "Cuba azan " : "Azan ") + PrayerClock.NAMES[prayer], 42, Color.WHITE);
        panel.addView(brand); panel.addView(arabic); panel.addView(name);
        panel.addView(label(PrayerStore.prefs(this).getString("place", "Malaysia"), 20, 0xFFBAC4B7));
        remaining = label("Azan sedang dimuatkan…", 20, 0xFFBAC4B7); panel.addView(remaining);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); progress.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFD6FF38));
        panel.addView(progress, new LinearLayout.LayoutParams(480, 12));
        panel.addView(label("Paparan ditutup sendiri selepas azan tamat", 16, 0xFFBAC4B7));
        if (test) {
            Button stop = new Button(this); stop.setText("Tamatkan percubaan"); stop.setOnClickListener(v -> stopSelf()); panel.addView(stop);
        }
        panel.setFocusableInTouchMode(true); panel.setOnKeyListener((v, key, event) -> key == KeyEvent.KEYCODE_BACK);
        wm = getSystemService(WindowManager.class);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, PixelFormat.OPAQUE);
        try { wm.addView(panel, lp); overlay = panel; panel.requestFocus(); } catch (RuntimeException denied) { overlay = null; }
    }
    private TextView label(String s, int size, int color) { TextView v = new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setGravity(Gravity.CENTER); v.setPadding(12,12,12,12); return v; }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (player != null) { player.release(); player = null; }
        if (focus != null) getSystemService(AudioManager.class).abandonAudioFocusRequest(focus);
        if (overlay != null) { try { wm.removeView(overlay); } catch (RuntimeException ignored) {} overlay = null; }
        stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy();
    }
}
