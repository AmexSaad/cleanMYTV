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
    private WindowManager wm; private View overlay;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable watchdog = this::stopSelf;
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
        if ((test || PrayerStore.prefs(this).getBoolean("fullscreen", false)) && Settings.canDrawOverlays(this)) showOverlay(prayer, test, i.getLongExtra("at", 0));
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
            player.setOnPreparedListener(MediaPlayer::start); player.prepareAsync();
        } catch (Exception e) { fail("Audio azan tidak dapat dibuka."); }
        return START_NOT_STICKY;
    }
    private void fail(String message) { PrayerStore.prefs(this).edit().putString("problem", message).apply(); Toast.makeText(this, message, Toast.LENGTH_LONG).show(); stopSelf(); }
    private void showOverlay(int prayer, boolean test, long scheduled) {
        if (test) {
            long now = System.currentTimeMillis(); String today = PrayerClock.date(now);
            scheduled = PrayerStore.cached(this, PrayerStore.zone(this)).stream()
                .filter(e -> e.prayer == prayer && today.equals(PrayerClock.date(e.at))).mapToLong(e -> e.at).findFirst().orElse(now);
        }
        FrameLayout panel = new FrameLayout(this);
        panel.addView(new PrayerPoster(this, prayer, scheduled,
            PrayerStore.prefs(this).getString("place", "Malaysia"), test), new FrameLayout.LayoutParams(-1,-1));
        if (test) {
            Button stop = new Button(this); stop.setText("Tamatkan percubaan"); stop.setOnClickListener(v -> stopSelf());
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.RIGHT); p.setMargins(16,16,24,16);panel.addView(stop,p);
        }
        panel.setFocusableInTouchMode(true); panel.setOnKeyListener((v, key, event) -> key == KeyEvent.KEYCODE_BACK);
        wm = getSystemService(WindowManager.class);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, PixelFormat.OPAQUE);
        try { wm.addView(panel, lp); overlay = panel; panel.requestFocus(); } catch (RuntimeException denied) { overlay = null; }
    }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (player != null) { player.release(); player = null; }
        if (focus != null) getSystemService(AudioManager.class).abandonAudioFocusRequest(focus);
        if (overlay != null) { try { wm.removeView(overlay); } catch (RuntimeException ignored) {} overlay = null; }
        stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy();
    }
}
