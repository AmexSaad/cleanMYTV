package my.cleanmytv.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.cleanmytv.app.Lang
import my.cleanmytv.app.S

@Composable
fun Logo(size: Dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.075f
        drawRoundRect(
            color = Pal.Pandan,
            topLeft = Offset(w * 0.08f, h * 0.14f),
            size = Size(w * 0.84f, h * 0.58f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = stroke)
        )
        drawLine(
            color = Pal.Pandan,
            start = Offset(w * 0.32f, h * 0.88f),
            end = Offset(w * 0.68f, h * 0.88f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // four-point sparkle
        val cx = w * 0.5f
        val cy = h * 0.43f
        val r = w * 0.2f
        val k = r * 0.3f
        val star = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + k, cy - k)
            lineTo(cx + r, cy)
            lineTo(cx + k, cy + k)
            lineTo(cx, cy + r)
            lineTo(cx - k, cy + k)
            lineTo(cx - r, cy)
            lineTo(cx - k, cy - k)
            close()
        }
        drawPath(star, Pal.Songket)
    }
}

@Composable
fun Wordmark(fontSize: TextUnit) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Pal.Text, fontWeight = FontWeight.Light)) { append("clean") }
            withStyle(SpanStyle(color = Pal.Songket, fontWeight = FontWeight.ExtraBold)) { append("MY") }
            withStyle(SpanStyle(color = Pal.Pandan, fontWeight = FontWeight.ExtraBold)) { append("TV") }
        },
        fontSize = fontSize
    )
}

@Composable
fun LangToggle(lang: Lang, onChange: (Lang) -> Unit) {
    val expanded = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Box {
        androidx.compose.material3.TextButton(onClick = { expanded.value = true }) {
            Text("${lang.label} ▾", color = Pal.Pandan, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, softWrap = false)
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            Lang.values().forEach { choice ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(if (choice == Lang.MS) "Bahasa Melayu" else "English") }, onClick = { onChange(choice); expanded.value = false })
            }
        }
    }
}
@Composable
fun TopBar(lang: Lang, horizontalPadding: Dp = 20.dp, onLang: (Lang) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Logo(30.dp)
        Spacer(Modifier.width(10.dp))
        Wordmark(20.sp)
        Spacer(Modifier.weight(1f))
        HelpGuide(lang)
        LangToggle(lang, onLang)
    }
}

@Composable
fun Stepper(current: Int, s: S) {
    val labels = listOf(s.stepConnect, s.stepAnalyse, s.stepClean)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        labels.forEachIndexed { i, label ->
            val done = i < current
            val on = i == current
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done -> Pal.Pandan
                                on -> Pal.Songket
                                else -> Pal.Panel
                            }
                        )
                        .border(1.dp, if (done || on) Color.Transparent else Pal.Line, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (done) "✓" else "${i + 1}",
                        color = if (done || on) Pal.Ink else Pal.Muted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    color = if (on) Pal.Text else Pal.Muted,
                    fontSize = 12.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            if (i < labels.lastIndex) {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(top = 13.dp, start = 8.dp, end = 8.dp)
                        .height(2.dp)
                        .background(if (done) Pal.Pandan else Pal.Line)
                )
            }
        }
    }
}

@Composable
fun Tile(
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Pal.Panel)
            .border(1.dp, if (highlight) Pal.Pandan.copy(alpha = 0.6f) else Pal.Line, shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Pal.Pandan,
            contentColor = Pal.Ink,
            disabledContainerColor = Pal.PanelHi,
            disabledContentColor = Pal.Muted
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Pal.Line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Pal.Text, disabledContentColor = Pal.Muted)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}
