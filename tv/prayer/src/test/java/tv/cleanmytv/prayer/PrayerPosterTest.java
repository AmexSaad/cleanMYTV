package tv.cleanmytv.prayer;

import android.graphics.*;
import android.view.View;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35,qualifiers="w1280dp-h720dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PrayerPosterTest {
    @Test public void allFivePostersUseTheirActualEventAndChosenPlaceOnOneBackground() throws Exception {
        String[] times={"05:49:00","13:01:00","16:20:00","19:00:00","20:12:00"};
        for(int p=0;p<5;p++) {
            long at=PrayerClock.at("01-Nov-2026",times[p]);
            PrayerPoster view=new PrayerPoster(RuntimeEnvironment.getApplication(),p,at,"Selangor / SGR01 • Petaling, Shah Alam",false);
            String description=view.getContentDescription().toString();
            assertTrue(description.contains(PrayerClock.NAMES[p]));assertTrue(description.contains(PrayerClock.posterTime(at)));
            assertTrue(description.contains("SGR01"));assertFalse(description.contains("Kuala Lumpur"));
            view.measure(View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));
            view.layout(0,0,1280,720);Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));
            File file=new File("build/poster-"+PrayerClock.KEYS[p]+".png");file.getParentFile().mkdirs();
            try(FileOutputStream out=new FileOutputStream(file)){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();
        }
    }
    @Test public void aPreviewDoesNotClaimThatPrayerTimeHasArrived() {
        PrayerPoster view=new PrayerPoster(RuntimeEnvironment.getApplication(),0,System.currentTimeMillis(),"Malaysia",true);
        assertTrue(view.getContentDescription().toString().contains("Percubaan"));
        assertFalse(view.getContentDescription().toString().contains("Telah masuk"));
    }
}
