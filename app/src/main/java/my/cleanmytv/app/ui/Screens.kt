package my.cleanmytv.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.cleanmytv.app.Candidate
import my.cleanmytv.app.Group
import my.cleanmytv.app.Lang
import my.cleanmytv.app.MainViewModel
import my.cleanmytv.app.Mode
import my.cleanmytv.app.RestoreRecord
import my.cleanmytv.app.S
import my.cleanmytv.app.Screen
import my.cleanmytv.app.Stage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppRoot(vm: MainViewModel) {
    val s = remember(vm.lang) { S(vm.lang) }
    BackHandler(enabled = vm.screen != Screen.CONNECT) { vm.onBack() }

    Column(
        Modifier
            .fillMaxSize()
            .background(Pal.Ink)
            .systemBarsPadding()
            .imePadding()
    ) {
        TopBar(vm.lang) { vm.changeLanguage(it) }
        when (vm.screen) {
            Screen.CONNECT -> ConnectScreen(vm, s)
            Screen.ANALYSING -> AnalysingScreen(vm, s)
            Screen.REVIEW -> ReviewScreen(vm, s)
            Screen.WORKING -> WorkingScreen(vm, s)
            Screen.DONE -> DoneScreen(vm, s)
            Screen.HISTORY -> HistoryScreen(vm, s)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Pal.Pandan,
    unfocusedBorderColor = Pal.Line,
    focusedLabelColor = Pal.Pandan,
    unfocusedLabelColor = Pal.Muted,
    cursorColor = Pal.Pandan,
    focusedTextColor = Pal.Text,
    unfocusedTextColor = Pal.Text
)

@Composable
private fun ErrorTile(text: String) {
    Tile {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Pal.Danger, modifier = Modifier.size(20.dp))
            Text(text, color = Pal.Danger, fontSize = 14.sp)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 1. Connect
// ---------------------------------------------------------------------------------------------

@Composable
private fun ConnectScreen(vm: MainViewModel, s: S) {
    var guideOpen by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Stepper(0, s)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(s.tagline, color = Pal.Songket, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                s.connectTitle,
                color = Pal.Text,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold
            )
            Text(s.connectIntro, color = Pal.Muted, fontSize = 14.sp, lineHeight = 20.sp)
        }

        Tile {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = vm.host,
                    onValueChange = { v -> vm.host = v.filter { !it.isWhitespace() } },
                    label = { Text(s.ipLabel) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = vm.port,
                    onValueChange = { v -> vm.port = v.filter { it.isDigit() }.take(5) },
                    label = { Text(s.portLabel) },
                    singleLine = true,
                    modifier = Modifier.width(96.dp),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors()
                )
            }

            GhostButton(
                text = if (vm.scanning) s.scanning else s.scanBtn,
                enabled = !vm.scanning,
                modifier = Modifier.fillMaxWidth()
            ) { vm.scanNetwork() }

            if (vm.scanning) {
                LinearProgressIndicator(
                    color = Pal.Pandan,
                    trackColor = Pal.Line,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (vm.found.isNotEmpty()) {
                Text(s.foundTvs, color = Pal.Muted, fontSize = 12.sp)
                vm.found.take(8).forEach { ip ->
                    val shape = RoundedCornerShape(12.dp)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (ip == vm.host) Pal.PanelHi else Pal.Ink)
                            .border(1.dp, if (ip == vm.host) Pal.Pandan else Pal.Line, shape)
                            .clickable { vm.host = ip }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ip, color = Pal.Text, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                    }
                }
            } else if (vm.scanDone && !vm.scanning) {
                Text(s.scanNone, color = Pal.Muted, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }

        vm.error?.let { ErrorTile(it(s)) }

        PrimaryButton(s.connectBtn) { vm.connectAndAnalyse() }

        Tile {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { guideOpen = !guideOpen },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Pal.Songket, modifier = Modifier.size(20.dp))
                Text(
                    s.setupTitle,
                    color = Pal.Text,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (guideOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Pal.Muted
                )
            }
            if (guideOpen) {
                s.setupSteps.forEachIndexed { i, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${i + 1}", color = Pal.Songket, fontWeight = FontWeight.Bold)
                        Text(step, color = Pal.Muted, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        }

        GhostButton(s.historyBtn, Modifier.fillMaxWidth()) { vm.openHistory() }

        Text(s.legal, color = Pal.Muted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

// ---------------------------------------------------------------------------------------------
// 2. Analysing
// ---------------------------------------------------------------------------------------------

@Composable
private fun AnalysingScreen(vm: MainViewModel, s: S) {
    Column(Modifier.fillMaxSize()) {
        Stepper(1, s)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Pal.Pandan, trackColor = Pal.Line)
            Spacer(Modifier.height(20.dp))
            Text(
                when (vm.stage) {
                    Stage.CONNECT -> s.stageConnect
                    Stage.INFO -> s.stageInfo
                    Stage.PACKAGES -> s.stagePackages
                    Stage.HOME -> s.stageHome
                    Stage.SORT -> s.stageSort
                },
                color = Pal.Text,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(24.dp))
            GhostButton(s.cancel) { vm.goHome() }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 3. Review
// ---------------------------------------------------------------------------------------------

@Composable
private fun Ring(value: Int, max: Int) {
    val frac = if (max <= 0) 0f else (value.toFloat() / max).coerceIn(0f, 1f)
    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = 9.dp.toPx()
            val inset = sw / 2f
            val arc = Size(size.width - sw, size.height - sw)
            drawArc(
                color = Pal.Line,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arc,
                style = Stroke(width = sw)
            )
            drawArc(
                color = Pal.Pandan,
                startAngle = -90f,
                sweepAngle = 360f * frac,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arc,
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )
        }
        Text("$value", color = Pal.Text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
private fun CandidateRow(c: Candidate, checked: Boolean, s: S, onToggle: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val lang = s.lang
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (checked) Pal.PanelHi else Pal.Panel)
            .border(1.dp, if (checked) Pal.Pandan.copy(alpha = 0.5f) else Pal.Line, shape)
            .clickable { onToggle(!checked) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = Pal.Pandan,
                uncheckedColor = Pal.Muted,
                checkmarkColor = Pal.Ink
            )
        )
        Column(
            Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            c.label?.let {
                Text(it.of(lang), color = Pal.Text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Text(c.pkg, color = Pal.Muted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            c.reason?.let { Text(it.of(lang), color = Pal.Muted, fontSize = 12.sp, lineHeight = 17.sp) }
            c.warn?.let {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Pal.Songket,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(14.dp)
                    )
                    Text(
                        "${s.warnPrefix}: ${it.of(lang)}",
                        color = Pal.Songket,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewScreen(vm: MainViewModel, s: S) {
    val a = vm.analysis ?: return
    val lang = s.lang
    val q = vm.query.trim().lowercase()
    val filtered = remember(a, q, lang) {
        a.candidates.filter { c ->
            q.isEmpty() ||
                c.pkg.lowercase().contains(q) ||
                (c.label?.of(lang)?.lowercase()?.contains(q) == true)
        }
    }
    val byGroup = filtered.groupBy { it.group }
    val selectedCount = vm.selectedList().size
    var confirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Stepper(2, s)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { vm.goHome() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Pal.Text)
                        }
                        Text(s.reviewTitle, color = Pal.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Tile(highlight = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Ring(selectedCount, a.candidates.size)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            val name = listOf(a.info.brand, a.info.model).filter { it.isNotBlank() }.joinToString(" ")
                            Text(
                                name.ifBlank { s.lblUnknown },
                                color = Pal.Text,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                "${s.lblAndroid} ${a.info.release.ifBlank { "?" }} (API ${a.info.sdk})",
                                color = Pal.Muted,
                                fontSize = 13.sp
                            )
                            Text(
                                "${s.lblHome}: ${a.info.homePkg ?: s.lblUnknown}",
                                color = Pal.Muted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(s.selectedCount(selectedCount), color = Pal.Pandan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(s.protectedNote, color = Pal.Muted, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }

            vm.notice?.let { n ->
                item {
                    Tile {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Pal.Pandan, modifier = Modifier.size(20.dp))
                            Text(n(s), color = Pal.Text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = { vm.notice = null }) { Text(s.close, color = Pal.Muted) }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GhostButton(s.importBtn, Modifier.fillMaxWidth()) { vm.importOpen = true }
                    OutlinedTextField(
                        value = vm.query,
                        onValueChange = { vm.query = it },
                        label = { Text(s.searchHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )
                }
            }

            if (a.candidates.isEmpty()) {
                item { Text(s.nothingFound, color = Pal.Muted, fontSize = 14.sp) }
            }

            for (group in listOf(Group.RECOMMENDED, Group.REVIEW, Group.OTHER)) {
                val list = byGroup[group].orEmpty()
                if (list.isEmpty()) continue
                val title = when (group) {
                    Group.RECOMMENDED -> s.groupRec
                    Group.REVIEW -> s.groupRev
                    Group.OTHER -> s.groupOth
                }
                val desc = when (group) {
                    Group.RECOMMENDED -> s.groupRecDesc
                    Group.REVIEW -> s.groupRevDesc
                    Group.OTHER -> s.groupOthDesc
                }
                val allOn = list.all { vm.selected[it.pkg] == true }
                val open = group != Group.OTHER || vm.expandOther || q.isNotEmpty()

                item(key = "h-$group") {
                    Row(
                        Modifier.padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("$title · ${list.size}", color = Pal.Text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(desc, color = Pal.Muted, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                        if (group == Group.OTHER) {
                            TextButton(onClick = { vm.expandOther = !vm.expandOther }) {
                                Text(if (vm.expandOther) s.hideAll else s.showAll, color = Pal.Pandan)
                            }
                        } else {
                            TextButton(onClick = { vm.setGroup(group, !allOn) }) {
                                Text(if (allOn) s.clearGroup else s.selectGroup, color = Pal.Pandan)
                            }
                        }
                    }
                }
                if (open) {
                    items(list, key = { it.pkg }) { c ->
                        CandidateRow(c, vm.selected[c.pkg] == true, s) { vm.selected[c.pkg] = it }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(Pal.Panel)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            PrimaryButton(s.cleanBtn(selectedCount), enabled = selectedCount > 0) { confirm = true }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            containerColor = Pal.Panel,
            titleContentColor = Pal.Text,
            textContentColor = Pal.Muted,
            title = { Text(s.confirmTitle) },
            text = { Text(s.confirmBody(selectedCount)) },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    vm.startClean()
                }) { Text(s.confirmGo, color = Pal.Pandan, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(s.cancel, color = Pal.Muted) }
            }
        )
    }

    if (vm.importOpen) {
        AlertDialog(
            onDismissRequest = { vm.importOpen = false },
            containerColor = Pal.Panel,
            titleContentColor = Pal.Text,
            textContentColor = Pal.Muted,
            title = { Text(s.importTitle) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(s.importHint, fontSize = 13.sp, lineHeight = 18.sp)
                    OutlinedTextField(
                        value = vm.importUrl,
                        onValueChange = { vm.importUrl = it },
                        label = { Text(s.importUrl) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = fieldColors()
                    )
                    GhostButton(
                        s.importFetch,
                        Modifier.fillMaxWidth(),
                        enabled = !vm.importBusy && vm.importUrl.isNotBlank()
                    ) { vm.fetchImport() }
                    if (vm.importBusy) {
                        LinearProgressIndicator(
                            color = Pal.Pandan,
                            trackColor = Pal.Line,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    vm.importMsg?.let { Text(it(s), color = Pal.Danger, fontSize = 13.sp) }
                    OutlinedTextField(
                        value = vm.importText,
                        onValueChange = { vm.importText = it },
                        label = { Text(s.importPaste) },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        colors = fieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.applyImport() },
                    enabled = vm.importText.isNotBlank()
                ) { Text(s.importApply, color = Pal.Pandan, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { vm.importOpen = false }) { Text(s.cancel, color = Pal.Muted) }
            }
        )
    }
}

// ---------------------------------------------------------------------------------------------
// 4. Working (cleaning / restoring)
// ---------------------------------------------------------------------------------------------

@Composable
private fun WorkingScreen(vm: MainViewModel, s: S) {
    val frac = if (vm.total <= 0) 0f else (vm.doneCount.toFloat() / vm.total).coerceIn(0f, 1f)
    Column(Modifier.fillMaxSize()) {
        Stepper(2, s)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                when (vm.mode) {
                    Mode.CLEAN -> s.cleaningTitle
                    Mode.RESTORE -> s.restoringTitle
                    Mode.REAPPLY -> s.reapplyingTitle
                },
                color = Pal.Text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { frac },
                color = Pal.Pandan,
                trackColor = Pal.Line,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(12.dp))
            Text(s.progress(vm.doneCount, vm.total), color = Pal.Songket, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text(vm.currentPkg, color = Pal.Muted, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Spacer(Modifier.height(28.dp))
            GhostButton(s.stop) { vm.stopWork() }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 5. Done
// ---------------------------------------------------------------------------------------------

@Composable
private fun DoneScreen(vm: MainViewModel, s: S) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stepper(3, s)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Pal.Pandan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Pal.Pandan,
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                when (vm.mode) {
                    Mode.CLEAN -> s.doneCleanTitle
                    Mode.RESTORE -> s.doneRestoreTitle
                    Mode.REAPPLY -> s.doneReapplyTitle
                },
                color = Pal.Text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(s.doneSummary(vm.okCount, vm.failed.size), color = Pal.Songket, fontWeight = FontWeight.SemiBold)
        }

        Tile {
            if (vm.mode != Mode.RESTORE) {
                Text(s.rebootTip, color = Pal.Text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Text(s.doneHint, color = Pal.Muted, fontSize = 13.sp, lineHeight = 19.sp)
        }

        if (vm.failed.isNotEmpty()) {
            Tile {
                Text(s.failedList, color = Pal.Danger, fontWeight = FontWeight.Bold)
                vm.failed.take(12).forEach { r ->
                    Column {
                        Text(r.pkg, color = Pal.Text, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        if (r.msg.isNotBlank()) Text(r.msg, color = Pal.Muted, fontSize = 11.sp)
                    }
                }
            }
        }

        PrimaryButton(s.home) { vm.goHome() }
        GhostButton(s.historyBtn, Modifier.fillMaxWidth()) { vm.openHistory() }
    }
}

// ---------------------------------------------------------------------------------------------
// 6. History / restore
// ---------------------------------------------------------------------------------------------

@Composable
private fun HistoryScreen(vm: MainViewModel, s: S) {
    val fmt = remember(s.lang) { SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag(s.lang.code)) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.goHome() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Pal.Text)
                }
                Text(s.historyTitle, color = Pal.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        vm.error?.let { e -> item { ErrorTile(e(s)) } }

        if (vm.records.isEmpty()) {
            item { Text(s.historyEmpty, color = Pal.Muted, fontSize = 14.sp, lineHeight = 20.sp) }
        } else {
            items(vm.records.toList(), key = { it.id }) { rec -> RecordTile(rec, fmt.format(Date(rec.time)), s, vm) }
            item { Text(s.reapplyHint, color = Pal.Muted, fontSize = 12.sp, lineHeight = 17.sp) }
        }
    }
}

@Composable
private fun RecordTile(rec: RestoreRecord, date: String, s: S, vm: MainViewModel) {
    Tile {
        val name = listOf(rec.brand, rec.model).filter { it.isNotBlank() }.joinToString(" ")
        Text(name.ifBlank { s.lblUnknown }, color = Pal.Text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text("$date · ${rec.host}", color = Pal.Muted, fontSize = 12.sp)
        Text(s.recordLine(rec.packages.size), color = Pal.Songket, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        PrimaryButton(s.restoreThis) { vm.runRecord(rec, reapply = false) }
        GhostButton(s.reapply, Modifier.fillMaxWidth()) { vm.runRecord(rec, reapply = true) }
    }
}
