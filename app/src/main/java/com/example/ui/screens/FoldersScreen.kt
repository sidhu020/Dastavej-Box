package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DocumentEntity
import com.example.data.database.FolderEntity
import com.example.ui.components.DocumentCard
import java.util.Locale

private val PRESET_FOLDER_COLORS = listOf(
    "#3F51B5", // Indigo
    "#00897B", // Teal
    "#E91E63", // Pink
    "#FB8C00", // Orange
    "#8E24AA", // Purple
    "#43A047", // Green
    "#2196F3", // Blue
    "#795548"  // Brown
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    folders: List<FolderEntity>,
    allDocuments: List<DocumentEntity>,
    onCreateFolder: (name: String, colorHex: String, description: String) -> Unit,
    onEditFolder: (id: Long, name: String, colorHex: String, description: String) -> Unit,
    onDeleteFolder: (id: Long, name: String) -> Unit,
    onMoveDocumentToFolder: (docId: Long, targetFolder: String) -> Unit = { _, _ -> },
    onCopyDocumentToFolder: (docId: Long, targetFolder: String) -> Unit = { _, _ -> },
    onDocumentClick: (Long) -> Unit = {},
    onAddDocumentToFolder: ((folderName: String) -> Unit)? = null,
    onOpenDrawer: () -> Unit = {}
) {
    var activeFolder by remember { mutableStateOf<FolderEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateModal by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }

    // Dialog state for moving/copying documents
    var moveDocTarget by remember { mutableStateOf<DocumentEntity?>(null) }
    var copyDocTarget by remember { mutableStateOf<DocumentEntity?>(null) }

    // Intercept back button when inside a specific folder
    BackHandler(enabled = activeFolder != null) {
        activeFolder = null
    }

    // Keep activeFolder up to date if folders list updates
    val currentFolder = activeFolder?.let { af ->
        folders.find { it.id == af.id } ?: af
    }

    // When inside an active folder, render in-folder files view
    if (currentFolder != null) {
        val folderDocs = remember(currentFolder, allDocuments, searchQuery) {
            allDocuments.filter { doc ->
                !doc.isTrashed &&
                doc.folderName.equals(currentFolder.name, ignoreCase = true) &&
                (searchQuery.isBlank() || doc.title.contains(searchQuery, ignoreCase = true) || doc.category.contains(searchQuery, ignoreCase = true) || doc.accountNumber.contains(searchQuery, ignoreCase = true))
            }
        }

        val pdfCount = remember(folderDocs) { folderDocs.count { it.fileType == "PDF" } }
        val imgCount = remember(folderDocs) { folderDocs.count { it.fileType != "PDF" } }

        val parsedFolderColor = remember(currentFolder.colorHex) {
            try {
                Color(android.graphics.Color.parseColor(currentFolder.colorHex))
            } catch (_: Exception) {
                Color(0xFF3F51B5)
            }
        }

        val folderStorageBytes = remember(folderDocs) {
            folderDocs.sumOf { it.fileSize }
        }
        val folderStorageFormatted = remember(folderStorageBytes) {
            String.format(Locale.US, "%.1f MB", folderStorageBytes / (1024.0 * 1024.0))
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeFolder = null },
                            modifier = Modifier.testTag("btn_back_from_folder")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Folders",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(parsedFolderColor.copy(alpha = 0.2f))
                                .border(1.dp, parsedFolderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = parsedFolderColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentFolder.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${folderDocs.size} files • $pdfCount PDF • $imgCount IMG • $folderStorageFormatted",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Edit folder button
                        IconButton(
                            onClick = { folderToEdit = currentFolder },
                            modifier = Modifier.testTag("btn_edit_active_folder")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Folder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (onAddDocumentToFolder != null) {
                            Button(
                                onClick = { onAddDocumentToFolder(currentFolder.name) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_add_doc_to_folder")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Doc", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Folder Details Header Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (currentFolder.description.isNotBlank()) {
                                Text(
                                    text = currentFolder.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Single line stats badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = parsedFolderColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${folderDocs.size} Files",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = parsedFolderColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                if (pdfCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE53935).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "$pdfCount PDF",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE53935),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (imgCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF00ACC1).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "$imgCount IMG",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00ACC1),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = folderStorageFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // In-Folder Search Field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search files in ${currentFolder.name}...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_in_folder")
                    )
                }

                // Document List inside Folder
                if (folderDocs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(parsedFolderColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = parsedFolderColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isBlank()) "No documents in ${currentFolder.name}" else "No matching files found",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                if (onAddDocumentToFolder != null && searchQuery.isBlank()) {
                                    Button(
                                        onClick = { onAddDocumentToFolder(currentFolder.name) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_add_first_doc_to_folder")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Document to ${currentFolder.name}")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(folderDocs, key = { it.id }) { doc ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DocumentCard(
                                document = doc,
                                onClick = { onDocumentClick(doc.id) }
                            )
                            // Document Action Bar for Move / Copy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { moveDocTarget = doc },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Move", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                TextButton(
                                    onClick = { copyDocTarget = doc },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Default: All Folders Grid View
        val filteredFolders = remember(folders, searchQuery) {
            folders.filter { folder ->
                searchQuery.isBlank() ||
                folder.name.contains(searchQuery, ignoreCase = true) ||
                folder.description.contains(searchQuery, ignoreCase = true)
            }
        }

        val totalDocsInFolders = remember(allDocuments) {
            allDocuments.count { it.folderName.isNotBlank() && !it.isTrashed }
        }
        val totalFolderStorageBytes = remember(allDocuments) {
            allDocuments.filter { it.folderName.isNotBlank() && !it.isTrashed }.sumOf { it.fileSize }
        }
        val storageFormatted = remember(totalFolderStorageBytes) {
            String.format(Locale.US, "%.1f MB", totalFolderStorageBytes / (1024.0 * 1024.0))
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("btn_hamburger_folders")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Folder Explorer",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                text = "${folders.size} Folders • $totalDocsInFolders Files ($storageFormatted)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { showCreateModal = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_create_folder_top")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Folder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search folders by name or tag...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_folders")
                    )
                }

                // Folders Grid List
                if (filteredFolders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "No folders found" else "No matching folders found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Organize tax, medical, insurance, and IDs into custom folders",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showCreateModal = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_empty_create_folder")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Folder")
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredFolders, key = { it.id }) { folder ->
                            val folderDocs = remember(folder.name, allDocuments) {
                                allDocuments.filter { !it.isTrashed && it.folderName.equals(folder.name, ignoreCase = true) }
                            }
                            val pdfCount = remember(folderDocs) { folderDocs.count { it.fileType == "PDF" } }
                            val imgCount = remember(folderDocs) { folderDocs.count { it.fileType != "PDF" } }

                            val parsedColor = remember(folder.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(folder.colorHex))
                                } catch (_: Exception) {
                                    Color(0xFF3F51B5)
                                }
                            }

                            var menuExpanded by remember { mutableStateOf(false) }

                            Card(
                                onClick = { activeFolder = folder },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            parsedColor.copy(alpha = 0.5f),
                                            parsedColor.copy(alpha = 0.12f)
                                        )
                                    )
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_folder_${folder.name}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    parsedColor.copy(alpha = 0.12f),
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                                    MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        )
                                        .padding(12.dp)
                                ) {
                                    // Top Icon & Menu Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Folder Badge with Glow
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(parsedColor.copy(alpha = 0.20f))
                                                .border(1.dp, parsedColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = parsedColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        // Action Dropdown Menu
                                        Box {
                                            IconButton(
                                                onClick = { menuExpanded = true },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("btn_folder_options_${folder.name}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = menuExpanded,
                                                onDismissRequest = { menuExpanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Open Folder") },
                                                    onClick = {
                                                        menuExpanded = false
                                                        activeFolder = folder
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.FolderOpen,
                                                            contentDescription = null,
                                                            tint = parsedColor
                                                        )
                                                    }
                                                )

                                                DropdownMenuItem(
                                                    text = { Text("Edit Folder") },
                                                    onClick = {
                                                        menuExpanded = false
                                                        folderToEdit = folder
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                )

                                                if (onAddDocumentToFolder != null) {
                                                    DropdownMenuItem(
                                                        text = { Text("Add Document Here") },
                                                        onClick = {
                                                            menuExpanded = false
                                                            onAddDocumentToFolder(folder.name)
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Default.Add,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    )
                                                }

                                                if (!folder.name.equals("General", ignoreCase = true)) {
                                                    DropdownMenuItem(
                                                        text = { Text("Delete Folder", color = MaterialTheme.colorScheme.error) },
                                                        onClick = {
                                                            menuExpanded = false
                                                            folderToDelete = folder
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Default.Delete,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Folder Title
                                    Text(
                                        text = folder.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    // Folder Description
                                    if (folder.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = folder.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // ONE-LINE Stats Badges: file count, PDF count, IMG count
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Count Badge
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = parsedColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${folderDocs.size} files",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = parsedColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (pdfCount > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFE53935).copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "$pdfCount PDF",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE53935),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (imgCount > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF00ACC1).copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "$imgCount IMG",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00ACC1),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Explore Action Footer
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "View Files",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = parsedColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Move Document Dialog
    moveDocTarget?.let { doc ->
        MoveOrCopyFolderDialog(
            title = "Move \"${doc.title}\"",
            actionLabel = "Move to Folder",
            folders = folders,
            currentFolder = doc.folderName,
            onDismiss = { moveDocTarget = null },
            onSelectFolder = { targetFolder ->
                onMoveDocumentToFolder(doc.id, targetFolder)
                moveDocTarget = null
            }
        )
    }

    // Copy Document Dialog
    copyDocTarget?.let { doc ->
        MoveOrCopyFolderDialog(
            title = "Copy \"${doc.title}\"",
            actionLabel = "Copy to Folder",
            folders = folders,
            currentFolder = doc.folderName,
            onDismiss = { copyDocTarget = null },
            onSelectFolder = { targetFolder ->
                onCopyDocumentToFolder(doc.id, targetFolder)
                copyDocTarget = null
            }
        )
    }

    // Delete Folder Confirmation Dialog
    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Folder?") },
            text = {
                Text(
                    "Are you sure you want to delete the folder \"${folder.name}\"? Any documents inside this folder will automatically be kept safe and moved to the General folder."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFolder(folder.id, folder.name)
                        folderToDelete = null
                        if (activeFolder?.id == folder.id) {
                            activeFolder = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create New Folder Dialog
    if (showCreateModal) {
        FolderFormDialog(
            title = "Create Custom Folder",
            confirmLabel = "Create Folder",
            presetColors = PRESET_FOLDER_COLORS,
            onDismiss = { showCreateModal = false },
            onSave = { name, color, desc ->
                onCreateFolder(name, color, desc)
                showCreateModal = false
            }
        )
    }

    // Edit Folder Dialog
    folderToEdit?.let { folder ->
        FolderFormDialog(
            title = "Edit Folder Details",
            confirmLabel = "Save Changes",
            initialName = folder.name,
            initialDesc = folder.description,
            initialColorHex = folder.colorHex,
            presetColors = PRESET_FOLDER_COLORS,
            onDismiss = { folderToEdit = null },
            onSave = { name, color, desc ->
                onEditFolder(folder.id, name, color, desc)
                folderToEdit = null
            }
        )
    }
}

@Composable
private fun FolderFormDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialDesc: String = "",
    initialColorHex: String = PRESET_FOLDER_COLORS.first(),
    presetColors: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String, description: String) -> Unit
) {
    var folderName by remember { mutableStateOf(initialName) }
    var folderDesc by remember { mutableStateOf(initialDesc) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }
    var nameError by remember { mutableStateOf(false) }

    val activeColor = remember(selectedColorHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (_: Exception) {
            Color(0xFF3F51B5)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Folder Name Input
                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Folder Name *") },
                    placeholder = { Text("e.g. Tax & Returns, Property, Car") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Folder name cannot be empty", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_create_folder_name")
                )

                // Folder Description
                OutlinedTextField(
                    value = folderDesc,
                    onValueChange = { folderDesc = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g. FY 2024-25 records and receipts") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Folder Color Picker
                Column {
                    Text(
                        text = "Folder Theme Color:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColors.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (_: Exception) {
                                Color(0xFF3F51B5)
                            }
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (isSelected) 2.5.dp else 0.dp,
                                        if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isBlank()) {
                        nameError = true
                    } else {
                        onSave(
                            folderName.trim(),
                            selectedColorHex,
                            folderDesc.trim()
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_create_folder")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MoveOrCopyFolderDialog(
    title: String,
    actionLabel: String,
    folders: List<FolderEntity>,
    currentFolder: String,
    onDismiss: () -> Unit,
    onSelectFolder: (targetFolder: String) -> Unit
) {
    var selectedTarget by remember {
        val other = folders.firstOrNull { !it.name.equals(currentFolder, ignoreCase = true) }
        mutableStateOf(other?.name ?: "General")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column {
                Text("Select destination folder:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(folders) { folder ->
                        val isCurrent = folder.name.equals(currentFolder, ignoreCase = true)
                        val isSelected = selectedTarget.equals(folder.name, ignoreCase = true)
                        val color = try {
                            Color(android.graphics.Color.parseColor(folder.colorHex))
                        } catch (_: Exception) {
                            Color(0xFF3F51B5)
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTarget = folder.name }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isCurrent) {
                                        Text(
                                            text = "Current folder",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTarget = folder.name }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectFolder(selectedTarget) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(actionLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
