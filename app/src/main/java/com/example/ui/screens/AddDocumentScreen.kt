package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.FolderEntity
import com.example.ui.components.DEFAULT_CATEGORIES
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val FAMILY_MEMBERS = listOf("Self", "Spouse", "Father", "Mother", "Child", "Business")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    initialCategory: String? = null,
    initialFamilyMember: String? = null,
    initialFolder: String? = null,
    capturedImageBytes: ByteArray? = null,
    onClearCapturedImage: () -> Unit = {},
    categoriesList: List<String> = DEFAULT_CATEGORIES,
    familyMembersList: List<String> = FAMILY_MEMBERS,
    foldersList: List<FolderEntity> = emptyList(),
    onCreateCategory: (String) -> Unit = {},
    onCreateFamilyMember: (String) -> Unit = {},
    onCreateFolder: (String) -> Unit = {},
    onImportFile: (Uri, String, String, Boolean, Long?, Long?, String, String, String, String, String, String) -> Unit,
    onImportCameraBytes: (ByteArray, String, String, Long?, Long?, String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onLaunchCamera: () -> Unit,
    onMarkPickerActive: () -> Unit = {},
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var familyMember by remember(initialFamilyMember) {
        mutableStateOf(initialFamilyMember?.takeIf { it.isNotBlank() } ?: "Self")
    }
    var category by remember(initialCategory) {
        mutableStateOf(initialCategory?.takeIf { it.isNotBlank() } ?: (categoriesList.firstOrNull() ?: "Aadhaar Card"))
    }
    var folderName by remember(initialFolder, foldersList) {
        mutableStateOf(initialFolder?.takeIf { it.isNotBlank() } ?: (foldersList.firstOrNull()?.name ?: "General"))
    }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isPdf by remember { mutableStateOf(false) }
    var tags by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var issueDateMillis by remember { mutableStateOf<Long?>(null) }
    var expiryDateMillis by remember { mutableStateOf<Long?>(null) }
    var noExpiryDate by remember { mutableStateOf(true) }
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isTitleError by remember { mutableStateOf(false) }
    var isSourceError by remember { mutableStateOf(false) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var familyDropdownExpanded by remember { mutableStateOf(false) }
    var folderDropdownExpanded by remember { mutableStateOf(false) }

    // Inline Creation Dialogs
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    var showNewFamilyDialog by remember { mutableStateOf(false) }
    var newFamilyInput by remember { mutableStateOf("") }

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderInput by remember { mutableStateOf("") }

    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(capturedImageBytes) {
        if (capturedImageBytes != null) {
            isSourceError = false
            errorMessage = null
            if (title.isBlank()) {
                val timeStamp = SimpleDateFormat("ddMMM_HHmm", Locale.getDefault()).format(Date())
                title = "Scan_$timeStamp"
            }
        }
    }

    val issueDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = issueDateMillis ?: System.currentTimeMillis()
    )

    val expiryDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expiryDateMillis ?: (System.currentTimeMillis() + 365L * 24 * 3600 * 1000)
    )

    if (showIssueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showIssueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    issueDateMillis = issueDatePickerState.selectedDateMillis
                    showIssueDatePicker = false
                }) {
                    Text("Select Issue Date")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    issueDateMillis = null
                    showIssueDatePicker = false
                }) {
                    Text("Clear")
                }
            }
        ) {
            DatePicker(state = issueDatePickerState)
        }
    }

    if (showExpiryDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expiryDateMillis = expiryDatePickerState.selectedDateMillis
                    noExpiryDate = false
                    showExpiryDatePicker = false
                }) {
                    Text("Select Expiry Date")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    expiryDateMillis = null
                    noExpiryDate = true
                    showExpiryDatePicker = false
                }) {
                    Text("No Expiry")
                }
            }
        ) {
            DatePicker(state = expiryDatePickerState)
        }
    }

    // New Category Dialog
    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Create New Category", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter custom category name:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        placeholder = { Text("e.g. Utility Bills") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newCategoryInput.trim()
                        if (clean.isNotBlank()) {
                            onCreateCategory(clean)
                            category = clean
                            newCategoryInput = ""
                            showNewCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Family Profile Dialog
    if (showNewFamilyDialog) {
        AlertDialog(
            onDismissRequest = { showNewFamilyDialog = false },
            title = { Text("Add Family Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter family member name / relationship:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFamilyInput,
                        onValueChange = { newFamilyInput = it },
                        placeholder = { Text("e.g. Daughter, Grandfather") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newFamilyInput.trim()
                        if (clean.isNotBlank()) {
                            onCreateFamilyMember(clean)
                            familyMember = clean
                            newFamilyInput = ""
                            showNewFamilyDialog = false
                        }
                    }
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFamilyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create New Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter folder name:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFolderInput,
                        onValueChange = { newFolderInput = it },
                        placeholder = { Text("e.g. Property Documents") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newFolderInput.trim()
                        if (clean.isNotBlank()) {
                            onCreateFolder(clean)
                            folderName = clean
                            newFolderInput = ""
                            showNewFolderDialog = false
                        }
                    }
                ) {
                    Text("Create & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // System File Pickers
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onClearCapturedImage()
            selectedUri = uri
            isPdf = true
            isSourceError = false
            errorMessage = null
            if (title.isBlank()) {
                title = "Document_${SimpleDateFormat("ddMMM", Locale.getDefault()).format(Date())}"
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onClearCapturedImage()
            selectedUri = uri
            isPdf = false
            isSourceError = false
            errorMessage = null
            if (title.isBlank()) {
                title = "Document_${SimpleDateFormat("ddMMM", Locale.getDefault()).format(Date())}"
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Document",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_add_doc_back")
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
            // Document Source Buttons
            Text(
                text = "1. Select Document Source *",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSourceError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PDF Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onMarkPickerActive()
                            pdfPickerLauncher.launch("application/pdf")
                        }
                        .testTag("btn_source_pdf"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedUri != null && isPdf)
                            MaterialTheme.colorScheme.primaryContainer
                        else if (isSourceError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Import PDF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Gallery Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onMarkPickerActive()
                            imagePickerLauncher.launch("image/*")
                        }
                        .testTag("btn_source_gallery"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedUri != null && !isPdf && capturedImageBytes == null)
                            MaterialTheme.colorScheme.primaryContainer
                        else if (isSourceError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Gallery Photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Camera Scan Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onMarkPickerActive()
                            onLaunchCamera()
                        }
                        .testTag("btn_source_camera"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (capturedImageBytes != null)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan Camera",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Attached Source Banner / Thumbnail
            if (capturedImageBytes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bitmap = remember(capturedImageBytes) {
                            try {
                                BitmapFactory.decodeByteArray(capturedImageBytes, 0, capturedImageBytes.size)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured Scan",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Photo Captured with Camera",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Ready to save with custom details below",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(onClick = { onClearCapturedImage() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            } else if (selectedUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "File attached: ${if (isPdf) "PDF Document" else "Image File"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metadata Form
            Text(
                text = "2. Document Details & Metadata",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isTitleError = false
                },
                label = { Text(if (isTitleError) "Document Name * (Required - Blank!)" else "Document Name *") },
                placeholder = { Text("e.g. Aadhaar Card Front") },
                isError = isTitleError,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .testTag("input_doc_title"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isTitleError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isTitleError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Compact Dropdown: Category Selector
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Document Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dropdown_doc_category"),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categoriesList.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Create New Category", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = {
                            categoryDropdownExpanded = false
                            showNewCategoryDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compact Dropdown: Family Member Selector
            ExposedDropdownMenuBox(
                expanded = familyDropdownExpanded,
                onExpandedChange = { familyDropdownExpanded = !familyDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = familyMember,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Family Profile *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dropdown_doc_family_member"),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = familyDropdownExpanded,
                    onDismissRequest = { familyDropdownExpanded = false }
                ) {
                    familyMembersList.forEach { member ->
                        DropdownMenuItem(
                            text = { Text("👤 $member") },
                            onClick = {
                                familyMember = member
                                familyDropdownExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Add Family Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = {
                            familyDropdownExpanded = false
                            showNewFamilyDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compact Dropdown: Folder Selector
            ExposedDropdownMenuBox(
                expanded = folderDropdownExpanded,
                onExpandedChange = { folderDropdownExpanded = !folderDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Folder") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dropdown_doc_folder"),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = folderDropdownExpanded,
                    onDismissRequest = { folderDropdownExpanded = false }
                ) {
                    val availableFolders = if (foldersList.isEmpty()) listOf("General", "Personal ID", "Financial & Tax", "Medical & Health") else foldersList.map { it.name }
                    availableFolders.forEach { fName ->
                        DropdownMenuItem(
                            text = { Text("📁 $fName") },
                            onClick = {
                                folderName = fName
                                folderDropdownExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Create New Folder", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = {
                            folderDropdownExpanded = false
                            showNewFolderDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Account / Card / ID Number") },
                placeholder = { Text("e.g. 12-digit Aadhaar / PAN / Bank A/C") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doc_account_number"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = issuer,
                onValueChange = { issuer = it },
                label = { Text("Issuing Authority / Bank (Optional)") },
                placeholder = { Text("e.g. UIDAI, Income Tax Dept, SBI, RTO") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doc_issuer"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Issue Date Field
            val issueDateStr = if (issueDateMillis != null) {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(issueDateMillis!!))
            } else "No Issue Date Set"

            OutlinedTextField(
                value = issueDateStr,
                onValueChange = {},
                readOnly = true,
                label = { Text("Issue Date (Optional)") },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (issueDateMillis != null) {
                            IconButton(onClick = { issueDateMillis = null }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Clear Issue Date"
                                )
                            }
                        }
                        IconButton(onClick = { showIssueDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pick Issue Date"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showIssueDatePicker = true }
                    .testTag("input_doc_issue_date"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Expiry Date Section with "No Expiry Date" Checkbox
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                noExpiryDate = !noExpiryDate
                                if (noExpiryDate) {
                                    expiryDateMillis = null
                                } else if (expiryDateMillis == null) {
                                    expiryDateMillis = System.currentTimeMillis() + 365L * 24 * 3600 * 1000
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = noExpiryDate,
                            onCheckedChange = { checked ->
                                noExpiryDate = checked
                                if (checked) {
                                    expiryDateMillis = null
                                } else if (expiryDateMillis == null) {
                                    expiryDateMillis = System.currentTimeMillis() + 365L * 24 * 3600 * 1000
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No Expiry Date (Lifetime / Permanent Document)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "For lifelong IDs like Aadhaar, PAN, Voter ID, Certificates",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!noExpiryDate) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val expiryDateStr = if (expiryDateMillis != null) {
                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryDateMillis!!))
                        } else "Select Expiry Date"

                        OutlinedTextField(
                            value = expiryDateStr,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expiry Date *") },
                            trailingIcon = {
                                IconButton(onClick = { showExpiryDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Pick Expiry Date"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showExpiryDatePicker = true }
                                .testTag("input_doc_expiry"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags") },
                placeholder = { Text("e.g. identity, government, primary") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doc_tags"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & References (Optional)") },
                placeholder = { Text("e.g. Card number ending 4321, issued Delhi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doc_notes"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (selectedUri == null && capturedImageBytes == null) {
                        isSourceError = true
                        errorMessage = "Please select a document source (PDF, Gallery, or Camera) first!"
                        return@Button
                    }
                    if (title.isBlank()) {
                        isTitleError = true
                        errorMessage = "Document Name cannot be blank. Field highlighted in red."
                        try {
                            titleFocusRequester.requestFocus()
                        } catch (_: Exception) {}
                        return@Button
                    }
                    isProcessing = true
                    val finalExpiry = if (noExpiryDate) null else expiryDateMillis

                    if (capturedImageBytes != null) {
                        onImportCameraBytes(
                            capturedImageBytes,
                            title.trim(),
                            category,
                            finalExpiry,
                            issueDateMillis,
                            tags.trim(),
                            notes.trim(),
                            accountNumber.trim(),
                            issuer.trim(),
                            familyMember,
                            folderName
                        )
                    } else if (selectedUri != null) {
                        onImportFile(
                            selectedUri!!,
                            title.trim(),
                            category,
                            isPdf,
                            finalExpiry,
                            issueDateMillis,
                            tags.trim(),
                            notes.trim(),
                            accountNumber.trim(),
                            issuer.trim(),
                            familyMember,
                            folderName
                        )
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_document"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Securing & Digitizing...")
                } else {
                    Text(
                        text = "Save Encrypted Document",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
