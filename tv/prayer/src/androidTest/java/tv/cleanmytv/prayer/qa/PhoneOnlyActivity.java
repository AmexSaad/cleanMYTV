package tv.cleanmytv.prayer.qa;

/** Test fixture only: deliberately has LAUNCHER but no LEANBACK_LAUNCHER. Not the real Loklok app. */
public class PhoneOnlyActivity extends android.app.Activity {
    @Override public void onCreate(android.os.Bundle state){super.onCreate(state);android.widget.TextView text=new android.widget.TextView(this);text.setText("Loklok QA • ujian app Android biasa\nIni app ujian, bukan Loklok sebenar.");text.setTextColor(android.graphics.Color.WHITE);text.setTextSize(28);text.setGravity(android.view.Gravity.CENTER);text.setBackgroundColor(0xFF17334E);setContentView(text);}
}
