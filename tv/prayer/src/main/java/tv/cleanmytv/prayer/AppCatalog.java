package tv.cleanmytv.prayer;

import android.content.*;
import android.content.pm.*;
import java.util.*;

/** Read the TV itself; phone-only apps are not dependent on an old snapshot or a curated list. */
final class AppCatalog {
    static final class App {
        final String pkg, label; final ComponentName component; final ResolveInfo info;
        App(String pkg,String label,ComponentName component,ResolveInfo info) {
            this.pkg=pkg;this.label=label;this.component=component;this.info=info;
        }
    }
    static List<App> read(Context context) {
        PackageManager pm=context.getPackageManager();Map<String,App> apps=new LinkedHashMap<>();
        // Prefer the TV activity, but include ordinary sideloaded Android applications such as Loklok.
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER,null}) {
            Intent query=new Intent(Intent.ACTION_MAIN);if(category!=null)query.addCategory(category);
            for(ResolveInfo ri:pm.queryIntentActivities(query,0)) {
                ActivityInfo a=ri.activityInfo;
                if(a==null || !a.exported || !a.enabled || a.applicationInfo==null || !a.applicationInfo.enabled)continue;
                if(a.name.equals(HomeActivity.class.getName()))continue;
                if(a.permission!=null && pm.checkPermission(a.permission,context.getPackageName())!=PackageManager.PERMISSION_GRANTED)continue;
                String label;
                try{label=ri.loadLabel(pm).toString().replaceAll("[\\p{Cc}\\p{Cf}]"," ").trim();}catch(RuntimeException e){label=a.packageName;}
                if(label.isEmpty())label=a.packageName;
                if(a.packageName.equals(context.getPackageName()))label="Prayer Time";
                apps.putIfAbsent(a.packageName,new App(a.packageName,label,new ComponentName(a.packageName,a.name),ri));
            }
        }
        List<App> list=new ArrayList<>(apps.values());list.sort(Comparator.comparing(a->a.label.toLowerCase(Locale.ROOT)));return list;
    }
}
