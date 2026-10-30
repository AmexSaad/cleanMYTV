package tv.cleanyour.ui.design.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import tv.cleanyour.ui.design.theme.CytTheme

/** Lightweight vector artwork: scales sharply without adding a large bitmap to the APK. */
@Composable
fun LimeWelcomeArt(modifier: Modifier = Modifier, contentDescription: String? = null) {
    val lime = CytTheme.colors.primary
    val background = CytTheme.colors.bg
    Canvas(modifier.aspectRatio(1.65f).semantics {
        if (contentDescription != null) this.contentDescription = contentDescription
    }) {
        val w = size.width
        val h = size.height
        val ribbon = Path().apply {
            moveTo(0f, h * .76f)
            cubicTo(w * .38f, h * .73f, w * .6f, h * .13f, w, h * .10f)
            lineTo(w, h * .29f)
            cubicTo(w * .56f, h * .29f, w * .40f, h * .91f, 0f, h * .96f)
            close()
        }
        drawPath(ribbon, Brush.linearGradient(listOf(lime.copy(alpha = .3f), lime)))
        val left = w * .2f
        val top = h * .13f
        val width = w * .72f
        val height = h * .66f
        val corner = CornerRadius(w * .022f)
        drawRoundRect(background, Offset(left, top), Size(width, height), corner)
        drawRoundRect(lime.copy(alpha = .78f), Offset(left, top), Size(width, height), corner, style = Stroke(w * .005f))
        drawRoundRect(Brush.linearGradient(listOf(Color(0xFF243421), Color(0xFF0B100C))), Offset(left + w * .014f, top + w * .014f), Size(width - w * .028f, height - w * .028f), corner)
        val wave = Path().apply {
            moveTo(left + w * .02f, top + height * .80f)
            cubicTo(w * .48f, h * .75f, w * .54f, h * .19f, left + width - w * .02f, top + height * .23f)
            lineTo(left + width - w * .02f, top + height * .40f)
            cubicTo(w * .53f, h * .34f, w * .48f, h * .88f, left + w * .02f, top + height * .91f)
            close()
        }
        drawPath(wave, Brush.linearGradient(listOf(lime.copy(alpha = .22f), lime)))
        drawLine(Color(0xFFA7B397), Offset(w * .48f, h * .80f), Offset(w * .45f, h * .91f), w * .012f)
        drawLine(Color(0xFFA7B397), Offset(w * .67f, h * .80f), Offset(w * .70f, h * .91f), w * .012f)
        drawLine(Color(0xFF465140), Offset(w * .14f, h * .94f), Offset(w * .98f, h * .94f), w * .004f)
    }
}
