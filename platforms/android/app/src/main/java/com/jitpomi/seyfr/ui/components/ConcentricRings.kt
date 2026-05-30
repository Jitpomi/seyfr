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
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val strokeColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)

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
                // Rings ALWAYS expand outwards to give a continuous "vibrating wave" impression
                val currentPhase = i + phase
                
                val baseRadius = 40.dp.toPx()
                val spacing = 14.dp.toPx()
                val radius = baseRadius + (currentPhase * spacing)
                
                // Rings fade out as they get further from the center.
                // When actively transferring (isAnimating), they pulse brighter.
                val maxAlpha = if (isAnimating) 0.7f else 0.3f
                val alpha = kotlin.math.max(0f, maxAlpha - (currentPhase / 12f))
                
                drawCircle(
                    color = strokeColor.copy(alpha = alpha),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8.dp.toPx())
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
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
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
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
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
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    }
}
