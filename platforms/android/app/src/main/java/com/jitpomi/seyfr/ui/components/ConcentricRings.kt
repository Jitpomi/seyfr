package com.jitpomi.seyfr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ConcentricRings(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val ringColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val iconColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            for (i in 0..7) {
                val radius = 40.dp.toPx() + (i * 11.dp.toPx())
                drawCircle(
                    color = ringColor,
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5.dp.toPx())
                )
            }
        }

        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun FileRings(modifier: Modifier = Modifier) {
    ConcentricRings(modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.height(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FolderRings(modifier: Modifier = Modifier) {
    ConcentricRings(modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            modifier = Modifier.height(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun QRRings(modifier: Modifier = Modifier) {
    ConcentricRings(modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.height(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
