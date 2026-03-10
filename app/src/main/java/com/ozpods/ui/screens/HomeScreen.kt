package com.ozpods.ui.screens
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ozpods.ui.components.DeviceCard
import com.ozpods.ui.viewmodel.AirPodsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AirPodsViewModel = hiltViewModel()) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    var permissionsGranted by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (permissionsGranted) viewModel.startScanning()
    }
    DisposableEffect(permissionsGranted) {
        if (permissionsGranted) viewModel.startScanning()
        onDispose { }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("OzPods") },
                scrollBehavior = scrollBehavior,
                actions = {
                    if (permissionsGranted) {
                        IconButton(onClick = { viewModel.toggleScanning() }) {
                            Icon(
                                imageVector = if (isScanning) Icons.Rounded.BluetoothSearching
                                    else Icons.Rounded.BluetoothDisabled,
                                contentDescription = if (isScanning) "Stop scanning" else "Start scanning",
                                tint = if (isScanning) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        AnimatedVisibility(visible = !permissionsGranted, enter = fadeIn(), exit = fadeOut()) {
            PermissionRequest(modifier = Modifier.padding(padding),
                onRequestPermissions = { permissionLauncher.launch(requiredPermissions()) })
        }
        AnimatedVisibility(visible = permissionsGranted && devices.isEmpty(),
            enter = fadeIn(), exit = fadeOut()) {
            EmptyState(isScanning = isScanning, modifier = Modifier.padding(padding))
        }
        AnimatedVisibility(visible = permissionsGranted && devices.isNotEmpty(),
            enter = fadeIn(), exit = fadeOut()) {
            var isRefreshing by remember { mutableStateOf(false) }
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refresh()
                    scope.launch {
                        delay(500)
                        isRefreshing = false
                    }
                },
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = devices, key = { it.address }) { device ->
                        DeviceCard(device = device, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}
@Composable
private fun PermissionRequest(modifier: Modifier = Modifier, onRequestPermissions: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Rounded.BluetoothSearching, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Text("Bluetooth Permissions Required",
                style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text("OzPods needs Bluetooth and location permissions to scan for nearby AirPods.",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRequestPermissions) { Text("Grant Permissions") }
        }
    }
}
@Composable
private fun EmptyState(isScanning: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Rounded.Headphones, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
            Text(
                text = if (isScanning) "Scanning for AirPods..." else "Scanning paused",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = if (isScanning) "Open your AirPods case nearby"
                    else "Tap the Bluetooth icon to resume",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
private fun requiredPermissions(): Array<String> {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return permissions.toTypedArray()
}
