package com.ozpods.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ozpods.data.model.AirPodsDevice
import com.ozpods.data.model.BatteryInfo
import com.ozpods.ui.components.EarDetectionRow
import com.ozpods.ui.viewmodel.AirPodsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    address: String,
    onBack: () -> Unit,
    viewModel: AirPodsViewModel = hiltViewModel()
) {
    val device by viewModel.getDevice(address).collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.model?.displayName ?: "Device") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (device == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Device not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            DeviceDetailContent(
                device = device!!,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun DeviceDetailContent(device: AirPodsDevice, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 设备大图标 + 型号
        DeviceHeader(device)

        // 信号强度
        SignalCard(device.rssi)

        // 电池详情
        if (device.battery.isAnyAvailable) {
            BatteryDetailCard(device.battery)
        }

        // 耳戴检测
        if (device.earDetection.eitherInEar) {
            SectionCard(title = "Ear Detection") {
                EarDetectionRow(
                    earDetection = device.earDetection,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 设备信息
        DeviceInfoCard(device)

        // 最后发现
        LastSeenCard(device.lastSeen)
    }
}

@Composable
private fun DeviceHeader(device: AirPodsDevice) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (device.model.hasAnc) Icons.Rounded.HeadsetMic
            else Icons.Rounded.Headphones,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = device.model.displayName,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun SignalCard(rssi: Int) {
    val (strength, description) = when {
        rssi > -50 -> "Strong" to "Excellent signal, device is very close"
        rssi > -70 -> "Good" to "Good signal, device is nearby"
        rssi > -90 -> "Weak" to "Weak signal, device may be far away"
        else -> "Far" to "Very weak signal"
    }
    SectionCard(title = "Signal Strength", icon = Icons.Rounded.SignalCellularAlt) {
        Text(text = "$strength ($rssi dBm)", style = MaterialTheme.typography.titleMedium)
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BatteryDetailCard(battery: BatteryInfo) {
    SectionCard(title = "Battery") {
        BatteryBar("Left", battery.left)
        BatteryBar("Right", battery.right)
        BatteryBar("Case", battery.case)
    }
}

@Composable
private fun BatteryBar(label: String, level: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.size(48.dp, 20.dp)
        )
        if (level == BatteryInfo.UNAVAILABLE) {
            Text(
                text = "--",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LinearProgressIndicator(
                progress = { level / 100f },
                modifier = Modifier.weight(1f),
                color = when {
                    level <= 10 -> MaterialTheme.colorScheme.error
                    level <= 20 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Text(
                text = "$level%",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun DeviceInfoCard(device: AirPodsDevice) {
    SectionCard(title = "Device Info", icon = Icons.Rounded.Info) {
        InfoRow("MAC Address", device.address)
        InfoRow("Paired", if (device.isPaired) "Yes" else "No")
        InfoRow("Lid", if (device.isLidOpen) "Open" else "Closed")
        if (device.model.hasAnc) InfoRow("ANC", "Supported")
        InfoRow("Color ID", device.color.toString())
    }
}

@Composable
private fun LastSeenCard(lastSeen: Long) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }
    tick // 触发重组
    val elapsed = (System.currentTimeMillis() - lastSeen) / 1000
    val text = when {
        elapsed < 5 -> "Just now"
        elapsed < 60 -> "${elapsed}s ago"
        else -> "${elapsed / 60}m ago"
    }
    SectionCard(title = "Last Seen", icon = Icons.Rounded.Bluetooth) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = title, style = MaterialTheme.typography.titleSmall)
            }
            content()
        }
    }
}
