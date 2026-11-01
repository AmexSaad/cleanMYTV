package tv.cleanmytv.prayer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** No device-timezone dependency: all Malaysian zones use Asia/Kuala_Lumpur. */
public final class PrayerClock {
    public static final ZoneId MALAYSIA = ZoneId.of("Asia/Kuala_Lumpur");
    public static final String[] KEYS = {"fajr", "dhuhr", "asr", "maghrib", "isha"};
    public static final String[] NAMES = {"Subuh", "Zohor", "Asar", "Maghrib", "Isyak"};
    static final DateTimeFormatter API_DATE = DateTimeFormatter.ofPattern("dd-MMM-uuuu", Locale.ENGLISH)
        .withResolverStyle(java.time.format.ResolverStyle.STRICT);
    public static boolean validPrayer(int prayer) { return prayer >= 0 && prayer < KEYS.length; }
    public static String audioName(int prayer) {
        if (!validPrayer(prayer)) throw new IllegalArgumentException("Not an azan prayer");
        return prayer == 0 ? "azan_fajr" : "azan_regular";
    }
    public static long at(String date, String time) {
        // JAKIM mixes Jan/Feb with Malay Mac/Mei/Ogos/Okt/Dis, independent of the box locale.
        String[] parts=date.split("-");
        if(parts.length!=3)throw new DateTimeException("Invalid date");
        String month=parts[1].toLowerCase(Locale.ROOT);
        String[] english={"jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};
        if(month.equals("mac"))month="mar";if(month.equals("mei"))month="may";if(month.equals("ogos"))month="aug";if(month.equals("okt"))month="oct";if(month.equals("dis"))month="dec";
        int m=Arrays.asList(english).indexOf(month)+1;
        return LocalDate.of(Integer.parseInt(parts[2]),m,Integer.parseInt(parts[0])).atTime(LocalTime.parse(time)).atZone(MALAYSIA).toInstant().toEpochMilli();
    }
    public static boolean due(boolean added, boolean enabled, String zone, String alarmZone, long scheduled, long now, int prayer) {
        return added && enabled && !zone.isEmpty() && zone.equals(alarmZone) && validPrayer(prayer)
            && scheduled > 0 && now >= scheduled && now - scheduled <= 120_000;
    }
    public static String date(long now) { return Instant.ofEpochMilli(now).atZone(MALAYSIA).toLocalDate().toString(); }
    public static String time(long stamp) { return Instant.ofEpochMilli(stamp).atZone(MALAYSIA).format(DateTimeFormatter.ofPattern("HH:mm")); }
}
