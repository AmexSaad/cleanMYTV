package tv.cleanmytv.prayer;

import android.content.Context;
import android.graphics.*;
import android.text.*;
import android.view.View;
import java.util.Locale;

/** One packaged background; prayer, scheduled time and selected place are always live text. */
final class PrayerPoster extends View {
    private final Bitmap background;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final int prayer;
    private final long scheduled;
    private final String place;
    private final boolean test;

    PrayerPoster(Context context, int prayer, long scheduled, String place, boolean test) {
        super(context);
        this.prayer = prayer; this.scheduled = scheduled; this.place = place; this.test = test;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.prayer_night);
        setContentDescription("Prayer Time. " + (test ? "Percubaan. " : "Telah masuk waktu solat. ")
            + PrayerClock.NAMES[prayer] + ". " + PrayerClock.posterTime(scheduled) + ". " + place);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        canvas.drawColor(0xFF06142B);
        if (background != null) {
            float scale = Math.max(w / background.getWidth(), h / background.getHeight());
            float bw = background.getWidth() * scale, bh = background.getHeight() * scale;
            paint.setShader(null); paint.setColor(Color.WHITE);
            canvas.drawBitmap(background, null, new RectF((w-bw)/2, (h-bh)/2, (w+bw)/2, (h+bh)/2), paint);
        }
        canvas.save(); canvas.scale(w / 1280f, h / 720f);
        paint.setShader(new LinearGradient(0, 0, 970, 0, new int[]{0xA6031029,0x65031029,0x00031029}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,1280,720,paint); paint.setShader(null);
        text(canvas,"P R A Y E R   T I M E",76,103,18,0xFFCBD9EE,false);
        text(canvas,test ? "PRATONTON WAKTU SOLAT" : "TELAH MASUK WAKTU SOLAT",76,174,30,Color.WHITE,true);
        String name = PrayerClock.NAMES[prayer].toUpperCase(Locale.ROOT);
        paint.setTypeface(Typeface.create("serif",Typeface.BOLD)); paint.setTextSize(100);
        float size = Math.min(100, 715f / paint.measureText(name) * 100);
        text(canvas,name,70,290,size,0xFFFFE4A3,true);
        text(canvas,PrayerClock.posterTime(scheduled),76,373,58,Color.WHITE,true);
        paint.setColor(0x809DB6D7);paint.setStrokeWidth(1);canvas.drawLine(78,410,710,410,paint);
        TextPaint label = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        label.setColor(0xFFE2EBF7); label.setTextSize(22);label.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        StaticLayout location = StaticLayout.Builder.obtain(place.toUpperCase(Locale.ROOT),0,place.length(),label,700)
            .setMaxLines(3).setEllipsize(TextUtils.TruncateAt.END).setIncludePad(false).build();
        canvas.save();canvas.translate(78,437);location.draw(canvas);canvas.restore();
        text(canvas,test ? "PERCUBAAN • Waktu jadual hari ini, atau jam semasa jika jadual belum tersedia" : "Paparan ditutup sendiri selepas azan tamat",78,665,15,0xFFCAD6E9,false);
        canvas.restore();
    }

    private void text(Canvas c,String value,float x,float y,float size,int color,boolean serif) {
        paint.setTypeface(Typeface.create(serif ? "serif" : "sans-serif",Typeface.NORMAL));
        paint.setTextSize(size);paint.setColor(color);paint.setShadowLayer(3,0,2,0x70000000);
        c.drawText(value,x,y,paint);paint.clearShadowLayer();
    }
}
