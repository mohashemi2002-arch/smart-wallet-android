package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.RegistrationUiState
import com.example.ui.viewmodel.RegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onRegistrationSuccess: (userId: String) -> Unit = {},
    viewModel: RegistrationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val backupFound by viewModel.backupFound.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is RegistrationUiState.Success) {
            val uid = (uiState as RegistrationUiState.Success).userId
            onRegistrationSuccess(uid)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "ثبت‌نام کاربر",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Avatar & Title
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "آیکون ثبت نام",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ایجاد حساب کاربری",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "جهت فعال‌سازی همگام‌سازی ابری و بازیابی داده‌ها مشخصات خود را وارد کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // First Name Field
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("نام") },
                            placeholder = { Text("مثال: علی") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "نام")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("first_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Last Name Field
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("نام خانوادگی") },
                            placeholder = { Text("مثال: محمدی") },
                            leadingIcon = {
                                Icon(Icons.Default.PersonOutline, contentDescription = "نام خانوادگی")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("last_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Phone Number Field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { 
                                phoneNumber = it 
                                viewModel.onPhoneChanged(it)
                            },
                            label = { Text("شماره همراه") },
                            placeholder = { Text("۰۹۱۲۳۴۵۶۷۸۹") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = "شماره همراه")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("رمز عبور") },
                            placeholder = { Text("حداقل ۶ کاراکتر") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "رمز عبور")
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.testTag("toggle_password_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "مخفی‌سازی رمز عبور" else "نمایش رمز عبور"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.register(firstName, lastName, phoneNumber, password)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Local Device Backup Detected Banner
                if (backupFound) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF1E3A2F)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "بازیابی",
                                tint = androidx.compose.ui.graphics.Color(0xFF81C784)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "نسخه پشتیبان قبلی شما روی این دستگاه یافت شد! با تکمیل ثبت‌نام، تمام اطلاعات به صورت خودکار بازیابی می‌شوند.",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Error State Display
                if (uiState is RegistrationUiState.Error) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "خطا",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = (uiState as RegistrationUiState.Error).message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.register(firstName, lastName, phoneNumber, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("register_button"),
                    shape = RoundedCornerShape(14.dp),
                    enabled = uiState !is RegistrationUiState.Loading
                ) {
                    if (uiState is RegistrationUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "ثبت‌نام و همگام‌سازی ابری",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Restore from Google Sheets Button
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.restoreFromGoogleSheets(firstName, lastName, phoneNumber)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("restore_button"),
                    shape = RoundedCornerShape(14.dp),
                    enabled = uiState !is RegistrationUiState.Loading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = androidx.compose.ui.graphics.Color(0xFF81C784)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "بازیابی",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بازیابی اطلاعات از فضای ابری",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Navigation to Login
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قبلاً ثبت‌نام کرده‌اید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.testTag("navigate_to_login_button")
                    ) {
                        Text(
                            text = "ورود به حساب",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
