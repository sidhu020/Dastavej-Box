package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusExpiredBg
import com.example.ui.theme.StatusExpiredRed
import com.example.ui.theme.StatusExpiringBg
import com.example.ui.theme.StatusExpiringAmber
import com.example.ui.theme.StatusValidBg
import com.example.ui.theme.StatusValidGreen

enum class ExpiryStatus {
    EXPIRED,
    EXPIRES_TODAY,
    EXPIRING_SOON,
    VALID,
    NO_EXPIRY
}

@Composable
fun ExpiryBadge(
    expiryDateMillis: Long?,
    modifier: Modifier = Modifier
) {
    if (expiryDateMillis == null) {
        return
    }

    val now = System.currentTimeMillis()
    val millisDiff = expiryDateMillis - now
    val daysRemaining = millisDiff / (1000 * 60 * 60 * 24)

    val (status, text, bgColor, textColor, icon) = when {
        daysRemaining < 0 -> Tuple5(
            ExpiryStatus.EXPIRED,
            "Expired",
            StatusExpiredBg,
            StatusExpiredRed,
            Icons.Default.Error
        )
        daysRemaining == 0L -> Tuple5(
            ExpiryStatus.EXPIRES_TODAY,
            "Expires Today",
            StatusExpiredBg,
            StatusExpiredRed,
            Icons.Default.Warning
        )
        daysRemaining <= 30 -> Tuple5(
            ExpiryStatus.EXPIRING_SOON,
            "Expires in ${daysRemaining}d",
            StatusExpiringBg,
            StatusExpiringAmber,
            Icons.Default.Schedule
        )
        else -> Tuple5(
            ExpiryStatus.VALID,
            "Valid",
            StatusValidBg,
            StatusValidGreen,
            Icons.Default.CheckCircle
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val val1: A,
    val val2: B,
    val val3: C,
    val val4: D,
    val val5: E
)
