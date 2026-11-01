package tv.cleanmytv.prayer;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.time.*;

public class PrayerReceiver extends BroadcastReceiver {
    static final String CALL = "tv.cleanmytv.prayer.CALL", REFRESH = "tv.cleanmytv.prayer.REFRESH";
    static PendingIntent pending(Context c, boolean refresh, String zone, int prayer, long at) {
        Intent i = new Intent(c, PrayerReceiver.class).setAction(refresh ? REFRESH : CALL)
            .putExtra("zone", zone).putExtra("prayer", prayer).putExtra("at", at);
        return PendingIntent.getBroadcast(c, refresh ? 2 : 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    static boolean exact(Context c) { return Build.VERSION.SDK_INT < 31 || c.getSystemService(AlarmManager.class).canScheduleExactAlarms(); }
    static void cancel(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        am.cancel(pending(c, false, "", 0, 0)); am.cancel(pending(c, true, "", 0, 0));
    }
    static void schedule(Context c) {
        cancel(c); if (!PrayerStore.added(c)) return;
        long now = System.currentTimeMillis(); String zone = PrayerStore.zone(c);
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (PrayerStore.enabled(c) && exact(c)) {
            for (PrayerStore.Event e : PrayerStore.cached(c, zone)) if (e.at > now) {
                try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, e.at, pending(c, false, zone, e.prayer, e.at)); }
                catch (SecurityException revoked) { /* Settings shows the missing permission; no inaccurate substitute. */ }
                break;
            }
        }
        long midnight = Instant.ofEpochMilli(now).atZone(PrayerClock.MALAYSIA).toLocalDate().plusDays(1)
            .atTime(0, 5).atZone(PrayerClock.MALAYSIA).toInstant().toEpochMilli();
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnight, pending(c, true, zone, 0, 0));
    }
    @Override public void onReceive(Context context, Intent intent) {
        Context c = context.getApplicationContext();
        if (CALL.equals(intent.getAction())) {
            long at = intent.getLongExtra("at", 0); int p = intent.getIntExtra("prayer", -1);
            String zone = PrayerStore.zone(c), alarmZone = intent.getStringExtra("zone");
            String key = zone + ":" + p + ":" + at;
            // Revalidate against the current cached schedule: a stale PendingIntent cannot call a changed timetable.
            boolean known = PrayerStore.cached(c, zone).stream().anyMatch(e -> e.at == at && e.prayer == p);
            if (known && PrayerClock.due(PrayerStore.added(c), PrayerStore.enabled(c), zone, alarmZone, at, System.currentTimeMillis(), p)
                && !key.equals(PrayerStore.prefs(c).getString("last-call", ""))) {
                try {
                    c.startForegroundService(new Intent(c, AzanService.class).putExtra("prayer", p).putExtra("zone", zone).putExtra("at", at));
                    PrayerStore.prefs(c).edit().putString("last-call", key).commit();
                } catch (RuntimeException blocked) {
                    PrayerStore.prefs(c).edit().putString("problem", "Box menyekat azan latar belakang. Semak kebenaran app.").apply();
                }
            }
            schedule(c); return;
        }
        schedule(c);
        if (!PrayerStore.added(c)) return;
        PendingResult result = goAsync();
        PrayerStore.IO.execute(() -> {
            try { PrayerStore.sync(c); } catch (Exception offline) { /* Keep only dated, zone-matched cached times. */ }
            finally { schedule(c); result.finish(); }
        });
    }
}
