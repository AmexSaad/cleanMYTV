package tv.cleanmytv.prayer;

import android.content.*;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

final class PrayerStore {
    static final java.util.concurrent.ExecutorService IO = java.util.concurrent.Executors.newSingleThreadExecutor();
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("prayer", Context.MODE_PRIVATE); }
    static boolean added(Context c) { return prefs(c).getBoolean("added", false); }
    static boolean enabled(Context c) { return added(c) && prefs(c).getBoolean("azan", false); }
    static String zone(Context c) { return prefs(c).getString("zone", ""); }
    static final class Event {
        final int prayer; final long at;
        Event(int prayer, long at) { this.prayer = prayer; this.at = at; }
    }
    static String read(InputStream in) throws IOException {
        try (InputStream input = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[8192]; int n;
            while ((n = input.read(b)) != -1) { if (out.size() + n > 2_000_000) throw new IOException("Data too large"); out.write(b, 0, n); }
            return out.toString("UTF-8");
        }
    }
    static String get(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(12_000); conn.setReadTimeout(15_000);
        try { if (conn.getResponseCode() != 200) throw new IOException("HTTP " + conn.getResponseCode()); return read(conn.getInputStream()); }
        finally { conn.disconnect(); }
    }
    static AtomicFile file(Context c, String zone) {
        if (!zone.matches("[A-Z]{3}[0-9]{2}")) throw new IllegalArgumentException("Invalid zone");
        return new AtomicFile(new File(c.getFilesDir(), "times-" + zone + ".json"));
    }
    static List<Event> parse(String text, String zone) throws JSONException {
        JSONObject obj = new JSONObject(text);
        if (!zone.equals(obj.getString("zone")) || !"OK!".equals(obj.getString("status"))) throw new JSONException("Wrong zone/status");
        JSONArray days = obj.getJSONArray("prayerTime"); List<Event> events = new ArrayList<>(); Set<String> seen = new HashSet<>();
        if (days.length() == 0 || days.length() > 366) throw new JSONException("Invalid calendar");
        for (int i = 0; i < days.length(); i++) {
            JSONObject d = days.getJSONObject(i); long previous = 0;
            if (!seen.add(d.getString("date"))) throw new JSONException("Duplicate date");
            for (int p = 0; p < 5; p++) {
                long at;
                try { at = PrayerClock.at(d.getString("date"), d.getString(PrayerClock.KEYS[p])); }
                catch (RuntimeException e) { throw new JSONException("Invalid prayer time"); }
                if (at <= previous) throw new JSONException("Unordered times");
                events.add(new Event(p, at)); previous = at;
            }
        }
        events.sort(Comparator.comparingLong(e -> e.at)); return events;
    }
    static List<Event> cached(Context c, String zone) {
        try { return parse(read(file(c, zone).openRead()), zone); } catch (Exception e) { return Collections.emptyList(); }
    }
    static boolean hasToday(Context c) {
        String today = PrayerClock.date(System.currentTimeMillis());
        return cached(c, zone(c)).stream().filter(e -> today.equals(PrayerClock.date(e.at))).count() == 5;
    }
    static void sync(Context c) throws Exception {
        String zone = zone(c); if (!added(c) || zone.isEmpty()) return;
        int year = Instant.now().atZone(PrayerClock.MALAYSIA).getYear();
        String text = get("https://solat.my/api/yearly/" + zone + "/" + year);
        List<Event> parsed = parse(text, zone);
        if (parsed.stream().noneMatch(e -> PrayerClock.date(e.at).equals(PrayerClock.date(System.currentTimeMillis())))) throw new IOException("No current timetable");
        AtomicFile f = file(c, zone); FileOutputStream out = null;
        try { out = f.startWrite(); out.write(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)); f.finishWrite(out); }
        catch (IOException e) { if (out != null) f.failWrite(out); throw e; }
        prefs(c).edit().putString("updated-" + zone, PrayerClock.date(System.currentTimeMillis())).apply();
    }
}
