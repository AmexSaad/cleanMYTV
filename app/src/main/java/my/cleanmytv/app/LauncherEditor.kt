package my.cleanmytv.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LauncherEditor(vm: MainViewModel, close: () -> Unit) {
    val model: LauncherModel = viewModel()
    val ms = vm.lang == Lang.MS
    fun t(a:String,b:String) = if(ms) a else b
    val c = model.config
    val latest by rememberUpdatedState(c)
    var tab by remember { mutableStateOf(0) }
    var fullPreview by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var restore by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var previewSize by remember { mutableStateOf(IntSize(1,1)) }
    LaunchedEffect(Unit) {
        model.host = vm.host; model.port = vm.port
        model.edit(model.config.copy(lang = if(ms) "ms" else "en"))
        if(vm.connected) model.refresh()
    }
    BackHandler { if(!model.busy) close() }
    val sampleTime = rememberLauncherTime()
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = close, enabled = !model.busy) { Text(t("← Kembali", "← Back")) }
                Spacer(Modifier.weight(1f))
                Text("Custom MyLauncher", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(t("Ruang TV, gaya anda.", "Your TV, your style."), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(t("Selepas urus app TV, reka paparan anda di sini. Seret jam dalam pratonton atau gunakan pelaras di bawah.", "After managing TV apps, design your home here. Drag the clock in the preview or use the controls below."), style = MaterialTheme.typography.bodyMedium)
            Card(shape = RoundedCornerShape(20.dp)) {
                Box(Modifier.fillMaxWidth().aspectRatio(16f/9f).onSizeChanged { previewSize = it }) {
                    LauncherScene(c, Modifier.fillMaxSize(), widgetModifier = Modifier.pointerInput(model) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val current = latest
                            val availableW = previewSize.width * (1f-.32f*current.clockSize)
                            val availableH = previewSize.height * (1f-.32f*current.clockSize)
                            model.edit(current.copy(clockX = (current.clockX+drag.x/availableW).coerceIn(0f,1f), clockY = (current.clockY+drag.y/availableH).coerceIn(0f,1f)))
                        }
                    }) { PreviewApps(c) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("Pratonton • waktu telefon", "Preview • phone time"), style = MaterialTheme.typography.labelSmall)
                TextButton(onClick = { fullPreview = true }) { Text(t("Besarkan", "Expand")) }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(t("Jam / Tarikh", "Clock / Date"), "Wallpaper", t("App & Susunan", "Apps & Layout"), t("Tema", "Themes"), "TV").forEachIndexed { i, label ->
                    FilterChip(selected = tab == i, onClick = { tab = i }, label = {Text(label)})
                }
            }
            when(tab) {
                0 -> {
                    Text(t("12 gaya jam & tarikh", "12 clock & date styles"), style = MaterialTheme.typography.titleLarge)
                    (0..11).chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { i ->
                                OutlinedCard(onClick = { model.edit(c.copy(clock = i)) }, modifier = Modifier.weight(1f).border(if(c.clock == i) 2.dp else 0.dp, if(c.clock == i) Color(0xFF0866FF) else Color.Transparent, RoundedCornerShape(12.dp))) {
                                    Box(Modifier.fillMaxWidth().aspectRatio(16f/9f).background(Color(0xFF102745))) {
                                        LauncherClock(c.copy(clock = i, showClock = true, showDate = true, darkText = false), Modifier.fillMaxSize().padding(8.dp), sampleTime)
                                    }
                                    Text("J${(i+1).toString().padStart(2,'0')} · " + (if(ms) clockNamesMs else clockNamesEn)[i], Modifier.padding(10.dp), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    Toggle(t("Paparkan jam", "Show clock"), c.showClock) { model.edit(c.copy(showClock=it)) }
                    Toggle(t("Paparkan tarikh", "Show date"), c.showDate) { model.edit(c.copy(showDate=it)) }
                    Toggle(t("Format 24 jam", "24-hour format"), c.hour24) { model.edit(c.copy(hour24=it)) }
                    Toggle(t("Teks gelap", "Dark text"), c.darkText) { model.edit(c.copy(darkText=it)) }
                    Adjustment(t("Saiz widget", "Widget size"), c.clockSize, .6f..1.5f) { model.edit(c.copy(clockSize=it)) }
                    Adjustment(t("Kedudukan kiri / kanan", "Left / right position"), c.clockX) { model.edit(c.copy(clockX=it)) }
                    Adjustment(t("Kedudukan atas / bawah", "Top / bottom position"), c.clockY) { model.edit(c.copy(clockY=it)) }
                    Text(t("Waktu sebenar pada TV mengikut jam dan zon waktu TV. Tarikh dalam kad kalendar ialah sebahagian reka bentuk J09.", "Live TV time uses the TV clock and time zone. The calendar date is part of the J09 design."), style = MaterialTheme.typography.bodySmall)
                }
                1 -> {
                    Text("20 wallpaper", style = MaterialTheme.typography.titleLarge)
                    (0..19).chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { i ->
                                OutlinedCard(onClick = { model.edit(c.copy(wallpaper=i)) }, modifier = Modifier.weight(1f).border(if(c.wallpaper==i) 2.dp else 0.dp, if(c.wallpaper==i) Color(0xFF0866FF) else Color.Transparent, RoundedCornerShape(12.dp))) {
                                    LauncherWallpaper(i, Modifier.fillMaxWidth().aspectRatio(16f/9f))
                                    Text("W${(i+1).toString().padStart(2,'0')} · " + (if(ms) wallpaperNamesMs else wallpaperNamesEn)[i], Modifier.padding(10.dp), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    Adjustment(t("Gelapkan wallpaper", "Dim wallpaper"), c.dim, 0f.. .7f) { model.edit(c.copy(dim=it)) }
                    Text(t("Koleksi imej asal dijana untuk cleanMYTV; boleh digunakan tanpa muat turun tambahan.", "Original image collection generated for cleanMYTV; available without extra downloads."), style = MaterialTheme.typography.bodySmall)
                }
                2 -> {
                    Text(t("Susunan app", "App layout"), style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!c.vertical, {model.edit(c.copy(vertical=false))}, label={Text(t("Melintang", "Horizontal"))})
                        FilterChip(c.vertical, {model.edit(c.copy(vertical=true))}, label={Text(t("Menegak", "Vertical"))})
                    }
                    OutlinedButton(onClick = {model.refresh()}, enabled = !model.busy) { Text(t("Imbas semula app TV", "Refresh TV apps")) }
                    Text(t("Hanya app aktif yang boleh dibuka akan disenaraikan. Nama pakej dipaparkan di telefon; ikon dan nama sebenar dipaparkan pada TV. Maksimum 60 pilihan.", "Only enabled launchable apps are listed. The phone shows package names; the TV shows real names and icons. Maximum 60 selections."), style=MaterialTheme.typography.bodySmall)
                    c.apps.forEachIndexed { i, pkg ->
                        OutlinedCard {
                            Column(Modifier.fillMaxWidth().padding(10.dp)) {
                                Text("${i+1}. $pkg", style=MaterialTheme.typography.bodySmall)
                                Row {
                                    TextButton(onClick={ val list=c.apps.toMutableList(); java.util.Collections.swap(list,i,i-1); model.edit(c.copy(apps=list)) }, enabled=i>0) {Text("↑")}
                                    TextButton(onClick={ val list=c.apps.toMutableList(); java.util.Collections.swap(list,i,i+1); model.edit(c.copy(apps=list)) }, enabled=i<c.apps.lastIndex) {Text("↓")}
                                    TextButton(onClick={model.edit(c.copy(apps=c.apps-pkg))}) {Text(t("Keluarkan", "Remove"))}
                                }
                            }
                        }
                    }
                    Text(t("Tambah ke paparan", "Add to home"), style=MaterialTheme.typography.titleMedium)
                    model.apps.filter {it.pkg !in c.apps}.forEach { app ->
                        TextButton(onClick={model.edit(c.copy(apps=c.apps+app.pkg))}, enabled=c.apps.size<60) {Text("+ ${app.pkg}", style=MaterialTheme.typography.bodySmall)}
                    }
                    if(model.apps.isEmpty()) Text(t("Sambungkan TV dan tekan imbas semula untuk membaca app sebenar.", "Connect your TV and refresh to read its actual apps."))
                }
                3 -> {
                    Text(t("Tema saya", "My themes"), style=MaterialTheme.typography.titleLarge)
                    Text(t("Draf disimpan automatik pada telefon. Simpan sehingga 10 tema; setiap tema termasuk susunan app.", "Drafts save automatically on the phone. Save up to 10 themes, including app order."))
                    OutlinedTextField(name,{name=it.take(36)},label={Text(t("Nama tema", "Theme name"))},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Button(onClick={model.save(name)},enabled=name.isNotBlank()) {Text(t("Simpan tema", "Save theme"))}
                    model.saved.keys.forEach { key ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                            Text(key,Modifier.weight(1f))
                            TextButton(onClick={model.load(key)}) {Text(t("Guna", "Use"))}
                            TextButton(onClick={model.delete(key)}) {Text(t("Padam", "Delete"))}
                        }
                    }
                }
                4 -> {
                    Text(t("Hantar ke TV anda", "Send to your TV"),style=MaterialTheme.typography.titleLarge)
                    Text(t("Telefon dan TV perlu pada Wi-Fi yang sama, dengan ADB dibenarkan. Penghantaran memasang komponen MyLauncher daripada APK telefon ini pada TV.", "Keep phone and TV on the same Wi-Fi with ADB allowed. Sending installs the MyLauncher component from this phone APK on the TV."))
                    OutlinedTextField(model.host,{model.host=it},enabled=!model.busy,label={Text(t("Alamat IP TV", "TV IP address"))},singleLine=true,modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(model.port,{model.port=it},enabled=!model.busy,label={Text("Port")},singleLine=true)
                    OutlinedButton(onClick={model.refresh()},enabled=!model.busy) {Text(t("Baca app TV", "Read TV apps"))}
                    OutlinedButton(onClick={model.homeSettings()},enabled=!model.busy) {Text(t("Pilih launcher lalai pada TV", "Choose default launcher on TV"))}
                    OutlinedButton(onClick={restore=true},enabled=!model.busy) {Text(t("Kembali ke launcher asal", "Restore original launcher"))}
                    Text(t("MyLauncher boleh dibuka tanpa menukar launcher lalai. Butang Home bergantung pada kebenaran firmware TV. Launcher asal tidak dinyahaktifkan.", "MyLauncher can open without changing the default. Home-button support depends on TV firmware. The original launcher remains enabled."),style=MaterialTheme.typography.bodySmall)
                }
            }
            if(model.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if(model.message.isNotBlank()) Surface(color=Color(0xFFEAF2FF),shape=RoundedCornerShape(14.dp)) { Text(model.message,Modifier.padding(14.dp)) }
            Button(onClick={confirm=true},enabled=!model.busy,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {Text(t("Pasang & hantar reka bentuk ke TV", "Install & send design to TV"))}
            Spacer(Modifier.height(16.dp))
        }
    }
    if(fullPreview) Dialog(onDismissRequest={fullPreview=false},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface {Column(Modifier.padding(12.dp)) {
            LauncherScene(c,Modifier.fillMaxWidth().aspectRatio(16f/9f)) {PreviewApps(c)}
            TextButton(onClick={fullPreview=false}) {Text(t("Tutup pratonton", "Close preview"))}
        }}
    }
    if(confirm) AlertDialog(onDismissRequest={confirm=false},title={Text(t("Hantar ke TV ini?", "Send to this TV?"))},
        text={Text("${model.host}:${model.port}\n\n"+t("MyLauncher akan dipasang atau dikemas kini pada TV, kemudian reka bentuk ini dibuka. Launcher asal kekal tersedia. Pastikan IP ini betul dan kekalkan app terbuka sehingga selesai.", "MyLauncher will be installed or updated on the TV, then this design will open. The original launcher stays available. Check this IP and keep the app open until finished."))},
        confirmButton={TextButton(onClick={confirm=false;model.applyToTv()}){Text(t("Hantar", "Send"))}},dismissButton={TextButton(onClick={confirm=false}){Text(t("Batal", "Cancel"))}})
    if(restore) AlertDialog(onDismissRequest={restore=false},title={Text(t("Pulihkan launcher asal?", "Restore original launcher?"))},
        text={Text(t("Pilih semula launcher yang direkod sebelum penghantaran pertama pada TV ini.", "Select the launcher recorded before the first send to this TV."))},
        confirmButton={TextButton(onClick={restore=false;model.restoreHome()}){Text(t("Pulihkan", "Restore"))}},dismissButton={TextButton(onClick={restore=false}){Text(t("Batal", "Cancel"))}})
}

@Composable
private fun Toggle(label:String,value:Boolean,change:(Boolean)->Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {Text(label,Modifier.weight(1f));Switch(value,change)}
}
@Composable
private fun Adjustment(label:String,value:Float,range:ClosedFloatingPointRange<Float> = 0f..1f,change:(Float)->Unit) {
    Text(label,style=MaterialTheme.typography.labelLarge)
    Slider(value,onValueChange=change,valueRange=range)
}
@Composable
private fun BoxScope.PreviewApps(config:LauncherConfig) {
    val list=config.apps.take(if(config.vertical) 3 else 5)
    val slot: @Composable (String)->Unit = {pkg -> Surface(color=Color(0xDD132641),shape=RoundedCornerShape(5.dp)) {
        Text(pkg.substringAfterLast('.').take(12),Modifier.padding(5.dp),color=Color.White,style=MaterialTheme.typography.labelSmall,maxLines=1)
    }}
    if(config.vertical) Column(Modifier.align(Alignment.CenterEnd).padding(8.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {list.forEach {slot(it)}}
    else Row(Modifier.align(Alignment.BottomStart).padding(8.dp).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)) {list.forEach {slot(it)}}
}
