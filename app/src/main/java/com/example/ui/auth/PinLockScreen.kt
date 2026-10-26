package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.domain.util.AuthManager

@Composable
fun PinLockScreen(
    authManager: AuthManager,
    activity: FragmentActivity,
    onSwitchToPasswordLogin: () -> Unit,
    onRecoverCloudClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    // Auto-launch biometric prompt on start if biometric is enabled
    LaunchedEffect(Unit) {
        if (authManager.isBiometricEnabled) {
            authManager.promptBiometric(
                activity = activity,
                onSuccess = {},
                onError = { /* Biometric failed or cancelled, user can use PIN */ }
            )
        }
    }

    // Trigger PIN verification when 4 digits are entered
    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            val success = authManager.loginWithPin(enteredPin)
            if (!success) {
                errorMessage = "پین کد وارد شده نادرست است"
                // Trigger shake animation
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 400
                        -20f at 50
                        20f at 100
                        -15f at 150
                        15f at 200
                        -10f at 250
                        10f at 300
                        0f at 400
                    }
                )
                enteredPin = ""
            } else {
                errorMessage = null
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0A0E17)
                    )
                )
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Lock / Shield Icon
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF3B82F6).copy(alpha = 0.3f), Color(0xFF1E40AF).copy(alpha = 0.1f))
                        )
                    )
                    .border(2.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "قفل برنامه",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Greeting & Name
            val displayName = authManager.savedFirstName.ifEmpty { authManager.savedUsername }.ifEmpty { "کاربر گرامی" }
            Text(
                text = "خوش آمدید، $displayName",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "پین کد ۴ رقمی امنیتی خود را وارد کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4 PIN Indicator Dots with shake animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(x = shakeOffset.value.dp)
            ) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) Color(0xFF38BDF8) else Color(0xFF334155)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) Color(0xFF38BDF8) else Color(0xFF64748B),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error message display
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Custom Modern Numpad
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val numpad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIOMETRIC", "0", "BACKSPACE")
                )

                numpad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { item ->
                            when (item) {
                                "BIOMETRIC" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                                            .clickable {
                                                authManager.promptBiometric(
                                                    activity = activity,
                                                    onSuccess = {},
                                                    onError = { msg -> errorMessage = msg }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "اثر انگشت",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                                "BACKSPACE" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                                            .clickable {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    errorMessage = null
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "پاک کردن",
                                            tint = Color(0xFFE2E8F0),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                else -> {
                                    // Digit Button
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B))
                                            .border(1.dp, Color(0xFF334155), CircleShape)
                                            .clickable {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += item
                                                    errorMessage = null
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Alternatives
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch to standard Password Login
                TextButton(
                    onClick = onSwitchToPasswordLogin,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8))
                ) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ورود با رمز عبور", fontSize = 13.sp)
                }

                // Cloud Recovery
                TextButton(
                    onClick = onRecoverCloudClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF38BDF8))
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بازیابی از فضای ابری", fontSize = 13.sp)
                }
            }
        }
    }
}
