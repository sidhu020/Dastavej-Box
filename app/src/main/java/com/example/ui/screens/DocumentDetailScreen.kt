package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.DocumentEntity
import com.example.data.database.FolderEntity
import com.example.ui.components.DEFAULT_CATEGORIES
import com.example.ui.components.ExpiryBadge
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    document: DocumentEntity,
    categoriesList: List<String> = DEFAULT_CATEGORIES,
    familyMembersList: List<String> = listOf("Self", "Spouse", "Father", "Mother", "Child", "Business"),
    foldersList: List<FolderEntity> = emptyList(),
    onOpenViewer: () -> Unit,
    onEditDocument: (DocumentEntity) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onConvertToPdf: (Long) -> Unit,
    onLogRenewal: (Long, String, Long?) -> Unit,
    onDeleteDocument: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenewalDialog by remember { mutableStateOf(false) }
    var isNumberCopied by remember { mutableStateOf(false) }
    var isOcrCopied by remember { mutableStateOf(false) }

    // OCR accordion dropdown state
    var ocrDropdownExpanded by remember { mutableStateOf(false) }

    // Privacy shield toggle
    var isImageVisible by remember { mutableStateOf(false) }

    // Edit fields
    var editTitle by remember { mutableStateOf(document.title) }
    var editCategory by remember { mutableStateOf(document.category) }
    var editFamilyMember by remember { mutableStateOf(document.familyMember) }
    var editFolderName by remember { mutableStateOf(document.folderName) }
    var editAccountNumber by remember { mutableStateOf(document.accountNumber) }
    var editIssuer by remember { mutableStateOf(document.issuer) }
    var editTags by remember { mutableStateOf(document.tags) }
    var editNotes by remember { mutableStateOf(document.notes) }
    var editIssueDate by remember { mutableStateOf(document.issueDate) }
    var editExpiryDate by remember { mutableStateOf(document.expiryDate) }
    var noExpiryDate by remember { mutableStateOf(document.expiryDate == null) }
    var isTitleBlankError by remember { mutableStateOf(false) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var familyDropdownExpanded by remember { mutableStateOf(false) }
    var folderDropdownExpanded by remember { mutableStateOf(false) }

    // Date pickers for edit dialog
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }

    // Renewal fields
    var renewalNote by remember { mutableStateOf("") }
    var renewalNewExpiryDate by remember { mutableStateOf<Long?>(null) }
    var showRenewalDatePicker by remember { mutableStateOf(false) }

    val editIssueDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = editIssueDate ?: System.currentTimeMillis()
    )

    val editExpiryDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = editExpiryDate ?: (System.currentTimeMillis() + 365L * 24 * 3600 * 1000)
    )

    val renewalDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = renewalNewExpiryDate ?: (System.currentTimeMillis() + 365L * 24 * 3600 * 1000)
    )

    val addedDateStr = remember(document.dateAdded) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(document.dateAdded))
    }

    if (showIssueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showIssueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    editIssueDate = editIssueDatePickerState.selectedDateMillis
                    showIssueDatePicker = false
                }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editIssueDate = null
                    showIssueDatePicker = false
                }) {
                    Text("Clear")
                }
            }
        ) {
            DatePicker(state = editIssueDatePickerState)
        }
    }

    if (showExpiryDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    editExpiryDate = editExpiryDatePickerState.selectedDateMillis
                    noExpiryDate = false
                    showExpiryDatePicker = false
                }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editExpiryDate = null
                    noExpiryDate = true
                    showExpiryDatePicker = false
                }) {
                    Text("No Expiry")
                }
            }
        ) {
            DatePicker(state = editExpiryDatePickerState)
        }
    }

    if (showRenewalDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showRenewalDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    renewalNewExpiryDate = renewalDatePickerState.selectedDateMillis
                    showRenewalDatePicker = false
                }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    renewalNewExpiryDate = null
                    showRenewalDatePicker = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = renewalDatePickerState)
        }
    }

    // Move to Trash Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Move to Trash?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This document will be moved to the Trash Bin. You can restore it anytime or empty the trash later.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteDocument()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("btn_confirm_delete")
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Renewal History Logger Dialog
    if (showRenewalDialog) {
        AlertDialog(
            onDismissRequest = { showRenewalDialog = false },
            title = { Text("Log Document Renewal / Update", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Record a renewal, payment receipt, or updated validity for this document.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = renewalNote,
                        onValueChange = { renewalNote = it },
                        label = { Text("Renewal Note / Reference") },
                        placeholder = { Text("e.g. Renewed for 5 years, receipt #8921") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val renewalExpiryStr = if (renewalNewExpiryDate != null) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(renewalNewExpiryDate!!))
                    } else "Keep existing expiry or tap to update"

                    OutlinedTextField(
                        value = renewalExpiryStr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("New Expiry Date (Optional)") },
                        trailingIcon = {
                            IconButton(onClick = { showRenewalDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRenewalDatePicker = true }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renewalNote.isNotBlank() || renewalNewExpiryDate != null) {
                            onLogRenewal(document.id, renewalNote.trim(), renewalNewExpiryDate)
                            renewalNote = ""
                            renewalNewExpiryDate = null
                            showRenewalDialog = false
                        }
                    }
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenewalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Document Details Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Document Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = {
                            editTitle = it
                            if (it.isNotBlank()) isTitleBlankError = false
                        },
                        label = { Text(if (isTitleBlankError) "Document Name * (Required)" else "Document Name *") },
                        isError = isTitleBlankError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categoriesList.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        editCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Family Profile Dropdown
                    ExposedDropdownMenuBox(
                        expanded = familyDropdownExpanded,
                        onExpandedChange = { familyDropdownExpanded = !familyDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editFamilyMember,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Family Profile") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = familyDropdownExpanded,
                            onDismissRequest = { familyDropdownExpanded = false }
                        ) {
                            familyMembersList.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text("👤 $member") },
                                    onClick = {
                                        editFamilyMember = member
                                        familyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Folder Dropdown
                    ExposedDropdownMenuBox(
                        expanded = folderDropdownExpanded,
                        onExpandedChange = { folderDropdownExpanded = !folderDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = editFolderName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Folder") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            singleLine = true
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
                                        editFolderName = fName
                                        folderDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editAccountNumber,
                        onValueChange = { editAccountNumber = it },
                        label = { Text("Account / Card / ID Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editIssuer,
                        onValueChange = { editIssuer = it },
                        label = { Text("Issuing Authority") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Issue Date Picker in Edit Dialog
                    val issueDateStr = if (editIssueDate != null) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(editIssueDate!!))
                    } else "No Issue Date Set"

                    OutlinedTextField(
                        value = issueDateStr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Issue Date") },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (editIssueDate != null) {
                                    IconButton(onClick = { editIssueDate = null }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
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
                    )

                    // Expiry Date Section in Edit Dialog
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        noExpiryDate = !noExpiryDate
                                        if (noExpiryDate) {
                                            editExpiryDate = null
                                        } else if (editExpiryDate == null) {
                                            editExpiryDate = System.currentTimeMillis() + 365L * 24 * 3600 * 1000
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = noExpiryDate,
                                    onCheckedChange = { checked ->
                                        noExpiryDate = checked
                                        if (checked) {
                                            editExpiryDate = null
                                        } else if (editExpiryDate == null) {
                                            editExpiryDate = System.currentTimeMillis() + 365L * 24 * 3600 * 1000
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lifetime / No Expiry Date", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (!noExpiryDate) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val expiryDateStr = if (editExpiryDate != null) {
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(editExpiryDate!!))
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
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editTags,
                        onValueChange = { editTags = it },
                        label = { Text("Tags") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes / References") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isBlank()) {
                            isTitleBlankError = true
                            return@Button
                        }
                        val finalExpiry = if (noExpiryDate) null else editExpiryDate
                        val updatedDoc = document.copy(
                            title = editTitle.trim(),
                            category = editCategory,
                            familyMember = editFamilyMember,
                            folderName = editFolderName,
                            accountNumber = editAccountNumber.trim(),
                            issuer = editIssuer.trim(),
                            tags = editTags.trim(),
                            notes = editNotes.trim(),
                            issueDate = editIssueDate,
                            expiryDate = finalExpiry
                        )
                        onEditDocument(updatedDoc)
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("btn_save_edit")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Document Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_detail_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onToggleFavorite(document.id, document.isFavorite) },
                        modifier = Modifier.testTag("btn_detail_fav")
                    ) {
                        Icon(
                            imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (document.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            tint = if (document.isFavorite) androidx.compose.ui.graphics.Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("btn_detail_edit")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("btn_detail_delete")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Move to Trash",
                            tint = MaterialTheme.colorScheme.error
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Preview Banner Card (Privacy Shield Mode)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isImageVisible) {
                        if (!document.thumbnailPath.isNullOrEmpty() && File(document.thumbnailPath).exists()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(document.thumbnailPath))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = document.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (File(document.filePath).exists() && document.fileType == "IMAGE") {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(document.filePath))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = document.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "PDF Document Asset",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        // Protected Privacy Placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Hidden Image",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Document Preview Hidden for Privacy",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { isImageVisible = true },
                                modifier = Modifier.testTag("btn_reveal_image"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveRedEye,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Click to Reveal Image", fontSize = 12.sp)
                            }
                        }
                    }

                    if (isImageVisible) {
                        // Open Full Viewer / Hide Overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { isImageVisible = false },
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .testTag("btn_hide_image")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Hide Image",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = onOpenViewer,
                                modifier = Modifier.testTag("btn_open_viewer"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (document.fileType == "PDF") "Open PDF Viewer" else "Full View",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Single Line Share Row
            Button(
                onClick = {
                    val file = File(document.filePath)
                    if (file.exists()) {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = if (document.fileType == "PDF") "application/pdf" else "image/*"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Document Securely"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_share_whatsapp"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Document via WhatsApp or Apps", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }

            if (document.fileType == "IMAGE") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onConvertToPdf(document.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_convert_pdf"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Convert Image to PDF Document", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Account / Card / Document Number Card with Copy Option
            if (document.accountNumber.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Account / Document Number",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = document.accountNumber,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(document.accountNumber))
                                isNumberCopied = true
                            },
                            modifier = Modifier.testTag("btn_copy_account_number"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (isNumberCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy Number",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isNumberCopied) "Copied!" else "Copy", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Proper Structured Metadata Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Document Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Structured Detail Rows in Proper Format
                    DocumentDetailRow(label = "Document Name", value = document.title, isPrimary = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    DocumentDetailRow(label = "Family Profile", value = "👤 ${document.familyMember}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    DocumentDetailRow(label = "Folder", value = "📁 ${document.folderName}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    DocumentDetailRow(label = "Category", value = document.category)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    val issueDateStr = if (document.issueDate != null) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(document.issueDate))
                    } else "Not Set"
                    DocumentDetailRow(label = "Issue Date", value = issueDateStr)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    val expiryDateStr = if (document.expiryDate != null) {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(document.expiryDate))
                    } else "Permanent (No Expiry)"
                    DocumentDetailRow(
                        label = "Expiry Date",
                        value = expiryDateStr,
                        customBadge = {
                            if (document.expiryDate != null) {
                                ExpiryBadge(expiryDateMillis = document.expiryDate)
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    if (document.issuer.isNotBlank()) {
                        DocumentDetailRow(label = "Issuing Authority", value = document.issuer)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }

                    DocumentDetailRow(label = "Added On", value = addedDateStr)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    DocumentDetailRow(label = "File Specs", value = "${document.fileType} • ${document.fileSize / 1024} KB")

                    if (document.tags.isNotBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        DocumentDetailRow(label = "Tags", value = document.tags)
                    }

                    if (document.notes.isNotBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Notes & References",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = document.notes,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dropdown Accordion for OCR Extracted Text
            if (document.ocrText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { ocrDropdownExpanded = !ocrDropdownExpanded }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "OCR Extracted Text",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val wordsCount = document.ocrText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                                    Text(
                                        text = "$wordsCount words digitized • Tap to ${if (ocrDropdownExpanded) "collapse" else "view"}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(document.ocrText))
                                        isOcrCopied = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isOcrCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy Text",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Icon(
                                    imageVector = if (ocrDropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle Dropdown",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = ocrDropdownExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = document.ocrText,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Renewal & Document Timeline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Timeline & Renewals",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showRenewalDialog = true },
                            modifier = Modifier.testTag("btn_log_renewal")
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = "Renew Document", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• [$addedDateStr] Created & saved into vault",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (document.renewalHistory.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = document.renewalHistory,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DocumentDetailRow(
    label: String,
    value: String,
    isPrimary: Boolean = false,
    customBadge: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )

        Row(
            modifier = Modifier.weight(0.6f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = if (isPrimary) 14.5.sp else 13.5.sp,
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (customBadge != null) {
                Spacer(modifier = Modifier.width(6.dp))
                customBadge()
            }
        }
    }
}
