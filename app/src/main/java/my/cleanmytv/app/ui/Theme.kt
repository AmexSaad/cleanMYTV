package my.cleanmytv.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Night-navy base with pandan green and songket gold accents. */
object Pal {
    val Ink = Color(0xFF0A1020)
    val Panel = Color(0xFF131C33)
    val PanelHi = Color(0xFF1B2644)
    val Line = Color(0xFF26335A)
    val Pandan = Color(0xFF2BD9A0)
    val Songket = Color(0xFFF5B942)
    val Text = Color(0xFFEAF0FF)
    val Muted = Color(0xFF93A0BF)
    val Danger = Color(0xFFFF6B6B)
}

@Composable
fun CleanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pal.Pandan,
            onPrimary = Pal.Ink,
            secondary = Pal.Songket,
            onSecondary = Pal.Ink,
            background = Pal.Ink,
            onBackground = Pal.Text,
            surface = Pal.Panel,
            onSurface = Pal.Text,
            surfaceVariant = Pal.PanelHi,
            onSurfaceVariant = Pal.Muted,
            outline = Pal.Line,
            error = Pal.Danger
        ),
        content = content
    )
}
