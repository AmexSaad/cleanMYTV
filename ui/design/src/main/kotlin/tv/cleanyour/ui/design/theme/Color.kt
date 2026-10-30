package tv.cleanyour.ui.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// DESIGN-SYSTEM 2 and 11. Dark is the default and the brand; light mirrors the roles one to one.
@Immutable
data class CytColors(
    val bg: Color, val surface1: Color, val surface2: Color, val surface3: Color,
    val rim: Color, val outline: Color, val outlineStrong: Color,
    val textPrimary: Color, val textSecondary: Color, val textTertiary: Color,
    val primary: Color, val onPrimary: Color, val primaryPressed: Color, val accent: Color,
    val glowBrand: Color, val glowTv: Color,
    val success: Color, val successContainer: Color, val onSuccessContainer: Color,
    val warning: Color, val warningContainer: Color, val onWarningContainer: Color,
    val danger: Color, val dangerFill: Color, val onDanger: Color,
    val dangerContainer: Color, val onDangerContainer: Color,
    val scrim: Color,
    // Glass recipe (DESIGN-SYSTEM 5.1): fill top and bottom, specular top, rim.
    val glassFillTop: Color, val glassFillBottom: Color, val glassSpecular: Color,
    // The springy highlight: brighter fill, an 80% rim and three outer rings.
    val highlightFillTop: Color, val highlightFillBottom: Color, val highlightSpecular: Color,
    val highlightRim: Color, val highlightRing: Color,
    // Ambient glow core alpha (24% dark, 14% light) and its mid stop.
    val glowCoreAlpha: Float, val glowMidAlpha: Float,
    val isDark: Boolean,
)

val DarkCytColors = CytColors(
    bg = Color(0xFF090D0B), surface1 = Color(0xFF151B17), surface2 = Color(0xFF1C2420),
    surface3 = Color(0xFF29352D), rim = Color(0x33FFFFFF), outline = Color(0x1FFFFFFF),
    outlineStrong = Color(0x5CFFFFFF), textPrimary = Color(0xFFF5F8F2),
    textSecondary = Color(0xFFB8C4B8), textTertiary = Color(0xFFA5B3A6),
    primary = Color(0xFFD6FF38), onPrimary = Color(0xFF111608), primaryPressed = Color(0xFFC4E52B),
    accent = Color(0xFFD6FF38),
    glowBrand = Color(0xFF9CBE25), glowTv = Color(0xFF92AE67),
    success = Color(0xFF30D158), successContainer = Color(0xFF0D2A16),
    onSuccessContainer = Color(0xFF5CE07E), warning = Color(0xFFFFB340),
    warningContainer = Color(0xFF33240A), onWarningContainer = Color(0xFFFFC46B),
    danger = Color(0xFFFF6961), dangerFill = Color(0xFFFF453A), onDanger = Color(0xFF090D0B),
    dangerContainer = Color(0xFF3A1512), onDangerContainer = Color(0xFFFF8A80),
    scrim = Color(0xA3000000),
    glassFillTop = Color.White.copy(alpha = 0.09f), glassFillBottom = Color.White.copy(alpha = 0.04f),
    glassSpecular = Color.White.copy(alpha = 0.08f),
    highlightFillTop = Color.White.copy(alpha = 0.16f), highlightFillBottom = Color.White.copy(alpha = 0.06f),
    highlightSpecular = Color.White.copy(alpha = 0.14f), highlightRim = Color.White.copy(alpha = 0.80f),
    highlightRing = Color(0xFFD6FF38),
    glowCoreAlpha = 0.10f, glowMidAlpha = 0.035f,
    isDark = true,
)

val LightCytColors = CytColors(
    bg = Color(0xFFF5F8FF), surface1 = Color(0xFFFFFFFF), surface2 = Color(0xFFEFF5FF),
    surface3 = Color(0xFFE0E9F7), rim = Color(0x243C6BA5), outline = Color(0x1C356096),
    outlineStrong = Color(0x66557599), textPrimary = Color(0xFF14233C),
    textSecondary = Color(0xFF435671), textTertiary = Color(0xFF536681),
    primary = Color(0xFF0866FF), onPrimary = Color(0xFFFFFFFF), primaryPressed = Color(0xFF004ED1),
    accent = Color(0xFF155DCC),
    glowBrand = Color(0xFF6FAAFF), glowTv = Color(0xFFB5D9F7),
    success = Color(0xFF157030), successContainer = Color(0xFFE3F4E8),
    onSuccessContainer = Color(0xFF0F5A26), warning = Color(0xFF8F5300),
    warningContainer = Color(0xFFFFF1DB), onWarningContainer = Color(0xFF7A4600),
    danger = Color(0xFFC01B2C), dangerFill = Color(0xFFC01B2C), onDanger = Color(0xFFFFFFFF),
    dangerContainer = Color(0xFFFDE7E9), onDangerContainer = Color(0xFFA3121F),
    scrim = Color(0x66000000),
    glassFillTop = Color.White.copy(alpha = 0.72f), glassFillBottom = Color.White.copy(alpha = 0.56f),
    glassSpecular = Color.White.copy(alpha = 0.50f),
    highlightFillTop = Color.White.copy(alpha = 0.95f), highlightFillBottom = Color.White.copy(alpha = 0.80f),
    highlightSpecular = Color.White.copy(alpha = 0.60f), highlightRim = Color(0xFF0866FF).copy(alpha = 0.55f),
    highlightRing = Color(0xFF0866FF),
    glowCoreAlpha = 0.14f, glowMidAlpha = 0.06f,
    isDark = false,
)

// Stock Material 3 components inherit the brand without wrappers (DESIGN-SYSTEM 11).
fun CytColors.toM3(): ColorScheme = (if (isDark) darkColorScheme() else lightColorScheme()).copy(
    primary = primary, onPrimary = onPrimary, background = bg, onBackground = textPrimary,
    surface = bg, onSurface = textPrimary, onSurfaceVariant = textSecondary,
    surfaceContainerLowest = bg, surfaceContainerLow = surface1, surfaceContainer = surface2,
    surfaceContainerHigh = surface3, surfaceContainerHighest = surface3,
    surfaceVariant = surface3, inverseSurface = surface3, inverseOnSurface = textPrimary,
    outline = outlineStrong, outlineVariant = outline, error = danger,
    errorContainer = dangerContainer, onErrorContainer = onDangerContainer,
    secondary = accent, onSecondary = onPrimary, tertiary = accent, scrim = scrim,
    primaryContainer = surface3, onPrimaryContainer = textPrimary,
    secondaryContainer = surface3, onSecondaryContainer = textPrimary,
)
