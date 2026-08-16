package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.OrangePrimaryDark
import com.example.ui.theme.OrangeSecondary

/**
 * 3D Background Canvas rendering animated floating gradient 3D orbs and subtle light beams.
 * Creates depth and ambience behind glassmorphic UI components.
 */
@Composable
fun Background3DCanvas(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8F2),
                        Color(0xFFFAFAFA),
                        Color(0xFFF5F5F7)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-right 3D Orange Sphere Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangePrimary.copy(alpha = 0.28f),
                        OrangeSecondary.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(width * 0.85f, height * 0.12f)
            )

            // Center-left Warm Amber 3D Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFB74D).copy(alpha = 0.22f),
                        Color(0xFFFFE0B2).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.08f, height * 0.45f),
                    radius = width * 0.60f
                ),
                radius = width * 0.60f,
                center = Offset(width * 0.08f, height * 0.45f)
            )

            // Bottom-right Deep Orange Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrangePrimaryDark.copy(alpha = 0.18f),
                        OrangePrimary.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.90f, height * 0.85f),
                    radius = width * 0.50f
                ),
                radius = width * 0.50f,
                center = Offset(width * 0.90f, height * 0.85f)
            )
        }

        content()
    }
}

/**
 * Reusable Glassmorphic Card featuring semi-transparent frosted white background,
 * glossy 3D gradient border, and elevation shadow.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 6.dp,
    borderWidth: Dp = 1.5.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.95f),
            OrangePrimary.copy(alpha = 0.40f),
            Color.White.copy(alpha = 0.60f),
            OrangeSecondary.copy(alpha = 0.30f)
        )
    )

    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = OrangePrimary.copy(alpha = 0.25f),
            spotColor = OrangePrimaryDark.copy(alpha = 0.30f)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.92f),
                    Color(0xFFFFF9F5).copy(alpha = 0.82f)
                )
            )
        )
        .border(
            width = borderWidth,
            brush = borderBrush,
            shape = shape
        )

    if (onClick != null) {
        Box(
            modifier = cardModifier.clickable { onClick() },
            content = content
        )
    } else {
        Box(
            modifier = cardModifier,
            content = content
        )
    }
}

/**
 * Glassmorphic 3D Button with glossy gradient highlights, crisp borders, and soft shadows.
 */
@Composable
fun Glassmorphic3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            OrangePrimary,
            OrangePrimaryDark
        )
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.70f),
            Color.White.copy(alpha = 0.20f)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = OrangePrimary.copy(alpha = 0.4f),
                spotColor = OrangePrimaryDark.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(brush = gradientBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 20.dp),
        content = content
    )
}
