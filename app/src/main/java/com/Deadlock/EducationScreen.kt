package com.Deadlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EducationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Privacy 101",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 24.dp, start = 8.dp)
        )

        InfoCard(
            title = "Permission Blindness",
            description = "Users often grant invasive permissions indiscriminately during installation without understanding the long-term security risks. DeadLock is designed to cure this blindness.",
            icon = Icons.Rounded.Info,
            iconTint = SamsungBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The 'Big Three' Sensors",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
        )

        InfoCard(
            title = "Camera & Microphone",
            description = "Over-privileged apps can exploit these sensors to silently capture audio, images, or video in the background without your active consent.",
            icon = Icons.Rounded.Warning,
            iconTint = DangerRed
        )

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            title = "Background Location",
            description = "Constant location access allows applications to map your daily routine, home address, and workplace, threatening physical privacy.",
            icon = Icons.Rounded.Lock,
            iconTint = SamsungBlue
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InfoCard(title: String, description: String, icon: ImageVector, iconTint: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCard)
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}