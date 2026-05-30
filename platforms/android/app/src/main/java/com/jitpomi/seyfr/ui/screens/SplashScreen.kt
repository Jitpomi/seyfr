package com.jitpomi.seyfr.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(onSplashComplete: () -> Unit) {
    val drawProgress = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        // Wait a tiny bit for Compose to settle
        delay(150)
        
        // 1. Calligraphic drawing of the logo
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )

        // 2. Elegantly reveal the text
        textOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )

        // Let the user admire the calligraphy briefly
        delay(1000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            
            // The Calligraphic Logo Canvas
            val inkColor = Color.Black
            
            Canvas(modifier = Modifier.height(100.dp)) {
                val center = this.center

                // Centered "S" Path
                val sPath = Path().apply {
                    moveTo(32.5f, -35f)
                    cubicTo(27.5f, -45f, 17.5f, -50f, 2.5f, -50f)
                    cubicTo(-17.5f, -50f, -27.5f, -40f, -27.5f, -25f)
                    cubicTo(-27.5f, -10f, -7.5f, -5f, 2.5f, 0f)
                    cubicTo(22.5f, 10f, 27.5f, 25f, 12.5f, 40f)
                    cubicTo(-2.5f, 50f, -22.5f, 45f, -32.5f, 30f)
                }
                
                // Outer Circle Path (Increased radius for more padding)
                val circlePath = Path().apply {
                    addOval(Rect(left = -110f, top = -110f, right = 110f, bottom = 110f))
                }

                // Path Measures for calculating drawing progress
                val circleMeasure = PathMeasure().apply { setPath(circlePath, false) }
                val sMeasure = PathMeasure().apply { setPath(sPath, false) }

                val circleAnimPath = Path()
                circleMeasure.getSegment(0f, drawProgress.value * circleMeasure.length, circleAnimPath, true)

                val sAnimPath = Path()
                // Stagger the 'S' so it draws slightly after the circle starts
                val sProgress = ((drawProgress.value - 0.2f) / 0.8f).coerceIn(0f, 1f)
                sMeasure.getSegment(0f, sProgress * sMeasure.length, sAnimPath, true)

                withTransform({
                    translate(center.x, center.y)
                    scale(1.2f, 1.2f) // Scale the path vectors up slightly
                }) {
                    drawPath(
                        path = circleAnimPath,
                        color = inkColor,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    
                    drawPath(
                        path = sAnimPath,
                        color = inkColor,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // The Text Reveal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffset.value.dp)
            ) {
                Text(
                    text = "SEYFR",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Send Your Files Right",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
