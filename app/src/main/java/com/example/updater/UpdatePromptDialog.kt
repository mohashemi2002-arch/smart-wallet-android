package com.example.updater

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PersianFormatters
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePromptDialog(
    metadata: UpdateMetadata,
    updateManager: AppUpdateManager,
    onDismiss: () -> Unit
) {
    val downloadState by updateManager.downloadState.collectAsState()
    val scope = rememberCoroutineScope()
    
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BasicAlertDialog(
            onDismissRequest = {
                // If it's mandatory, they shouldn't be able to dismiss it normally,
                // but let's allow dismiss unless downloading, or maybe just block if mandatory.
                if (downloadState !is DownloadState.Downloading && !metadata.mandatory) {
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Color(0xFF5C82E6),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Text(
                        text = if (metadata.mandatory) "بروزرسانی اجباری" else "بروزرسانی جدید در دسترس است",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (metadata.mandatory) Color(0xFFFF6B6B) else Color.White
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF5C82E6).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "نسخه جدید: ${PersianFormatters.toPersianDigits(metadata.versionName)}",
                            color = Color(0xFF7FA2FF),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val notesText = metadata.releaseNotes.joinToString("\n• ", prefix = "• ").ifEmpty { "بهبود عملکرد و رفع ایرادات جزئی." }
                        Text(
                            text = notesText,
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    when (val state = downloadState) {
                        is DownloadState.Idle -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (!metadata.mandatory) {
                                    TextButton(onClick = onDismiss) {
                                        Text("بعداً", color = Color.Gray)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                
                                Button(
                                    onClick = {
                                        scope.launch { updateManager.downloadUpdate(metadata) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
                                ) {
                                    Text("دانلود و نصب", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is DownloadState.Downloading -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { state.progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF5C82E6),
                                    trackColor = Color(0xFF252E3E)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val percentStr = PersianFormatters.toPersianDigits((state.progress * 100).toInt().toString()) + "٪"
                                    Text(percentStr, color = Color.LightGray, fontSize = 12.sp)
                                    
                                    val mbStr = PersianFormatters.toPersianDigits("${state.downloadedMB} / ${state.totalMB} مگابایت")
                                    Text(mbStr, color = Color.LightGray, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("در حال دانلود...")
                                }
                            }
                        }
                        is DownloadState.ReadyToInstall -> {
                            Button(
                                onClick = { updateManager.installUpdate(state.file) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("نصب بروزرسانی", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        is DownloadState.Error -> {
                            Text(state.message, color = Color.Red, fontSize = 12.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (!metadata.mandatory) {
                                    TextButton(onClick = onDismiss) {
                                        Text("بستن", color = Color.Gray)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                
                                Button(
                                    onClick = {
                                        scope.launch { updateManager.downloadUpdate(metadata) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C82E6))
                                ) {
                                    Text("تلاش مجدد", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
