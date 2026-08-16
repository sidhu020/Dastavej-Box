package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class ToastNotificationType {
    SUCCESS,
    DESTRUCTIVE,
    WARNING,
    INFO
}

/**
 * Modern Global Top Toast Banner.
 * Positioned safely beneath the Android status bar and completely isolated from
 * bottom navigation bars, gesture handles, or recent buttons.
 */
@Composable
fun TopToastBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Long = 3200L
) {
    var visibleMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            visibleMessage = message
            delay(durationMillis)
            visibleMessage = null
            onDismiss()
        } else {
            visibleMessage = null
        }
    }

    val activeMsg = visibleMessage
    val isVisible = !activeMsg.isNullOrBlank()

    val toastType = remember(activeMsg) {
        val lower = activeMsg?.lowercase() ?: ""
        when {
            lower.contains("deleted") || lower.contains("removed") || lower.contains("trashed") ||
                    lower.contains("failed") || lower.contains("error") || lower.contains("incorrect") ||
                    lower.contains("invalid") || lower.contains("wrong") -> ToastNotificationType.DESTRUCTIVE

            lower.contains("saved") || lower.contains("added") || lower.contains("restored") ||
                    lower.contains("success") || lower.contains("imported") || lower.contains("updated") ||
                    lower.contains("created") || lower.contains("moved") || lower.contains("decrypted") ||
                    lower.contains("copied") || lower.contains("unlocked") || lower.contains("verified") -> ToastNotificationType.SUCCESS

            lower.contains("warning") || lower.contains("caution") || lower.contains("expiring") ||
                    lower.contains("attention") -> ToastNotificationType.WARNING

            else -> ToastNotificationType.INFO
        }
    }

    val (bgColor, borderColor, iconColor, textColor, iconVector) = when (toastType) {
        ToastNotificationType.SUCCESS -> ToastStyle(
            bgColor = Color(0xFF14532D), // Rich forest emerald
            borderColor = Color(0xFF22C55E).copy(alpha = 0.5f),
            iconColor = Color(0xFF4ADE80),
            textColor = Color(0xFFF0FDF4),
            icon = Icons.Default.CheckCircle
        )
        ToastNotificationType.DESTRUCTIVE -> ToastStyle(
            bgColor = Color(0xFF7F1D1D), // Rich ruby crimson
            borderColor = Color(0xFFEF4444).copy(alpha = 0.5f),
            iconColor = Color(0xFFF87171),
            textColor = Color(0xFFFEF2F2),
            icon = if (activeMsg?.contains("deleted", ignoreCase = true) == true || activeMsg?.contains("trashed", ignoreCase = true) == true)
                Icons.Default.DeleteOutline else Icons.Default.ErrorOutline
        )
        ToastNotificationType.WARNING -> ToastStyle(
            bgColor = Color(0xFF78350F), // Warm amber gold
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.5f),
            iconColor = Color(0xFFFBBF24),
            textColor = Color(0xFFFFFBEB),
            icon = Icons.Default.WarningAmber
        )
        ToastNotificationType.INFO -> ToastStyle(
            bgColor = Color(0xFF1E293B), // Deep slate navy
            borderColor = Color(0xFF38BDF8).copy(alpha = 0.5f),
            iconColor = Color(0xFF38BDF8),
            textColor = Color(0xFFF8FAFC),
            icon = if (activeMsg?.contains("locked", ignoreCase = true) == true) Icons.Default.Lock else Icons.Default.Info
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 10.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .border(width = 1.2.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
                    .clickable {
                        visibleMessage = null
                        onDismiss()
                    }
                    .testTag("top_toast_banner"),
                color = bgColor,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = activeMsg ?: "",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            visibleMessage = null
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Alert",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class ToastStyle(
    val bgColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val textColor: Color,
    val icon: ImageVector
)
