package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    isBiometricEnabled: Boolean,
    onToggleBiometrics: (Boolean) -> Unit,
    totalDocuments: Int,
    totalStorageBytes: Long,
    categoriesList: List<String> = emptyList(),
    familyMembersList: List<String> = emptyList(),
    onAddCategory: (String) -> Unit = {},
    onRemoveCategory: (String) -> Unit = {},
    onAddFamilyMember: (String) -> Unit = {},
    onRemoveFamilyMember: (String) -> Unit = {},
    onVerifyOldPin: ((String, (Boolean) -> Unit) -> Unit)? = null,
    onResetPin: ((String, (Boolean) -> Unit) -> Unit)? = null,
    onBiometricAuthForReset: ((((Boolean) -> Unit) -> Unit))? = null,
    onOpenBackupRestore: () -> Unit,
    onOpenAboutApp: () -> Unit = {},
    onOpenAboutDeveloper: () -> Unit = {},
    onLockVault: () -> Unit,
    onBackClick: () -> Unit
) {
    val storageMb = String.format(Locale.US, "%.1f MB", totalStorageBytes / (1024.0 * 1024.0))

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    var showAddFamilyDialog by remember { mutableStateOf(false) }
    var newFamilyInput by remember { mutableStateOf("") }

    var showChangePinDialog by remember { mutableStateOf(false) }
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var isOldPinVerified by remember { mutableStateOf(false) }
    var changePinError by remember { mutableStateOf<String?>(null) }

    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePinDialog = false
                currentPinInput = ""
                newPinInput = ""
                confirmPinInput = ""
                isOldPinVerified = false
                changePinError = null
            },
            title = {
                Text(if (isOldPinVerified) "Set New Vault PIN" else "Verify Current Vault PIN", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isOldPinVerified) {
                        Text(
                            text = "Enter your current 4-digit PIN, or authenticate with biometrics if you forgot it.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = currentPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    currentPinInput = it
                                    changePinError = null
                                }
                            },
                            label = { Text("Current 4-Digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_current_pin")
                        )

                        if (onBiometricAuthForReset != null) {
                            TextButton(
                                onClick = {
                                    onBiometricAuthForReset { authenticated ->
                                        if (authenticated) {
                                            isOldPinVerified = true
                                            changePinError = null
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Forgot PIN? Reset with Biometric")
                            }
                        }
                    } else {
                        Text(
                            text = "Identity verified. Enter your new 4-digit master PIN.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    newPinInput = it
                                    changePinError = null
                                }
                            },
                            label = { Text("New 4-Digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_new_pin")
                        )

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    confirmPinInput = it
                                    changePinError = null
                                }
                            },
                            label = { Text("Confirm New PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_confirm_new_pin")
                        )
                    }

                    if (changePinError != null) {
                        Text(
                            text = changePinError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (!isOldPinVerified) {
                    Button(
                        onClick = {
                            if (currentPinInput.length == 4 && onVerifyOldPin != null) {
                                onVerifyOldPin(currentPinInput) { isValid ->
                                    if (isValid) {
                                        isOldPinVerified = true
                                        changePinError = null
                                    } else {
                                        changePinError = "Incorrect current PIN. Please try again or use biometric reset."
                                    }
                                }
                            } else {
                                changePinError = "Please enter your 4-digit PIN."
                            }
                        }
                    ) {
                        Text("Verify PIN")
                    }
                } else {
                    Button(
                        onClick = {
                            if (newPinInput.length != 4) {
                                changePinError = "New PIN must be exactly 4 digits."
                                return@Button
                            }
                            if (newPinInput != confirmPinInput) {
                                changePinError = "New PIN and Confirm PIN do not match."
                                return@Button
                            }
                            if (onResetPin != null) {
                                onResetPin(newPinInput) { success ->
                                    if (success) {
                                        showChangePinDialog = false
                                        currentPinInput = ""
                                        newPinInput = ""
                                        confirmPinInput = ""
                                        isOldPinVerified = false
                                        changePinError = null
                                    } else {
                                        changePinError = "Failed to update PIN. Try again."
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Save New PIN")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePinDialog = false
                        currentPinInput = ""
                        newPinInput = ""
                        confirmPinInput = ""
                        isOldPinVerified = false
                        changePinError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Vault Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_settings_back")
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
            // Security Preferences Section
            Text(
                text = "Security & Protection",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric Authentication",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Unlock vault with fingerprint or face recognition",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = onToggleBiometrics,
                            modifier = Modifier.testTag("switch_biometrics")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_change_master_pin"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Password, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Master PIN / Reset")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onLockVault,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_lock_vault_now"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Vault Immediately")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Categories Management Section
            Text(
                text = "Document Categories",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Manage Categories",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Custom categories for sorting your documents",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categoriesList.forEach { category ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(category, fontSize = 12.sp) },
                                trailingIcon = {
                                    if (categoriesList.size > 1) {
                                        IconButton(
                                            onClick = { onRemoveCategory(category) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove category",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            newCategoryInput = ""
                            showAddCategoryDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_category_settings"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add New Category")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Family Profiles Section
            Text(
                text = "Family Profiles",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FamilyRestroom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Manage Family Members",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Organize documents by person or entity",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        familyMembersList.forEach { member ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(member, fontSize = 12.sp) },
                                trailingIcon = {
                                    if (familyMembersList.size > 1) {
                                        IconButton(
                                            onClick = { onRemoveFamilyMember(member) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove family member",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            newFamilyInput = ""
                            showAddFamilyDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_add_family_settings"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Family Profile")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data & Storage Section
            Text(
                text = "Data & Backup",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Local Storage Usage",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$totalDocuments Documents stored • $storageMb used",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onOpenBackupRestore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_settings_backup_restore"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encrypted Backup & Restore (.dastavej)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            Text(
                text = "About Application",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dastavej Box v2.4",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Offline Military-Grade Security Vault",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Private by Design. 100% Offline by Default.\n" +
                                "• All documents, images, and metadata are stored in your device's isolated local app storage.\n" +
                                "• Zero cloud tracking or external telemetry.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenAboutApp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_settings_about_app"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("About App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOpenAboutDeveloper,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_settings_about_dev"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("About Developer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Utility Bills") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryInput.isNotBlank()) {
                            onAddCategory(newCategoryInput.trim())
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Family Member Dialog
    if (showAddFamilyDialog) {
        AlertDialog(
            onDismissRequest = { showAddFamilyDialog = false },
            title = { Text("Add Family Profile") },
            text = {
                OutlinedTextField(
                    value = newFamilyInput,
                    onValueChange = { newFamilyInput = it },
                    label = { Text("Profile / Person Name") },
                    placeholder = { Text("e.g. Daughter, Office, Pets") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFamilyInput.isNotBlank()) {
                            onAddFamilyMember(newFamilyInput.trim())
                            showAddFamilyDialog = false
                        }
                    }
                ) {
                    Text("Add Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFamilyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
