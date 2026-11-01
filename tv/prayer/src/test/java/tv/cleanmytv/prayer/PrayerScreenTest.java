package tv.cleanmytv.prayer;

import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35,qualifiers="w1280dp-h720dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PrayerScreenTest {
    @Test public void firstLaunchOffersAddAndDoesNotEnableAzan() throws Exception {
        try(org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class)){
            MainActivity a=controller.get();PrayerStore.prefs(a).edit().clear().commit();controller.setup().visible();
            View root=a.getWindow().getDecorView();assertTrue(text(root).contains("+ Tambah waktu solat & azan"));assertFalse(PrayerStore.enabled(a));
            root.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));root.layout(0,0,1280,720);
            Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);root.draw(new Canvas(b));File f=new File("build/prayer-preview.png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}
        }
    }
    @Test public void addedWidgetKeepsAudioOffAndShowsTheTwoAudioTests() throws Exception {
        try(org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class)){
            MainActivity a=controller.get();PrayerStore.prefs(a).edit().clear().putBoolean("added",true).putString("zone","SGR01").putString("place","Selangor / Petaling • SGR01").putString("updated-SGR01",PrayerClock.date(System.currentTimeMillis())).commit();
            String date=java.time.Instant.now().atZone(PrayerClock.MALAYSIA).format(PrayerClock.API_DATE);
            String sample="{\"status\":\"OK!\",\"zone\":\"SGR01\",\"prayerTime\":[{\"date\":\""+date+"\",\"fajr\":\"05:49:00\",\"dhuhr\":\"13:01:00\",\"asr\":\"16:20:00\",\"maghrib\":\"19:00:00\",\"isha\":\"20:12:00\"}]}";
            android.util.AtomicFile f=PrayerStore.file(a,"SGR01");FileOutputStream stream=f.startWrite();stream.write(sample.getBytes(java.nio.charset.StandardCharsets.UTF_8));f.finishWrite(stream);
            controller.setup().visible();View root=a.getWindow().getDecorView();String words=text(root);
            assertTrue(words.contains("Azan automatik: MATI"));assertTrue(words.contains("Cuba azan Subuh"));assertTrue(words.contains("Cuba azan biasa"));assertFalse(PrayerStore.enabled(a));
            root.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));root.layout(0,0,1280,720);
            Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);root.draw(new Canvas(b));try(FileOutputStream out=new FileOutputStream("build/prayer-settings-preview.png")){b.compress(Bitmap.CompressFormat.PNG,100,out);}
        }
    }
    private String text(View v){StringBuilder s=new StringBuilder();if(v instanceof TextView)s.append(((TextView)v).getText());if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)s.append(text(g.getChildAt(i)));}return s.toString();}
}
