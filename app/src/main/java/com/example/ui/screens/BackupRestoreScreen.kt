package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onExportBackup: (String, File, (Boolean, String) -> Unit) -> Unit,
    onImportBackup: (File, String, (Boolean, String) -> Unit) -> Unit,
    onMarkPickerActive: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var exportPassphrase by remember { mutableStateOf("") }
    var restorePassphrase by remember { mutableStateOf("") }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }

    var isProcessingExport by remember { mutableStateOf(false) }
    var isProcessingRestore by remember { mutableStateOf(false) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedRestoreUri = uri
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Encrypted Vault Backup", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_backup_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Security Info Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AES-256-GCM Encrypted Vault",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Backups create a password-encrypted .dastavej archive saved directly to Downloads/DastavejVault/Backups/. No cloud account required.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Export Section
            Text(
                text = "Export Encrypted Backup",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = exportPassphrase,
                onValueChange = { exportPassphrase = it },
                label = { Text("Backup Passphrase *") },
                placeholder = { Text("Enter a strong password to lock backup") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_export_passphrase"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (exportPassphrase.isBlank()) {
                        statusMessage = "Please enter a backup passphrase"
                        isErrorStatus = true
                        return@Button
                    }
                    isProcessingExport = true
                    statusMessage = null

                    val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
                    val outputFile = File(backupDir, "dastavej_vault_backup_${System.currentTimeMillis()}.dastavej")

                    onExportBackup(exportPassphrase.trim(), outputFile) { success, msg ->
                        isProcessingExport = false
                        statusMessage = msg
                        isErrorStatus = !success
                    }
                },
                enabled = !isProcessingExport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_export_vault"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessingExport) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Encrypted Archive (.dastavej)")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Restore Section
            Text(
                text = "Restore Vault from Backup",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onMarkPickerActive()
                    filePickerLauncher.launch("*/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_select_backup_file"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (selectedRestoreUri == null) "Select .dastavej Backup File" else "File Selected ✓")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = restorePassphrase,
                onValueChange = { restorePassphrase = it },
                label = { Text("Enter Passphrase to Decrypt *") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_restore_passphrase"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (selectedRestoreUri == null) {
                        statusMessage = "Please select a .dastavej backup file first"
                        isErrorStatus = true
                        return@Button
                    }
                    if (restorePassphrase.isBlank()) {
                        statusMessage = "Please enter the passphrase for decryption"
                        isErrorStatus = true
                        return@Button
                    }

                    isProcessingRestore = true
                    statusMessage = null

                    // Copy Uri content to temp file
                    val tempBackupFile = File(context.cacheDir, "restore_import.dastavej")
                    context.contentResolver.openInputStream(selectedRestoreUri!!)?.use { input ->
                        FileOutputStream(tempBackupFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    onImportBackup(tempBackupFile, restorePassphrase.trim()) { success, msg ->
                        isProcessingRestore = false
                        statusMessage = msg
                        isErrorStatus = !success
                    }
                },
                enabled = !isProcessingRestore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_restore_vault"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessingRestore) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Decrypt & Restore Documents")
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isErrorStatus) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = statusMessage!!,
                        color = if (isErrorStatus) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
