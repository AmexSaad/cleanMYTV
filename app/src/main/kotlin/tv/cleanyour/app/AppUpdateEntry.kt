package tv.cleanyour.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.cleanyour.core.engine.RunState
import tv.cleanyour.ui.design.theme.CytTheme

@Composable
internal fun AppUpdateEntry(graph: AppGraph) {
    if (!graph.store.rules.updateCheck) return
    var show by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text("v${graph.appVersion}", Modifier.weight(1f), color = CytTheme.colors.textSecondary,
            style = CytTheme.type.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        TextButton(onClick = { show = true }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.clean_update_button))
        }
    }
    if (show) AppUpdateDialog(graph, onDismiss = { show = false })
}

@Composable
private fun AppUpdateDialog(graph: AppGraph, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf<AppUpdateStatus>(AppUpdateStatus.Checking) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt) { status = AppUpdateStatus.Checking; status = graph.store.releaseStatus(graph.http, graph.appVersion) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.clean_update_title)) },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.clean_update_current) + " " + graph.appVersion)
                when (val value = status) {
                    AppUpdateStatus.Checking -> { CircularProgressIndicator(Modifier.size(24.dp)); Text(stringResource(R.string.clean_update_checking)) }
                    AppUpdateStatus.Latest -> Text(stringResource(R.string.clean_update_latest))
                    AppUpdateStatus.Failed -> Text(stringResource(R.string.clean_update_failed))
                    AppUpdateStatus.TvBusy -> Text(stringResource(R.string.clean_update_tv_busy))
                    is AppUpdateStatus.Available -> {
                        Text(stringResource(R.string.clean_update_available) + " " + value.release.version, style = CytTheme.type.titleMedium)
                        Text(stringResource(R.string.clean_update_notes), style = CytTheme.type.labelLarge)
                        Text(value.release.notes.ifBlank { stringResource(R.string.clean_update_no_notes) })
                        Text(stringResource(R.string.clean_update_install_hint), style = CytTheme.type.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            when (val value = status) {
                is AppUpdateStatus.Available -> Button(onClick = {
                    // Do not invite replacement of the phone app while a TV run is unfinished.
                    val safe = graph.tvs.tvs.value.all { tv ->
                        val state = graph.engine.runState(tv.id).value
                        state == RunState.Idle || state is RunState.Finished
                    }
                    if (!safe) status = AppUpdateStatus.TvBusy
                    else try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value.release.apkUrl))) }
                    catch (_: ActivityNotFoundException) { status = AppUpdateStatus.Failed }
                }) { Text(stringResource(R.string.clean_update_download)) }
                AppUpdateStatus.Failed, AppUpdateStatus.TvBusy -> TextButton(onClick = { attempt++ }) { Text(stringResource(R.string.clean_update_retry)) }
                AppUpdateStatus.Checking, AppUpdateStatus.Latest -> Unit
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.clean_update_later)) } },
    )
}
