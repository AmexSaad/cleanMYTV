@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package tv.cleanyour.feature.launcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.cleanyour.core.flows.Busy
import tv.cleanyour.core.flows.HomeScreenIntent
import tv.cleanyour.core.flows.HomeScreenState
import tv.cleanyour.ui.design.component.*
import tv.cleanyour.ui.design.theme.Glow

/** Dashboard entry; installing remains an explicit, guarded action. */
@Composable
fun PrayerScreen(state: HomeScreenState, onIntent: (HomeScreenIntent) -> Unit, onBack: () -> Unit) {
    ScreenScaffold(Glow.Tv, topBar = { CytTopBar(onBack = onBack) }) {
        ScreenTitle(stringResource(R.string.prayer_title), Modifier.padding(top = 20.dp))
        Body(stringResource(R.string.home_prayer_note), Modifier.padding(top = 16.dp))
        Body(stringResource(R.string.prayer_poster_note), Modifier.padding(top = 12.dp))
        val busy = state.busy
        if (busy != null) {
            LiveLine(stringResource(R.string.home_applying))
            if (busy is Busy.Working) ProgressBar(busy.percent / 100f, Modifier.padding(top = 16.dp))
        } else if (state.prayerAvailable) {
            if (!state.prayerInstalled || state.prayerUpdateAvailable) {
                PrimaryButton(stringResource(if (state.prayerUpdateAvailable) R.string.home_update_prayer else R.string.home_add_prayer),
                    { onIntent(HomeScreenIntent.AddPrayer) }, Modifier.padding(top = 24.dp))
            } else Body(stringResource(R.string.prayer_installed), Modifier.padding(top = 24.dp))
            if (state.prayerInstalled) TertiaryButton(stringResource(R.string.home_remove_prayer),
                { onIntent(HomeScreenIntent.RemovePrayer) }, Modifier.padding(top = 12.dp), guardObscured = true)
        }
    }
}
