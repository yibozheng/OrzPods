package com.ozpods.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ozpods.data.model.AirPodsDevice
import kotlinx.coroutines.delay
@Composable
fun DeviceCard(device: AirPodsDevice, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = if (device.model.hasAnc) Icons.Rounded.HeadsetMic
                        else Icons.Rounded.Headphones,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = device.model.displayName,
                        style = MaterialTheme.typography.titleMedium)
                    Text(text = buildStatusText(device),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    SignalStrengthBadge(rssi = device.rssi)
                    LastSeenText(lastSeen = device.lastSeen)
                }
            }
            if (device.battery.isAnyAvailable) {
                BatteryRow(battery = device.battery,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
            }
            if (device.earDetection.eitherInEar) {
                EarDetectionRow(earDetection = device.earDetection,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
@Composable
private fun SignalStrengthBadge(rssi: Int) {
    val strength = when {
        rssi > -50 -> "Strong"; rssi > -70 -> "Good"
        rssi > -90 -> "Weak"; else -> "Far"
    }
    Text(text = "$strength ($rssi dBm)", style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}
@Composable
private fun LastSeenText(lastSeen: Long) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }
    // 读取 tick 触发重组
    tick
    val elapsed = (System.currentTimeMillis() - lastSeen) / 1000
    val text = when {
        elapsed < 5 -> "Just now"
        elapsed < 60 -> "${elapsed}s ago"
        else -> "${elapsed / 60}m ago"
    }
    Text(text = text, style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}
private fun buildStatusText(device: AirPodsDevice): String {
    val parts = mutableListOf<String>()
    if (device.isPaired) parts.add("Paired")
    if (device.isLidOpen) parts.add("Lid Open")
    if (device.model.hasAnc) parts.add("ANC")
    return parts.joinToString(" \u2022 ")
}
