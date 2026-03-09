package com.ozpods.ui.components
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.HearingDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ozpods.data.model.EarDetection
@Composable
fun EarDetectionRow(earDetection: EarDetection, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        EarIndicator("Left", earDetection.leftInEar)
        EarIndicator("Right", earDetection.rightInEar)
    }
}
@Composable
private fun EarIndicator(side: String, inEar: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = if (inEar) Icons.Rounded.Hearing else Icons.Rounded.HearingDisabled,
            contentDescription = "$side ear",
            tint = if (inEar) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp))
        Text(text = "$side: ${if (inEar) "In Ear" else "Out"}",
            style = MaterialTheme.typography.labelSmall)
    }
}
