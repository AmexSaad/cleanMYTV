package my.cleanmytv.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Night-navy base with pandan green and songket gold accents. */
object Pal {
    val Ink = Color(0xFFF7F8F2)
    val Panel = Color(0xFFFFFFFF)
    val PanelHi = Color(0xFFE8EFE9)
    val Line = Color(0xFFD3DDD5)
    val Pandan = Color(0xFF176B55)
    val Songket = Color(0xFF9C4C30)
    val Text = Color(0xFF20392F)
    val Muted = Color(0xFF5C7066)
    val Danger = Color(0xFFBA3535)
}

@Composable
fun CleanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Pal.Pandan,
            primaryContainer = Pal.PanelHi,
            onPrimaryContainer = Pal.Text,
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
