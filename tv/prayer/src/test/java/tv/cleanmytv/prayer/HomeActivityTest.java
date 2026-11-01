package tv.cleanmytv.prayer;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35,qualifiers="w1280dp-h720dp-land-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HomeActivityTest {
    @Test public void inlineAddCanPinAPhoneOnlyAppAndWidgetShowsAllFiveTimes() throws Exception {
        try(org.robolectric.android.controller.ActivityController<HomeActivity> controller=Robolectric.buildActivity(HomeActivity.class)){
            HomeActivity a=controller.get();a.getSharedPreferences("home",0).edit().clear().commit();
            PrayerStore.prefs(a).edit().clear().putBoolean("added",true).putString("zone","SGR01").putString("place","Selangor / SGR01 • Petaling, Shah Alam").commit();
            String date=java.time.Instant.now().atZone(PrayerClock.MALAYSIA).format(PrayerClock.API_DATE);
            String sample="{\"status\":\"OK!\",\"zone\":\"SGR01\",\"prayerTime\":[{\"date\":\""+date+"\",\"fajr\":\"05:49:00\",\"dhuhr\":\"13:01:00\",\"asr\":\"16:20:00\",\"maghrib\":\"19:00:00\",\"isha\":\"20:12:00\"}]}";
            android.util.AtomicFile f=PrayerStore.file(a,"SGR01");FileOutputStream out=f.startWrite();out.write(sample.getBytes(java.nio.charset.StandardCharsets.UTF_8));f.finishWrite(out);
            Shadows.shadowOf(a.getPackageManager()).addResolveInfoForIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),AppCatalogTest.app("com.example.loklokqa","Loklok QA","com.example.loklokqa.MainActivity"));
            Shadows.shadowOf(a.getPackageManager()).addResolveInfoForIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),AppCatalogTest.app(a.getPackageName(),"cleanMYTV Home",HomeActivity.class.getName()));
            controller.setup().visible();View root=a.getWindow().getDecorView();
            assertNotNull(findAdd(root));assertTrue(words(root).contains("Prayer Time"));for(String prayer:PrayerClock.NAMES)assertTrue(words(root).contains(prayer));
            findAdd(root).performClick();AlertDialog dialog=(AlertDialog)org.robolectric.shadows.ShadowDialog.getLatestDialog();assertNotNull(dialog);
            ListView list=dialog.getListView(); // Custom content list, not AlertDialog's optional standard list.
            if(list==null)list=findList(dialog.getWindow().getDecorView());assertNotNull(list);
            int target=-1;for(int i=0;i<list.getAdapter().getCount();i++)if(list.getAdapter().getItem(i).toString().contains("Loklok QA"))target=i;
            assertTrue(target>=0);list.performItemClick(null,target,0);dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            assertTrue(a.getSharedPreferences("home",0).getString("dock","").contains("com.example.loklokqa"));
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            root=a.getWindow().getDecorView();assertTrue(words(root).contains("Loklok QA"));assertNotNull(findAdd(root));
            root.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));root.layout(0,0,1280,720);
            Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);root.draw(new Canvas(image));try(FileOutputStream imageOut=new FileOutputStream("build/home-widget-dock.png")){image.compress(Bitmap.CompressFormat.PNG,100,imageOut);}
            assertFalse(PrayerStore.enabled(a)); // Showing a live widget never enables sound.
        }
    }
    private View findAdd(View v){if("Tambah app ke dock".contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup){for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=findAdd(((ViewGroup)v).getChildAt(i));if(found!=null)return found;}}return null;}
    private ListView findList(View v){if(v instanceof ListView)return (ListView)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){ListView list=findList(((ViewGroup)v).getChildAt(i));if(list!=null)return list;}return null;}
    private String words(View v){StringBuilder s=new StringBuilder();if(v instanceof TextView)s.append(((TextView)v).getText());if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)s.append(words(((ViewGroup)v).getChildAt(i)));return s.toString();}
}
