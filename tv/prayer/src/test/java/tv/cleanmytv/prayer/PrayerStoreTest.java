package tv.cleanmytv.prayer;

import android.content.Context;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.json.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class PrayerStoreTest {
    private final String sample="{\"status\":\"OK!\",\"zone\":\"SGR01\",\"prayerTime\":[{\"date\":\"01-Nov-2026\",\"fajr\":\"05:49:00\",\"syuruk\":\"06:58:00\",\"dhuhr\":\"13:01:00\",\"asr\":\"16:20:00\",\"maghrib\":\"19:00:00\",\"isha\":\"20:12:00\"}]}";
    @Test public void installationDoesNotOptUserIntoAzanOrWidget() {
        Context c=org.robolectric.RuntimeEnvironment.getApplication(); PrayerStore.prefs(c).edit().clear().commit();
        assertFalse(PrayerStore.added(c)); assertFalse(PrayerStore.enabled(c)); assertEquals("",PrayerStore.zone(c));
        PrayerStore.prefs(c).edit().putBoolean("azan",true).commit(); assertFalse(PrayerStore.enabled(c));
    }
    @Test public void onlyFivePrayersBecomeAlarmsAndNotSunrise() throws Exception {
        java.util.List<PrayerStore.Event> e=PrayerStore.parse(sample,"SGR01");assertEquals(5,e.size());
        for(int p=0;p<5;p++)assertEquals(p,e.get(p).prayer);
        assertEquals("05:49",PrayerClock.time(e.get(0).at));
    }
    @Test public void wrongZoneMissingPrayerInvalidDateAndUnorderedTimeAreRejected() throws Exception {
        for(String bad:new String[]{sample.replace("SGR01","SGR02"),sample.replace("01-Nov","31-Feb"),sample.replace("05:49:00","23:49:00"),sample.replace("\"isha\":\"20:12:00\"","\"other\":\"20:12:00\"")}){
            try{PrayerStore.parse(bad,"SGR01");fail("Invalid timetable accepted");}catch(JSONException expected){}
        }
    }
    @Test public void everyBundledLocationHasAStateAndAValidZone() throws Exception {
        Context c=org.robolectric.RuntimeEnvironment.getApplication(); JSONArray all=new JSONArray(PrayerStore.read(c.getAssets().open("locations.json")));
        java.util.Set<String> zones=new java.util.HashSet<>(),states=new java.util.HashSet<>();
        for(int i=0;i<all.length();i++){JSONObject o=all.getJSONObject(i);assertTrue(o.getString("code").matches("[A-Z]{3}[0-9]{2}")); assertFalse(o.getString("location").isEmpty());zones.add(o.getString("code"));states.add(o.getString("state"));}
        assertTrue(zones.size()>=50);assertTrue(states.contains("Sabah"));assertTrue(states.contains("Sarawak"));assertTrue(states.contains("Selangor"));
    }
    @Test public void actualJakimYearFixtureParsesAll365DaysIncludingMalayMonths() throws Exception {
        String year=PrayerStore.read(getClass().getResourceAsStream("/solat-sgr01-2026.json"));
        java.util.List<PrayerStore.Event> events=PrayerStore.parse(year,"SGR01");
        assertEquals(365*5,events.size());
        assertEquals("2026-01-01",PrayerClock.date(events.get(0).at));
        assertEquals("2026-12-31",PrayerClock.date(events.get(events.size()-1).at));
    }
}
