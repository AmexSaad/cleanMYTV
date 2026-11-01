package tv.cleanmytv.prayer;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Optional TV module: no zone, alarms or sound are enabled merely by installing it. */
public class MainActivity extends Activity {
    private LinearLayout root; private TextView summary;
    private final Handler handler = new Handler(Looper.getMainLooper()); private boolean syncing;
    private final Runnable clock = new Runnable() { @Override public void run() { renderTimes(); handler.postDelayed(this, 30_000); } };
    @Override protected void onCreate(Bundle b) { super.onCreate(b); }
    @Override protected void onResume() { super.onResume(); render(); handler.removeCallbacks(clock); handler.post(clock); PrayerReceiver.schedule(this);
        if (PrayerStore.added(this) && !PrayerStore.zone(this).isEmpty() && !PrayerClock.date(System.currentTimeMillis()).equals(PrayerStore.prefs(this).getString("updated-" + PrayerStore.zone(this), ""))) sync(); }
    @Override protected void onPause() { handler.removeCallbacks(clock); super.onPause(); }
    private TextView text(String s, int size, int color) { TextView v = new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(8,8,8,8); return v; }
    private void button(String s, Runnable action) {
        Button b=new Button(this);b.setText(s);b.setTextSize(18);b.setAllCaps(false);b.setPadding(20,12,20,12);b.setMinHeight(56);
        StateListDrawable background=new StateListDrawable();
        GradientDrawable selected=new GradientDrawable();selected.setColor(0xFFFFE4A3);selected.setCornerRadius(12);
        GradientDrawable rest=new GradientDrawable();rest.setColor(0xFF102849);rest.setCornerRadius(12);rest.setStroke(1,0xFF466386);
        background.addState(new int[]{android.R.attr.state_focused},selected);background.addState(new int[]{android.R.attr.state_pressed},selected);background.addState(new int[]{},rest);b.setBackground(background);
        b.setTextColor(new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{0xFF071A36,0xFF071A36,Color.WHITE}));
        b.setOnClickListener(v->action.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(4,6,4,6);root.addView(b,lp);
    }
    private void render() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);scroll.setBackgroundColor(0xFF06142B);root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(36,24,36,36);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFF132E50,0xFF06142B})); scroll.addView(root); setContentView(scroll);
        root.addView(text("PRAYER TIME  /  MALAYSIA",18,0xFFBED7F5)); root.addView(text("Waktu solat & azan",32,Color.WHITE));
        button("Buka menu TV • + tambah app & widget waktu solat", () -> open(new Intent(this,HomeActivity.class)));
        button("Pilih Prayer Time sebagai menu utama TV", () -> open(new Intent(Settings.ACTION_HOME_SETTINGS)));
        if (!PrayerStore.added(this)) {
            root.addView(text("Fungsi pilihan. Tambah untuk melihat waktu solat Malaysia. Audio azan hanya dihidupkan apabila anda memilihnya.",18,0xFFCAD6E9));
            button("+ Tambah waktu solat & azan", () -> { PrayerStore.prefs(this).edit().putBoolean("added",true).putBoolean("azan",false).apply(); render(); pickState(); });
            button("Kembali", this::finish); return;
        }
        String place = PrayerStore.prefs(this).getString("place", "Belum pilih tempat");
        button("Malaysia › " + place + "  •  Tukar negeri / zon", this::pickState);
        summary = text("Memuatkan jadual…",22,Color.WHITE); root.addView(summary); renderTimes();
        button("Azan automatik: " + (PrayerStore.enabled(this) ? "HIDUP" : "MATI"), () -> {
            if (!PrayerStore.enabled(this)) {
                if (!PrayerStore.hasToday(this)) { toast("Pilih zon dan muat turun jadual hari ini dahulu."); return; }
                if (!PrayerReceiver.exact(this)) { open(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()))); return; }
                if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            boolean next = !PrayerStore.enabled(this); PrayerStore.prefs(this).edit().putBoolean("azan",next).apply();
            if (!next) stopService(new Intent(this,AzanService.class)); PrayerReceiver.schedule(this); render();
        });
        boolean full = PrayerStore.prefs(this).getBoolean("fullscreen",false);
        button("Paparan penuh sehingga azan tamat: " + (full ? "HIDUP" : "MATI"), () -> {
            if (!full && !Settings.canDrawOverlays(this)) { open(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))); return; }
            PrayerStore.prefs(this).edit().putBoolean("fullscreen",!full).apply(); render();
        });
        root.addView(text("Subuh menggunakan rakaman berasingan dengan ‘As-salātu khayrun minan-nawm’. Syuruk tidak memainkan azan. Paparan penuh memerlukan kebenaran di atas app lain. Butang sistem bergantung pada firmware box.",16,0xFFCAD6E9));
        button("▶ Cuba azan Subuh", () -> test(0)); button("▶ Cuba azan biasa", () -> test(1));
        button("▶ Cuba poster & azan • pilih waktu", () -> new AlertDialog.Builder(this).setTitle("Pratonton poster & azan")
            .setItems(PrayerClock.NAMES, (d,w) -> test(w)).setNegativeButton("Batal",null).show());
        button("■ Hentikan percubaan / azan", () -> stopService(new Intent(this,AzanService.class)));
        button("Muat semula jadual", this::sync);
        button("Buang fungsi waktu solat & azan", () -> new AlertDialog.Builder(this).setTitle("Buang fungsi ini?")
            .setMessage("Azan dan jadual latar belakang akan dihentikan. Boleh tambah semula bila-bila masa.")
            .setPositiveButton("Buang", (d,w) -> { PrayerStore.prefs(this).edit().putBoolean("added",false).putBoolean("azan",false).apply(); PrayerReceiver.cancel(this); stopService(new Intent(this,AzanService.class)); render(); })
            .setNegativeButton("Batal",null).show());
        button("Sumber & kredit audio", () -> new AlertDialog.Builder(this).setTitle("Sumber percuma")
            .setMessage("Jadual: e-Solat JAKIM melalui https://solat.my/api\n\nAzan biasa: ejaz215, ‘33937 call-to-prayer-from-the-prophet-s-mo’, Wikimedia Commons.\n\nSubuh: Islamic Center Malmö / Besim Azemi, ‘Eid al-Fitr Fajr azan at Malmö Mosque – 19 August 2012’, Wikimedia Commons. Audio diambil daripada video dan ditukar kepada OGG.\n\nKedua-dua rakaman: CC BY 3.0 — https://creativecommons.org/licenses/by/3.0/\nPautan sumber lengkap: README projek cleanMYTV, tv/prayer/ATTRIBUTION.md. Tiada sokongan rasmi daripada perakam.")
            .setPositiveButton("Tutup",null).show());
        String problem = PrayerStore.prefs(this).getString("problem", ""); if (!problem.isEmpty()) root.addView(text(problem,16,0xFFFFCC80));
    }
    private void test(int prayer) {
        if (!Settings.canDrawOverlays(this)) {
            toast("Benarkan paparan di atas app lain untuk cuba poster. Kemudian tekan Cuba sekali lagi.");
            open(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))); return;
        }
        startForegroundService(new Intent(this,AzanService.class).putExtra("test",true).putExtra("prayer",prayer));
    }
    private void open(Intent i) { try { startActivity(i); } catch (RuntimeException e) { toast("Buka Tetapan Android › Akses khas app untuk memberi kebenaran pada box ini."); } }
    private void toast(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
    private JSONArray locations() throws Exception { return new JSONArray(PrayerStore.read(getAssets().open("locations.json"))); }
    private void pickState() {
        try {
            JSONArray all = locations(); TreeSet<String> states = new TreeSet<>(); for(int i=0;i<all.length();i++) states.add(all.getJSONObject(i).getString("state"));
            String[] names = states.toArray(new String[0]); new AlertDialog.Builder(this).setTitle("Malaysia › Pilih negeri")
                .setItems(names,(d,w)->pickZone(names[w])).setNegativeButton("Batal",null).show();
        } catch(Exception e) { toast("Senarai zon tidak dapat dibuka."); }
    }
    private void pickZone(String state) {
        try {
            JSONArray all=locations(); TreeMap<String,List<String>> zones=new TreeMap<>();
            for(int i=0;i<all.length();i++){ JSONObject o=all.getJSONObject(i); if(state.equals(o.getString("state"))) zones.computeIfAbsent(o.getString("code"), k->new ArrayList<>()).add(o.getString("location")); }
            String[] codes=zones.keySet().toArray(new String[0]), labels=new String[codes.length];
            for(int i=0;i<codes.length;i++) labels[i]=codes[i]+" • "+String.join(", ",zones.get(codes[i]));
            new AlertDialog.Builder(this).setTitle(state+" › Daerah / zon").setItems(labels,(d,w)->{
                PrayerStore.prefs(this).edit().putString("zone",codes[w]).putString("place",state+" / "+labels[w]).putBoolean("azan",false).apply();
                PrayerReceiver.cancel(this); stopService(new Intent(this,AzanService.class)); render(); sync();
            }).setNegativeButton("Kembali",(d,w)->pickState()).show();
        } catch(Exception e) { toast("Senarai zon tidak dapat dibuka."); }
    }
    private void sync() {
        if(syncing || PrayerStore.zone(this).isEmpty()) return; syncing=true; if(summary!=null) summary.setText("Memuat turun jadual JAKIM…");
        String requestedZone=PrayerStore.zone(this);
        PrayerStore.IO.execute(()->{ String error=""; try{ PrayerStore.sync(this); }catch(Exception e){error="Internet tidak tersedia atau jadual belum dapat disahkan. Jadual sah yang disimpan masih digunakan.";}
            PrayerReceiver.schedule(this); String message=error; runOnUiThread(()->{syncing=false; if(isFinishing()||isDestroyed())return;
                if(PrayerStore.added(this) && !requestedZone.equals(PrayerStore.zone(this))){sync();return;}
                renderTimes(); if(!message.isEmpty())toast(message);}); });
    }
    private void renderTimes() {
        if(summary==null || !PrayerStore.added(this))return;
        long now=System.currentTimeMillis(); String today=PrayerClock.date(now); StringBuilder line=new StringBuilder(today+" • Waktu Malaysia (UTC+8)\n\n");
        int count=0; PrayerStore.Event next=null;
        for(PrayerStore.Event e:PrayerStore.cached(this,PrayerStore.zone(this))){
            if(today.equals(PrayerClock.date(e.at))){line.append(PrayerClock.NAMES[e.prayer]).append("  ").append(PrayerClock.time(e.at)).append("     "); count++;}
            if(e.at>now && next==null)next=e;
        }
        if(count!=5)line.append("Pilih zon dan muat turun jadual. Waktu tidak dianggarkan.");
        else if(next!=null)line.append("\n\nSeterusnya: ").append(PrayerClock.NAMES[next.prayer]).append(" • ").append(PrayerClock.time(next.at)).append(" • ").append((next.at-now+59999)/60000).append(" minit lagi");
        line.append("\n\nSumber: e-Solat JAKIM melalui Solat.my");
        if(PrayerStore.enabled(this) && !PrayerReceiver.exact(this))line.append("\n⚠ Kebenaran penggera tepat diperlukan untuk azan automatik.");
        if(PrayerStore.prefs(this).getBoolean("fullscreen",false) && !Settings.canDrawOverlays(this))line.append("\n⚠ Kebenaran paparan penuh belum diberikan; audio sahaja.");
        summary.setText(line.toString());
    }
}
