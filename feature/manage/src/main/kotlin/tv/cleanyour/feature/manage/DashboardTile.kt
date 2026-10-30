package tv.cleanyour.feature.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import tv.cleanyour.ui.design.theme.CytTheme

/** Navigation only. Mutating TV actions remain behind their original review and guards. */
@Composable
internal fun DashboardTile(label: String, icon: ImageVector, modifier: Modifier = Modifier, subtitle: String? = null, onClick: () -> Unit) {
    val colors = CytTheme.colors
    val shape = CytTheme.shapes.medium
    Column(
        modifier.fillMaxWidth().heightIn(min = 104.dp).clip(shape)
            .background(colors.surface1).border(1.dp, colors.outlineStrong, shape)
            .clickable(role = Role.Button, onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = colors.primary)
        Text(label, color = colors.textPrimary, style = CytTheme.type.labelLarge)
        if (!subtitle.isNullOrBlank()) Text(subtitle, color = colors.textSecondary, style = CytTheme.type.bodySmall)
    }
}
