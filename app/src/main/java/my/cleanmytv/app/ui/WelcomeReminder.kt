package my.cleanmytv.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import my.cleanmytv.app.Lang

@Composable
fun WelcomeReminder(lang: Lang) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("welcome_reminder", 0) }
    var show by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(!prefs.getBoolean("hide", false)) }
    var hide by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var help by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    fun finish() {
        if(hide) prefs.edit().putBoolean("hide", true).apply()
        show = false
    }
    if(show) AlertDialog(
        onDismissRequest = { finish() },
        icon = { Logo(56.dp) },
        title = { Text("cleanMYTV") },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sebelum mula", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text("Pilih hanya app yang anda kenal. Sesetengah app diperlukan untuk TV berfungsi.")
                Text("• Telefon dan TV pada rangkaian yang sama.\n• Guna Sejarah & Pulihkan jika ada masalah.\n• Tekan Bantuan untuk panduan sambungan.")
                HorizontalDivider()
                Text("Before you begin", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text("Choose only apps you recognise. Some apps are essential for your TV to work.")
                Text("• Keep your phone and TV on the same network.\n• Use History & Restore if something goes wrong.\n• Open Help for connection instructions.")
                Row {
                    Checkbox(hide, onCheckedChange = { hide = it })
                    TextButton(onClick = { hide = !hide }) { Text("Jangan tunjuk lagi / Don’t show again") }
                }
            }
        },
        confirmButton = { Button(onClick = { finish() }) { Text("Faham / Continue") } },
        dismissButton = { TextButton(onClick = { finish(); help = true }) { Text("Bantuan / Help") } }
    )
    if(help) HelpGuide(lang, autoOpen = true, showButton = false, onClosed = { help = false })
}
