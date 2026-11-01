package tv.cleanmytv.prayer;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Small native TV Home bundled with Prayer Time. Existing launchers are never removed or disabled. */
public class HomeActivity extends Activity {
    private static final int GOLD=0xFFFFE4A3, INK=0xFF07152A, TEXT=0xFFE7EFFB;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private LinearLayout root,widget; private TextView clock; private boolean visible; private AlertDialog appPicker;
    private List<AppCatalog.App> apps=Collections.emptyList(); private final List<String> dock=new ArrayList<>();
    private JSONObject config=new JSONObject(); private int columns=5;
    private final Runnable minute=new Runnable(){public void run(){if(!visible)return;updateWidget();handler.postDelayed(this,60_000-System.currentTimeMillis()%60_000);}};
    private final BroadcastReceiver packages=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(visible)render();}};
    @Override protected void onCreate(Bundle state){super.onCreate(state);}
    @Override protected void onStart(){super.onStart();IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_PACKAGE_ADDED);f.addAction(Intent.ACTION_PACKAGE_REMOVED);f.addAction(Intent.ACTION_PACKAGE_CHANGED);f.addDataScheme("package");
        if(Build.VERSION.SDK_INT>=33)registerReceiver(packages,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(packages,f);}
    @Override protected void onResume(){super.onResume();visible=true;render();ensureSchedule();handler.removeCallbacks(minute);handler.post(minute);}
    @Override protected void onPause(){visible=false;if(appPicker!=null)appPicker.dismiss();handler.removeCallbacks(minute);super.onPause();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);if(appPicker!=null)appPicker.dismiss();else if(visible)render();}
    @Override protected void onStop(){unregisterReceiver(packages);super.onStop();}
    @Override public void onBackPressed(){/* Home stays available; dialogs handle their own Back. */}
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);return t;}
    private GradientDrawable surface(int fill,int border){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(16));d.setStroke(dp(1),border);return d;}
    private void focus(View v){v.setFocusable(true);v.setFocusableInTouchMode(true);v.setBackground(surface(0xD9132946,0xFF466382));v.setOnFocusChangeListener((view,on)->view.setBackground(surface(on?0xFF27476D:0xD9132946,on?GOLD:0xFF466382)));}
    private Button button(String label,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextColor(TEXT);b.setTextSize(16);focus(b);b.setOnClickListener(v->action.run());return b;}
    private void open(Intent intent){try{startActivity(intent);}catch(RuntimeException denied){Toast.makeText(this,"App tidak dapat dibuka. Semak ia masih dipasang dan dihidupkan.",Toast.LENGTH_LONG).show();}}
    private void prayer(){open(new Intent(this,MainActivity.class));}
    private android.content.SharedPreferences prefs(){return getSharedPreferences("home",MODE_PRIVATE);}

    private void ensureSchedule(){
        PrayerReceiver.schedule(this);
        if(!PrayerStore.added(this) || PrayerStore.zone(this).isEmpty() || PrayerStore.hasToday(this))return;
        String key=PrayerStore.zone(this)+":"+PrayerClock.date(System.currentTimeMillis());
        if(key.equals(prefs().getString("syncAttempt","")))return;
        prefs().edit().putString("syncAttempt",key).apply();Context app=getApplicationContext();
        PrayerStore.IO.execute(()->{try{PrayerStore.sync(app);}catch(Exception unavailable){/* Never substitute estimated times. */}
            finally{PrayerReceiver.schedule(app);handler.post(()->{if(visible && !isDestroyed())updateWidget();});}});
    }

    private void loadDock(){
        config=new JSONObject();File f=new File(getExternalFilesDir(null),"config.json");
        try{if(f.length()>0 && f.length()<=65536)config=new JSONObject(PrayerStore.read(new FileInputStream(f)));}catch(Exception ignored){}
        columns=Math.max(3,Math.min(8,config.optInt("columns",5)));
        JSONArray sent=config.optJSONArray("dock_apps");
        if(sent!=null && !sent.toString().equals(prefs().getString("sentDock","")))prefs().edit().putString("dock",sent.toString()).putString("sentDock",sent.toString()).apply();
        dock.clear();
        try{JSONArray saved=new JSONArray(prefs().getString("dock","[]"));for(int i=0;i<saved.length() && dock.size()<24;i++){String p=saved.getString(i);if(!dock.contains(p))dock.add(p);}}catch(JSONException ignored){}
        if(!prefs().contains("dock")){
            for(String p:new String[]{"com.netflix.ninja","com.netflix.mediaclient","com.google.android.youtube.tv","com.google.android.youtube"})if(find(p)!=null)dock.add(p);
            dock.add(getPackageName());saveDock();
        }
    }
    private void saveDock(){prefs().edit().putString("dock",new JSONArray(dock).toString()).apply();}
    private AppCatalog.App find(String pkg){for(AppCatalog.App app:apps)if(app.pkg.equals(pkg))return app;return null;}

    private void render(){
        apps=AppCatalog.read(this);loadDock();
        FrameLayout frame=new FrameLayout(this);frame.setBackgroundColor(INK);
        File photo=new File(getExternalFilesDir(null),"wallpaper.jpg");
        if(photo.isFile() && photo.length()<=307200){Bitmap image=BitmapFactory.decodeFile(photo.getAbsolutePath());if(image!=null){ImageView bg=new ImageView(this);bg.setImageBitmap(image);bg.setScaleType(ImageView.ScaleType.CENTER_CROP);frame.addView(bg,new FrameLayout.LayoutParams(-1,-1));}}
        View shade=new View(this);shade.setBackgroundColor(0x85020A17);frame.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setFocusable(false);scroll.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);frame.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(32),dp(22),dp(32),dp(24));scroll.addView(root);setContentView(frame);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);root.addView(top);
        LinearLayout brand=new LinearLayout(this);brand.setOrientation(LinearLayout.VERTICAL);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        brand.addView(text("cleanMYTV",18,GOLD));clock=text("",38,TEXT);brand.addView(clock);
        Button settings=button("Tetapan TV",()->open(new Intent(Settings.ACTION_SETTINGS)));settings.setId(View.generateViewId());top.addView(settings,new LinearLayout.LayoutParams(dp(150),dp(54)));
        android.content.pm.ResolveInfo home=getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0);
        if(home==null || home.activityInfo==null || !getPackageName().equals(home.activityInfo.packageName)){
            Button activate=button("Aktifkan menu ini: pilih Prayer Time sebagai Home",()->open(new Intent(Settings.ACTION_HOME_SETTINGS)));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(52));cp.topMargin=dp(12);root.addView(activate,cp);
        }
        widget=new LinearLayout(this);widget.setOrientation(LinearLayout.VERTICAL);widget.setPadding(dp(22),dp(16),dp(22),dp(16));focus(widget);widget.setId(View.generateViewId());widget.setOnClickListener(v->prayer());
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(-1,-2);wp.setMargins(0,dp(18),0,dp(20));root.addView(widget,wp);updateWidget();
        root.addView(text("App pada menu utama",18,TEXT));
        LinearLayout row=null;int position=0;View first=null;List<View> tiles=new ArrayList<>();
        List<AppCatalog.App> shown=new ArrayList<>();for(String pkg:dock){AppCatalog.App app=find(pkg);if(app!=null)shown.add(app);}
        // The + is a real focusable dock tile even when the preceding row is full.
        for(int i=0;i<=shown.size();i++){
            if(position%columns==0){row=new LinearLayout(this);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.setMargins(0,dp(10),0,0);root.addView(row,rp);}
            View tile=i==shown.size()?addTile():appTile(shown.get(i));
            tile.setId(View.generateViewId());tiles.add(tile);if(first==null)first=tile;
            LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(120),1);tp.setMargins(dp(4),0,dp(4),0);row.addView(tile,tp);position++;
        }
        while(position%columns!=0){row.addView(new View(this),new LinearLayout.LayoutParams(0,dp(120),1));position++;}
        LinearLayout actions=new LinearLayout(this);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.setMargins(0,dp(20),0,0);root.addView(actions,ap);
        Button all=button("Semua app / tambah ke dock",this::picker),choose=button("Pilih menu Home",()->open(new Intent(Settings.ACTION_HOME_SETTINGS)));all.setId(View.generateViewId());choose.setId(View.generateViewId());
        actions.addView(all,new LinearLayout.LayoutParams(0,dp(52),1));actions.addView(choose,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView note=text("Tahan OK pada app untuk buang daripada dock. Senarai dibaca terus daripada TV.",13,0xFFB9CAE2);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.setMargins(0,dp(14),0,0);root.addView(note,np);
        if(first!=null){
            widget.setNextFocusDownId(first.getId());widget.setNextFocusUpId(settings.getId());widget.setNextFocusLeftId(widget.getId());widget.setNextFocusRightId(widget.getId());settings.setNextFocusDownId(widget.getId());settings.setNextFocusUpId(settings.getId());
            for(int i=0;i<tiles.size();i++){View tile=tiles.get(i);int col=i%columns;tile.setNextFocusLeftId(col==0?tile.getId():tiles.get(i-1).getId());tile.setNextFocusRightId(col==columns-1 || i==tiles.size()-1?tile.getId():tiles.get(i+1).getId());tile.setNextFocusUpId(i<columns?widget.getId():tiles.get(i-columns).getId());tile.setNextFocusDownId(i+columns<tiles.size()?tiles.get(i+columns).getId():(col<columns/2?all.getId():choose.getId()));}
            int lastRow=(tiles.size()-1)/columns*columns;all.setNextFocusUpId(tiles.get(lastRow).getId());choose.setNextFocusUpId(tiles.get(tiles.size()-1).getId());all.setNextFocusRightId(choose.getId());all.setNextFocusLeftId(all.getId());choose.setNextFocusLeftId(all.getId());choose.setNextFocusRightId(choose.getId());all.setNextFocusDownId(all.getId());choose.setNextFocusDownId(choose.getId());first.requestFocus();
        }
    }
    private View appTile(AppCatalog.App app){
        LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(10),dp(8),dp(10),dp(8));focus(tile);tile.setContentDescription(app.label);
        ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try{image.setImageDrawable(app.pkg.equals(getPackageName())?getDrawable(R.drawable.icon):app.info.loadIcon(getPackageManager()));}catch(RuntimeException ignored){image.setImageDrawable(getDrawable(R.drawable.icon));}
        tile.addView(image,new LinearLayout.LayoutParams(dp(58),dp(58)));TextView name=text(app.label,16,TEXT);name.setGravity(Gravity.CENTER);name.setMaxLines(2);name.setEllipsize(TextUtils.TruncateAt.END);tile.addView(name);
        tile.setOnClickListener(v->open(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(app.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        tile.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle(app.label).setMessage("Buang pintasan ini daripada dock? App kekal dipasang.").setPositiveButton("Buang dari dock",(d,w)->{dock.remove(app.pkg);saveDock();render();}).setNegativeButton("Batal",null).show();return true;});return tile;
    }
    private View addTile(){LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);focus(tile);tile.setContentDescription("Tambah app ke dock");TextView plus=text("+",40,GOLD);tile.addView(plus,new LinearLayout.LayoutParams(-2,-2));tile.addView(text("Tambah app",16,TEXT),new LinearLayout.LayoutParams(-2,-2));tile.setOnClickListener(v->picker());return tile;}

    private void updateWidget(){
        if(widget==null)return;long now=System.currentTimeMillis();
        clock.setText(Instant.ofEpochMilli(now).atZone(PrayerClock.MALAYSIA).format(DateTimeFormatter.ofPattern("h:mm a  •  EEE, d MMM",new Locale("ms","MY"))));
        widget.removeAllViews();LinearLayout title=new LinearLayout(this);title.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon=new ImageView(this);icon.setImageResource(R.drawable.icon);title.addView(icon,new LinearLayout.LayoutParams(dp(32),dp(32)));TextView label=text("  Prayer Time",21,GOLD);title.addView(label);widget.addView(title);
        if(!PrayerStore.added(this) || PrayerStore.zone(this).isEmpty()){widget.setContentDescription("Prayer Time. Tambah waktu solat dan pilih zon Malaysia.");widget.addView(text("+ Tambah waktu solat • pilih negeri dan zon Malaysia",17,TEXT));return;}
        TextView place=text(PrayerStore.prefs(this).getString("place","Malaysia"),14,0xFFCBD9ED);place.setMaxLines(2);place.setEllipsize(TextUtils.TruncateAt.END);widget.addView(place);
        List<PrayerStore.Event> events=PrayerStore.cached(this,PrayerStore.zone(this));String today=PrayerClock.date(now);PrayerStore.Event next=null;int count=0;
        LinearLayout times=new LinearLayout(this);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(8);widget.addView(times,lp);
        for(PrayerStore.Event event:events){if(event.at>now && next==null)next=event;if(!today.equals(PrayerClock.date(event.at)))continue;count++;
            TextView item=text(PrayerClock.NAMES[event.prayer]+"\n"+PrayerClock.time(event.at),18,TEXT);item.setGravity(Gravity.CENTER);times.addView(item,new LinearLayout.LayoutParams(0,-2,1));}
        if(count!=5){times.removeAllViews();times.addView(text("Jadual hari ini belum tersedia. Tekan untuk muat semula.",16,TEXT));}
        if(next!=null)widget.addView(text("Seterusnya: "+PrayerClock.NAMES[next.prayer]+" • "+PrayerClock.time(next.at)+(today.equals(PrayerClock.date(next.at))?"":" • esok"),15,GOLD));
        widget.setContentDescription("Prayer Time. "+PrayerStore.prefs(this).getString("place","Malaysia")+". Tekan untuk tetapan waktu solat dan azan.");
    }

    private void picker(){
        apps=AppCatalog.read(this);dock.removeIf(pkg->find(pkg)==null);saveDock();List<AppCatalog.App> filtered=new ArrayList<>(apps);
        LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(16),dp(8),dp(16),0);
        EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Cari nama app atau pakej, contoh Loklok");content.addView(search,new LinearLayout.LayoutParams(-1,dp(56)));
        ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);content.addView(list,new LinearLayout.LayoutParams(-1,dp(320)));
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice,new ArrayList<>());list.setAdapter(adapter);
        Runnable refresh=()->{adapter.clear();for(AppCatalog.App app:filtered)adapter.add(app.label+"\n"+app.pkg);adapter.notifyDataSetChanged();for(int i=0;i<filtered.size();i++)list.setItemChecked(i,dock.contains(filtered.get(i).pkg));};refresh.run();
        list.setOnItemClickListener((p,v,index,id)->{String pkg=filtered.get(index).pkg;if(dock.contains(pkg))dock.remove(pkg);else if(dock.size()<24)dock.add(pkg);else Toast.makeText(this,"Dock penuh: buang satu app dahulu (maksimum 24).",Toast.LENGTH_LONG).show();saveDock();list.setItemChecked(index,dock.contains(pkg));});
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){String q=s.toString().toLowerCase(Locale.ROOT);filtered.clear();for(AppCatalog.App app:apps)if((app.label+" "+app.pkg).toLowerCase(Locale.ROOT).contains(q))filtered.add(app);refresh.run();}public void afterTextChanged(Editable e){}});
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Tambah app ke dock • "+apps.size()+" app pada TV").setView(content).setPositiveButton("Selesai",null).create();appPicker=dialog;dialog.setOnDismissListener(d->{appPicker=null;if(visible)render();});dialog.setOnShowListener(d->{dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);list.requestFocus();});dialog.show();
    }
}
