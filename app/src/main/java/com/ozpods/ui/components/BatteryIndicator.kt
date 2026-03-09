package com.ozpods.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery0Bar
import androidx.compose.material.icons.rounded.Battery2Bar
import androidx.compose.material.icons.rounded.Battery4Bar
import androidx.compose.material.icons.rounded.Battery6Bar
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.BatteryUnknown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ozpods.data.model.BatteryInfo
@Composable
fun BatteryIndicator(label: String, level: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = batteryIcon(level), contentDescription = "$label battery",
            tint = batteryColor(level), modifier = Modifier.size(28.dp))
        Text(text = if (level == BatteryInfo.UNAVAILABLE) "--" else "$level%",
            style = MaterialTheme.typography.labelMedium)
        Text(text = label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
fun BatteryRow(battery: BatteryInfo, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically) {
        BatteryIndicator(label = "Left", level = battery.left)
        BatteryIndicator(label = "Right", level = battery.right)
        BatteryIndicator(label = "Case", level = battery.case)
    }
}
private fun batteryIcon(level: Int): ImageVector = when {
    level == BatteryInfo.UNAVAILABLE -> Icons.Rounded.BatteryUnknown
    level <= 10 -> Icons.Rounded.Battery0Bar
    level <= 30 -> Icons.Rounded.Battery2Bar
    level <= 60 -> Icons.Rounded.Battery4Bar
    level <= 80 -> Icons.Rounded.Battery6Bar
    else -> Icons.Rounded.BatteryFull
}
private fun batteryColor(level: Int): Color = when {
    level == BatteryInfo.UNAVAILABLE -> Color.Gray
    level <= 10 -> Color(0xFFEA4335)
    level <= 20 -> Color(0xFFFBBC04)
    else -> Color(0xFF34A853)
}
