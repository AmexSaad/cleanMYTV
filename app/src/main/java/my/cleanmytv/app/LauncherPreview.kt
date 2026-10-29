package my.cleanmytv.app

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

@Composable
fun LauncherWallpaper(index: Int, modifier: Modifier = Modifier) {
    val atlas = ImageBitmap.imageResource(R.drawable.mylauncher_wallpapers)
    Canvas(modifier) {
        val cellW = atlas.width / 5
        val cellH = atlas.height / 4
        val i = index.coerceIn(0, 19)
        // Centre crop each independent atlas cell; no captions or gutters in this production asset.
        val ratio = size.width / size.height
        val srcW = minOf(cellW, (cellH * ratio).toInt()).coerceAtLeast(1)
        val srcH = minOf(cellH, (cellW / ratio).toInt()).coerceAtLeast(1)
        drawImage(atlas, IntOffset((i % 5) * cellW + (cellW - srcW) / 2, (i / 5) * cellH + (cellH - srcH) / 2),
            IntSize(srcW, srcH), dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)))
    }
}

@Composable
fun rememberLauncherTime(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(1000) } }
    return now
}

/** This exact widget renderer is shared by the phone preview and the real TV home. */
@Composable
fun LauncherClock(config: LauncherConfig, modifier: Modifier = Modifier, now: LocalDateTime = rememberLauncherTime()) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val c = config
        val locale = Locale.forLanguageTag(if (c.lang == "ms") "ms-MY" else "en-GB")
        val time = now.format(DateTimeFormatter.ofPattern(if (c.hour24) "HH:mm" else "hh:mm", locale))
        val date = now.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
        val day = now.format(DateTimeFormatter.ofPattern("EEEE", locale)).uppercase(locale)
        val ink = if (c.darkText) android.graphics.Color.rgb(15, 30, 52) else android.graphics.Color.WHITE
        drawIntoCanvas { cc ->
            val canvas = cc.nativeCanvas
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            fun text(value: String, x: Float, y: Float, height: Float, bold: Boolean = false, color: Int = ink, mono: Boolean = false) {
                p.color = color; p.style = Paint.Style.FILL; p.textSize = height
                p.typeface = Typeface.create(if (mono) "monospace" else if(c.clock == 1) "sans-serif-thin" else "sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
                p.setShadowLayer(height * .04f, 0f, height * .02f, 0x66000000)
                canvas.drawText(value, x, y, p); p.clearShadowLayer()
            }
            fun rect(x: Float, y: Float, width: Float, height: Float, color: Int, radius: Float = 8f) {
                p.color = color; p.style = Paint.Style.FILL
                canvas.drawRoundRect(x, y, x + width, y + height, radius, radius, p)
            }
            fun line(x: Float, y: Float, xx: Float, yy: Float, color: Int, stroke: Float) {
                p.color = color; p.strokeWidth = stroke; p.strokeCap = Paint.Cap.ROUND; p.style = Paint.Style.STROKE
                canvas.drawLine(x, y, xx, yy, p); p.style = Paint.Style.FILL
            }
            fun circle(x: Float, y: Float, r: Float, color: Int, stroke: Float = 0f) {
                p.color = color; p.strokeWidth = stroke; p.style = if (stroke > 0f) Paint.Style.STROKE else Paint.Style.FILL
                canvas.drawCircle(x, y, r, p); p.style = Paint.Style.FILL
            }
            val timeY = if (c.showDate) h * .55f else h * .70f
            if (!c.showClock && !c.showDate) return@drawIntoCanvas
            if (!c.showClock) { text(day, w * .03f, h * .40f, minOf(h * .19f, w * .095f), true); text(date, w * .03f, h * .72f, w * .08f); return@drawIntoCanvas }
            when (c.clock) {
                2 -> {
                    val glyphs = mapOf('0' to "111101101101111", '1' to "010110010010111", '2' to "111001111100111", '3' to "111001111001111", '4' to "101101111001001", '5' to "111100111001111", '6' to "111100111101111", '7' to "111001001001001", '8' to "111101111101111", '9' to "111101111001111", ':' to "000010000010000")
                    val unit = minOf(w / 21f, h / 9f)
                    time.forEachIndexed { n, ch -> glyphs.getValue(ch).forEachIndexed { j, v -> if (v == '1') circle((n * 4 + j % 3 + 1) * unit, (j / 3 + 1) * unit, unit * .29f, if(c.darkText) ink else 0xFF8CDFFF.toInt()) } }
                }
                3, 4 -> {
                    val x = w * .24f; val y = h * .46f; val r = minOf(w * .21f, h * .40f)
                    if(c.clock == 3) circle(x, y, r, ink, r * .018f)
                    repeat(60) { i ->
                        val a = Math.toRadians(i * 6.0 - 90)
                        val major = i % 5 == 0
                        val inner = r * if(major) .84f else .95f
                        line(x + cos(a).toFloat()*inner, y + sin(a).toFloat()*inner, x + cos(a).toFloat()*r, y + sin(a).toFloat()*r, ink, r * if(major) .026f else .008f)
                    }
                    val hour = Math.toRadians((now.hour % 12 + now.minute / 60.0) * 30 - 90)
                    val minute = Math.toRadians(now.minute * 6.0 - 90)
                    line(x, y, x + cos(hour).toFloat()*r*.50f, y + sin(hour).toFloat()*r*.50f, ink, r*.055f)
                    line(x, y, x + cos(minute).toFloat()*r*.77f, y + sin(minute).toFloat()*r*.77f, ink, r*.035f)
                    circle(x, y, r*.06f, ink)
                    text(time, w*.50f, h*.46f, w*.15f)
                    if(c.showDate) text(date, w*.51f, h*.67f, w*.056f)
                }
                5, 6 -> {
                    val light = c.clock == 6
                    val bg = if(light) 0xFFF3F6FC.toInt() else 0xFF17202D.toInt()
                    val fg = if(light) 0xFF152033.toInt() else android.graphics.Color.WHITE
                    rect(w*.02f, h*.06f, w*.44f, h*.65f, bg, w*.035f)
                    rect(w*.50f, h*.06f, w*.44f, h*.65f, bg, w*.035f)
                    text(time.take(2), w*.06f, h*.57f, w*.30f, true, fg, true)
                    text(time.takeLast(2), w*.54f, h*.57f, w*.30f, true, fg, true)
                    line(w*.02f, h*.38f, w*.94f, h*.38f, 0x55000000, h*.014f)
                }
                8 -> {
                    rect(w*.03f, h*.04f, w*.40f, h*.85f, 0xFFF5F8FF.toInt(), w*.04f)
                    val fg = 0xFF12264C.toInt()
                    text(now.format(DateTimeFormatter.ofPattern("MMM", locale)).uppercase(locale), w*.10f, h*.27f, w*.09f, true, fg)
                    text(now.dayOfMonth.toString(), w*.08f, h*.76f, w*.26f, true, fg)
                    text(time, w*.49f, h*.45f, w*.16f)
                    if(c.showDate) text(now.year.toString(), w*.50f, h*.70f, w*.09f)
                }
                9 -> {
                    if(c.showDate) { text(day, w*.02f, h*.35f, minOf(w*.12f, h*.25f), true); text(date, w*.02f, h*.61f, w*.085f) }
                    text(time, w*.02f, if(c.showDate) h*.85f else h*.64f, if(c.showDate) w*.085f else w*.27f)
                }
                11 -> {
                    val x = w*.5f; val y = h*.45f; val r = h*.40f
                    circle(x, y, r, 0x5544A5FF, h*.06f)
                    p.color = 0xFF70C9FF.toInt(); p.style = Paint.Style.STROKE; p.strokeWidth = h*.06f
                    canvas.drawArc(x-r, y-r, x+r, y+r, -90f, now.minute * 6f, false, p); p.style = Paint.Style.FILL
                    text(time, x-r*.71f, y+h*.03f, r*.50f, true)
                    if(c.showDate) text(now.format(DateTimeFormatter.ofPattern("d MMM", locale)), x-r*.62f, y+h*.20f, r*.29f)
                }
                else -> text(time, w*.02f, timeY, w * .29f, c.clock == 0, mono = c.clock == 7)
            }
            if(c.showDate && c.clock !in listOf(3,4,8,9,11)) text(date, w*.04f, h*.87f, w*.085f, c.clock == 0)
            if(!c.hour24 && c.clock !in listOf(8,9)) text(if(now.hour < 12) "AM" else "PM", w*.80f, h*.99f, w*.055f)
        }
    }
}

@Composable
fun LauncherScene(config: LauncherConfig, modifier: Modifier = Modifier, apps: List<LauncherApp> = emptyList(),
                  widgetModifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit = {}) {
    BoxWithConstraints(modifier) {
        LauncherWallpaper(config.wallpaper, Modifier.matchParentSize())
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = config.dim)))
        val cw = maxWidth * (.32f * config.clockSize)
        val ch = maxHeight * (.32f * config.clockSize)
        LauncherClock(config, Modifier.offset((maxWidth-cw)*config.clockX, (maxHeight-ch)*config.clockY).size(cw, ch).then(widgetModifier))
        content()
    }
}
