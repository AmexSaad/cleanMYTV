package tv.cleanmytv.prayer;

import org.junit.Test;
import java.time.*;
import static org.junit.Assert.*;

public class PrayerClockTest {
    @Test public void subuhUsesItsOwnAudioAndTheOtherFourUseRegularAudio() {
        assertEquals("azan_fajr", PrayerClock.audioName(0));
        for(int p=1;p<5;p++)assertEquals("azan_regular",PrayerClock.audioName(p));
        assertEquals(5,PrayerClock.KEYS.length);
        for(String key:PrayerClock.KEYS)assertNotEquals("syuruk",key);
    }
    @Test public void invalidPrayerCannotSelectAudio() { for(int p:new int[]{-1,5,6}){try{PrayerClock.audioName(p);fail();}catch(IllegalArgumentException expected){}} }
    @Test public void timesUseMalaysiaEvenIfDeviceTimezoneIsDifferent() {
        java.util.TimeZone original=java.util.TimeZone.getDefault();
        try{java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/New_York"));
            long at=PrayerClock.at("01-Nov-2026","05:49:00");
            assertEquals(Instant.parse("2026-10-31T21:49:00Z").toEpochMilli(),at);
            assertEquals("2026-11-01",PrayerClock.date(at));assertEquals("05:49",PrayerClock.time(at));
        }finally{java.util.TimeZone.setDefault(original);}
    }
    @Test public void optInZoneAndFreshnessMustAllMatch() {
        assertTrue(PrayerClock.due(true,true,"SGR01","SGR01",1000,1000,0));
        assertFalse(PrayerClock.due(false,true,"SGR01","SGR01",1000,1000,0));
        assertFalse(PrayerClock.due(true,false,"SGR01","SGR01",1000,1000,0));
        assertFalse(PrayerClock.due(true,true,"SGR02","SGR01",1000,1000,0));
        assertFalse(PrayerClock.due(true,true,"SGR01","SGR01",1000,999,0));
        assertFalse(PrayerClock.due(true,true,"SGR01","SGR01",1000,122000,0));
        assertFalse(PrayerClock.due(true,true,"SGR01","SGR01",1000,1000,5));
    }
    @Test public void invalidCalendarDatesAreRejected() { try{PrayerClock.at("31-Feb-2026","06:00:00");fail();}catch(java.time.DateTimeException expected){} }
    @Test public void jakimMalayMonthNamesAreUnderstoodIndependentlyOfDeviceLocale() {
        for(String[] pair:new String[][]{{"Mac","Mar"},{"Mei","May"},{"Ogos","Aug"},{"Okt","Oct"},{"Dis","Dec"}})
            assertEquals(PrayerClock.at("01-"+pair[1]+"-2026","06:00:00"),PrayerClock.at("01-"+pair[0]+"-2026","06:00:00"));
    }
}
