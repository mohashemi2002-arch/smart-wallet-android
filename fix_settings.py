import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Make sure to import necessary activity result contracts
if "import androidx.activity.compose.rememberLauncherForActivityResult" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport androidx.activity.compose.rememberLauncherForActivityResult\nimport androidx.activity.result.contract.ActivityResultContracts")

target = '''                        OutlinedButton(
                            onClick = {
                                exportDataText = financeViewModel.generateExport()
                                showExportDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("تولید فایل CSV/Excel")
                        }'''

replacement = '''                        OutlinedButton(
                            onClick = {
                                exportDataText = financeViewModel.generateExport()
                                showExportDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("تولید فایل CSV/Excel")
                        }
                        
                        val excelPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                Toast.makeText(context, "در حال پردازش و ورود اطلاعات اکسل...", Toast.LENGTH_LONG).show()
                                settingsViewModel.importPoolakiExcel(it, context) { resultMsg ->
                                    Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { excelPickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7FA2FF))
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("ورود اطلاعات از اکسل پولکی (Import)", fontWeight = FontWeight.Bold)
                                Text("خواندن فایل‌های پشتیبان اکسل با پسوند xlsx", fontSize = 10.sp, color = Color.Gray)
                            }
                        }'''

content = content.replace(target, replacement)

# Add UploadFile icon if not imported
if "import androidx.compose.material.icons.filled.UploadFile" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.UploadFile")

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
