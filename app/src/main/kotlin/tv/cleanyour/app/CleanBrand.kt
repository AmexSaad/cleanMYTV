package tv.cleanyour.app

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Locale
import tv.cleanyour.ui.design.theme.CytTheme

/** Locale changes recreate only the activity; the application-owned TV operation survives. */
internal object CleanLocale {
    fun language(context: Context): String =
        context.getSharedPreferences("cleanmytv-ui", Context.MODE_PRIVATE).getString("language", null)
            ?: if (Locale.getDefault().language in setOf("ms", "in", "id")) "ms" else "en"

    fun wrap(context: Context): Context = context.createConfigurationContext(
        Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(language(context))) }
    )

    fun select(context: Context, language: String) {
        require(language == "ms" || language == "en")
        context.getSharedPreferences("cleanmytv-ui", Context.MODE_PRIVATE).edit().putString("language", language).apply()
        (context as? Activity)?.recreate()
    }
}

@Composable
internal fun CleanBrandFrame(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(CytTheme.colors.bg)) {
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(painterResource(R.drawable.cleanmytv_mark), null, Modifier.size(32.dp))
            Text("cleanMYTV", Modifier.weight(1f), color = CytTheme.colors.textPrimary, style = CytTheme.type.titleMedium)
            Box {
                TextButton(onClick = { menu = true }) {
                    Text(if (CleanLocale.language(context) == "ms") "BM ▾" else "EN ▾", maxLines = 1)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Bahasa Melayu") }, onClick = { menu = false; CleanLocale.select(context, "ms") })
                    DropdownMenuItem(text = { Text("English") }, onClick = { menu = false; CleanLocale.select(context, "en") })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text(stringResource(R.string.clean_support)) }, onClick = {
                        menu = false
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sociabuzz.com/cleanmytv/tribe")))
                    })
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().consumeWindowInsets(WindowInsets.statusBars)) { content() }
    }
}
