package com.example.ui.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.AuthManager

enum class AuthTab { LOGIN, SIGNUP }

@Composable
fun AuthScreen(
    authManager: AuthManager,
    initialTab: AuthTab = if (authManager.hasAccount) AuthTab.LOGIN else AuthTab.SIGNUP,
    onSwitchToPinUnlock: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) }
    var showCloudRecoveryDialog by remember { mutableStateOf(false) }

    // Common State
    var username by remember { mutableStateOf(authManager.savedUsername) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf(authManager.savedFirstName) }
    var lastName by remember { mutableStateOf(authManager.savedLastName) }
    var phoneNumber by remember { mutableStateOf(authManager.savedPhoneNumber) }
    var rememberMe by remember { mutableStateOf(authManager.isRememberMeEnabled) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    if (showCloudRecoveryDialog) {
        CloudRecoveryDialog(
            authManager = authManager,
            onDismiss = { showCloudRecoveryDialog = false },
            onRecoverySuccess = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Icon & Title
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF38BDF8).copy(alpha = 0.25f), Color(0xFF0284C7).copy(alpha = 0.05f))
                        )
                    )
                    .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "کیف پول",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "کیف پول هوشمند (Smart Wallet)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp
                ),
                color = Color.White
            )

            Text(
                text = "مدیریت یکپارچه مالی، پیامک‌های بانکی و دارایی‌ها",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Switcher (ورود / ثبت‌نام)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedTab == AuthTab.LOGIN) Color(0xFF2563EB) else Color.Transparent
                        )
                        .clickable {
                            selectedTab = AuthTab.LOGIN
                            errorMessage = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ورود به حساب",
                        fontWeight = if (selectedTab == AuthTab.LOGIN) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedTab == AuthTab.SIGNUP) Color(0xFF2563EB) else Color.Transparent
                        )
                        .clickable {
                            selectedTab = AuthTab.SIGNUP
                            errorMessage = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ثبت‌نام جدید",
                        fontWeight = if (selectedTab == AuthTab.SIGNUP) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == AuthTab.SIGNUP) Color.White else Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Main Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Common Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("نام کاربری یا شماره همراه") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Common Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("رمز عبور") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )

                    // SIGN UP SPECIFIC FIELDS
                    if (selectedTab == AuthTab.SIGNUP) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = { Text("تکرار رمز عبور") },
                            leadingIcon = {
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF94A3B8))
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4-Digit Security PIN
                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pin = it
                                    errorMessage = null
                                }
                            },
                            label = { Text("پین کد ۴ رقمی ورود سریع") },
                            leadingIcon = {
                                Icon(Icons.Default.Pin, contentDescription = null, tint = Color(0xFF94A3B8))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Name & Last Name (for profile & sync)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("نام") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("نام خانوادگی") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Phone Number
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("شماره همراه (برای همگام‌سازی ابری)") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember Me Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF38BDF8),
                                uncheckedColor = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مرا به خاطر بسپار (عدم نیاز به ورود مجدد)",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
                    }

                    // Error display
                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Submit Button
                    Button(
                        onClick = {
                            if (selectedTab == AuthTab.LOGIN) {
                                // Validate Login
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "نام کاربری و رمز عبور را وارد کنید"
                                    return@Button
                                }
                                val ok = authManager.loginWithPassword(username, password, rememberMe)
                                if (!ok) {
                                    errorMessage = "نام کاربری یا رمز عبور اشتباه است"
                                }
                            } else {
                                // Validate Sign Up
                                if (username.isBlank()) {
                                    errorMessage = "نام کاربری را وارد کنید"
                                    return@Button
                                }
                                if (password.length < 4) {
                                    errorMessage = "رمز عبور باید حداقل ۴ کاراکتر باشد"
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    errorMessage = "رمز عبور و تکرار آن یکسان نیستند"
                                    return@Button
                                }
                                if (pin.length != 4) {
                                    errorMessage = "پین کد ۴ رقمی امنیتی را وارد کنید"
                                    return@Button
                                }

                                val result = authManager.register(
                                    username = username,
                                    password = password,
                                    pin = pin,
                                    firstName = firstName,
                                    lastName = lastName,
                                    phoneNumber = phoneNumber,
                                    rememberMe = rememberMe
                                )

                                result.fold(
                                    onSuccess = {
                                        Toast.makeText(context, "حساب کاربری با موفقیت ساخته شد", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.localizedMessage
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(
                            imageVector = if (selectedTab == AuthTab.LOGIN) Icons.AutoMirrored.Filled.Login else Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedTab == AuthTab.LOGIN) "ورود به حساب کاربری" else "ایجاد حساب کاربری و ورود",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Cloud Recovery Button (Requirement 1)
                    OutlinedButton(
                        onClick = { showCloudRecoveryDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF38BDF8)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "بازیابی از فضای ابری",
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بازیابی اطلاعات از فضای ابری",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Optional PIN mode button if account exists
                    if (authManager.hasAccount && authManager.hasPinSet && onSwitchToPinUnlock != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = onSwitchToPinUnlock,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pin,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ورود سریع با پین کد ۴ رقمی", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
