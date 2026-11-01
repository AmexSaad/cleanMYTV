package tv.cleanmytv.prayer;

import android.content.*;
import android.content.pm.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class AppCatalogTest {
    static ResolveInfo app(String pkg,String label,String activity){ResolveInfo ri=new ResolveInfo();ri.nonLocalizedLabel=label;ri.activityInfo=new ActivityInfo();ri.activityInfo.packageName=pkg;ri.activityInfo.name=activity;ri.activityInfo.exported=true;ri.activityInfo.enabled=true;ri.activityInfo.applicationInfo=new ApplicationInfo();ri.activityInfo.applicationInfo.packageName=pkg;ri.activityInfo.applicationInfo.enabled=true;return ri;}
    @Test public void ordinaryAndroidAppsAppearWithTheirRealLabelAndTvEntryWinsDuplicates(){
        Context c=RuntimeEnvironment.getApplication();org.robolectric.shadows.ShadowPackageManager pm=Shadows.shadowOf(c.getPackageManager());
        pm.addResolveInfoForIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),app("com.example.loklokqa","Loklok QA","com.example.loklokqa.PhoneActivity"));
        pm.addResolveInfoForIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),app("com.example.loklokqa","Loklok QA TV","com.example.loklokqa.TvActivity"));
        pm.addResolveInfoForIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),app("com.example.phoneonly","Phone-only QA","com.example.phoneonly.MainActivity"));
        pm.addResolveInfoForIntent(new Intent(Intent.ACTION_MAIN),app("com.example.mainonly","MAIN-only QA","com.example.mainonly.MainActivity"));
        List<AppCatalog.App> list=AppCatalog.read(c);AppCatalog.App loklok=list.stream().filter(a->a.pkg.equals("com.example.loklokqa")).findFirst().get();
        assertEquals("com.example.loklokqa.TvActivity",loklok.component.getClassName());assertEquals(1,list.stream().filter(a->a.pkg.equals(loklok.pkg)).count());
        assertTrue(list.stream().anyMatch(a->a.label.equals("Phone-only QA")));
        assertTrue(list.stream().anyMatch(a->a.label.equals("MAIN-only QA")));
    }
    @Test public void newlyInstalledAppsAreReadAgainWithoutAnOldSnapshotOrLabelCache(){
        Context c=RuntimeEnvironment.getApplication();String pkg="com.example.newplayer";assertFalse(AppCatalog.read(c).stream().anyMatch(a->a.pkg.equals(pkg)));
        Shadows.shadowOf(c.getPackageManager()).addResolveInfoForIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),app(pkg,"New player",pkg+".MainActivity"));
        assertTrue(AppCatalog.read(c).stream().anyMatch(a->a.pkg.equals(pkg)));
    }
}
