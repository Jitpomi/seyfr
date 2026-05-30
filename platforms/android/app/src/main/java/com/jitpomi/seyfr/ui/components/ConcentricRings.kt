package com.jitpomi.seyfr.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ConcentricRings(
    isAnimating: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isAnimating) 0.1f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val strokeColor = MaterialTheme.colorScheme.outlineVariant

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
                val baseRadius = 40.dp.toPx() + (i * 11.dp.toPx())
                val radius = if (isAnimating) baseRadius * scale else baseRadius
                val alpha = if (isAnimating) alphaAnim else 0.4f
                
                drawCircle(
                    color = strokeColor.copy(alpha = alpha),
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
fun FileRings(isAnimating: Boolean = false, modifier: Modifier = Modifier) {
    ConcentricRings(isAnimating = isAnimating, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.height(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FolderRings(isAnimating: Boolean = false, modifier: Modifier = Modifier) {
    ConcentricRings(isAnimating = isAnimating, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            modifier = Modifier.height(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun QRRings(isAnimating: Boolean = false, modifier: Modifier = Modifier) {
    ConcentricRings(isAnimating = isAnimating, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.QrCodeScanner,
            contentDescription = null,
            modifier = Modifier.height(28.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
