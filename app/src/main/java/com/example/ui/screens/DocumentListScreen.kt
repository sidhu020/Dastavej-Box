package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DocumentEntity
import com.example.data.database.FolderEntity
import com.example.ui.components.DEFAULT_CATEGORIES
import com.example.ui.components.DocumentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    selectedFileType: String?,
    onFileTypeSelect: (String?) -> Unit,
    showOnlyExpiring: Boolean,
    onToggleExpiring: (Boolean) -> Unit,
    selectedFamilyMember: String? = null,
    onFamilyMemberSelect: (String?) -> Unit = {},
    showOnlyFavorites: Boolean = false,
    onToggleFavorites: (Boolean) -> Unit = {},
    categoriesList: List<String> = DEFAULT_CATEGORIES,
    familyMembersList: List<String> = emptyList(),
    foldersList: List<FolderEntity> = emptyList(),
    documents: List<DocumentEntity>,
    onDocumentClick: (Long) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit = { _, _ -> },
    onBulkMoveToFolder: (ids: List<Long>, folderName: String) -> Unit = { _, _ -> },
    onBulkAddTag: (ids: List<Long>, tag: String) -> Unit = { _, _ -> },
    onBulkDelete: (ids: List<Long>) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var isBulkMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    var showMoveFolderModal by remember { mutableStateOf(false) }
    var showAddTagModal by remember { mutableStateOf(false) }
    var targetFolderName by remember { mutableStateOf("General") }
    var newTagInput by remember { mutableStateOf("") }

    // Dropdown Expansion States
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }
    var isFamilyDropdownOpen by remember { mutableStateOf(false) }
    var isFormatDropdownOpen by remember { mutableStateOf(false) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    val isAnyFilterActive = selectedCategory != null ||
            selectedFamilyMember != null ||
            selectedFileType != null ||
            showOnlyExpiring ||
            showOnlyFavorites ||
            searchQuery.isNotBlank()

    fun resetAllFilters() {
        onSearchQueryChange("")
        onCategorySelect(null)
        onFamilyMemberSelect(null)
        onFileTypeSelect(null)
        onToggleExpiring(false)
        onToggleFavorites(false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBulkMode) "${selectedIds.size} Selected" else "Document Explorer",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (!isBulkMode) {
                            Text(
                                text = "${documents.size} document${if (documents.size != 1) "s" else ""} available",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isBulkMode) {
                        IconButton(
                            onClick = {
                                isBulkMode = false
                                selectedIds.clear()
                            },
                            modifier = Modifier.testTag("btn_close_bulk_mode")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Selection"
                            )
                        }
                    } else {
                        // Navigation Drawer Menu Button (Back arrow removed as requested)
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("btn_hamburger_docs")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (isAnyFilterActive && !isBulkMode) {
                        IconButton(
                            onClick = { resetAllFilters() },
                            modifier = Modifier.testTag("btn_quick_reset_filters")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Filters",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            isBulkMode = !isBulkMode
                            if (!isBulkMode) selectedIds.clear()
                        },
                        modifier = Modifier.testTag("btn_toggle_bulk_mode")
                    ) {
                        Text(
                            text = if (isBulkMode) "Done" else "Select",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = isBulkMode && selectedIds.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { showMoveFolderModal = true }
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Move Folder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { showAddTagModal = true }
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Add Tag", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    val copy = selectedIds.toList()
                                    onBulkDelete(copy)
                                    selectedIds.clear()
                                    isBulkMode = false
                                }
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Modern Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("input_explorer_search"),
                placeholder = {
                    Text(
                        text = "Search by name, ID number, notes, tags...",
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Modern Dropdowns & Filter Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Category Dropdown Button
                ExplorerDropdownPill(
                    icon = Icons.Default.Category,
                    label = selectedCategory ?: "All Categories",
                    isActive = selectedCategory != null,
                    isOpen = isCategoryDropdownOpen,
                    onClick = { isCategoryDropdownOpen = true }
                ) {
                    DropdownMenu(
                        expanded = isCategoryDropdownOpen,
                        onDismissRequest = { isCategoryDropdownOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "All Categories",
                                        fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            trailingIcon = {
                                if (selectedCategory == null) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = {
                                onCategorySelect(null)
                                isCategoryDropdownOpen = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        categoriesList.forEach { cat ->
                            val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                },
                                onClick = {
                                    onCategorySelect(if (isSelected) null else cat)
                                    isCategoryDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // 2. Family Member Dropdown Button
                val effectiveFamilyList = remember(familyMembersList) {
                    val base = listOf("Self", "Spouse", "Father", "Mother", "Child", "Business")
                    (base + familyMembersList).distinct()
                }

                ExplorerDropdownPill(
                    icon = Icons.Default.Person,
                    label = if (selectedFamilyMember != null) "👤 $selectedFamilyMember" else "All Family",
                    isActive = selectedFamilyMember != null,
                    isOpen = isFamilyDropdownOpen,
                    onClick = { isFamilyDropdownOpen = true }
                ) {
                    DropdownMenu(
                        expanded = isFamilyDropdownOpen,
                        onDismissRequest = { isFamilyDropdownOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FamilyRestroom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "All Family Profiles",
                                        fontWeight = if (selectedFamilyMember == null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedFamilyMember == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            trailingIcon = {
                                if (selectedFamilyMember == null) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = {
                                onFamilyMemberSelect(null)
                                isFamilyDropdownOpen = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        effectiveFamilyList.forEach { member ->
                            val isSelected = selectedFamilyMember.equals(member, ignoreCase = true)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "👤 $member",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                },
                                onClick = {
                                    onFamilyMemberSelect(if (isSelected) null else member)
                                    isFamilyDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // 3. File Format Dropdown Button
                val formatLabel = when (selectedFileType) {
                    "PDF" -> "PDFs Only"
                    "IMAGE" -> "Images Only"
                    else -> "All Formats"
                }

                ExplorerDropdownPill(
                    icon = Icons.Default.FilterList,
                    label = formatLabel,
                    isActive = selectedFileType != null,
                    isOpen = isFormatDropdownOpen,
                    onClick = { isFormatDropdownOpen = true }
                ) {
                    DropdownMenu(
                        expanded = isFormatDropdownOpen,
                        onDismissRequest = { isFormatDropdownOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "All Formats (PDF & Images)",
                                    fontWeight = if (selectedFileType == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (selectedFileType == null) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = {
                                onFileTypeSelect(null)
                                isFormatDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "📄 PDF Documents Only",
                                    fontWeight = if (selectedFileType == "PDF") FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (selectedFileType == "PDF") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = {
                                onFileTypeSelect("PDF")
                                isFormatDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "🖼️ Photos & Images Only",
                                    fontWeight = if (selectedFileType == "IMAGE") FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (selectedFileType == "IMAGE") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            },
                            onClick = {
                                onFileTypeSelect("IMAGE")
                                isFormatDropdownOpen = false
                            }
                        )
                    }
                }

                // 4. Quick Action Pill: Favorites (★)
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { onToggleFavorites(!showOnlyFavorites) },
                    label = {
                        Text(
                            text = "Favorites",
                            fontSize = 12.5.sp,
                            fontWeight = if (showOnlyFavorites) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (showOnlyFavorites) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF78350F).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFFD97706)
                    ),
                    modifier = Modifier.height(38.dp)
                )

                // 5. Quick Action Pill: Expiring Soon
                FilterChip(
                    selected = showOnlyExpiring,
                    onClick = { onToggleExpiring(!showOnlyExpiring) },
                    label = {
                        Text(
                            text = "Expiring Soon",
                            fontSize = 12.5.sp,
                            fontWeight = if (showOnlyExpiring) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (showOnlyExpiring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        selectedLabelColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.height(38.dp)
                )
            }

            // Active Filters Ribbon Bar
            if (isAnyFilterActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Filtered View",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextButton(
                        onClick = { resetAllFilters() },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Reset All", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bulk Mode Action Row or Selection Count
            if (isBulkMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap documents to select",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = {
                            if (selectedIds.size == documents.size) {
                                selectedIds.clear()
                            } else {
                                selectedIds.clear()
                                selectedIds.addAll(documents.map { it.id })
                            }
                        }
                    ) {
                        Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedIds.size == documents.size) "Deselect All" else "Select All", fontSize = 12.sp)
                    }
                }
            }

            // Document List or Empty View
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAnyFilterActive) Icons.Default.FilterAltOff else Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isAnyFilterActive) "No Documents Match Filter" else "No Documents Found",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAnyFilterActive)
                                "No items match your active filters or search keyword."
                            else "Add your first document to encrypt and store safely.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )

                        if (isAnyFilterActive) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { resetAllFilters() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear All Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        val isSelected = selectedIds.contains(doc.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isBulkMode) {
                                IconButton(
                                    onClick = {
                                        if (isSelected) selectedIds.remove(doc.id) else selectedIds.add(doc.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Select",
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DocumentCard(
                                    document = doc,
                                    onClick = {
                                        if (isBulkMode) {
                                            if (isSelected) selectedIds.remove(doc.id) else selectedIds.add(doc.id)
                                        } else {
                                            onDocumentClick(doc.id)
                                        }
                                    },
                                    onToggleFavorite = onToggleFavorite
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMoveFolderModal) {
        AlertDialog(
            onDismissRequest = { showMoveFolderModal = false },
            title = { Text("Move ${selectedIds.size} Documents", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select target folder:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    val availableFolders = if (foldersList.isEmpty()) listOf("General", "Personal ID", "Financial & Tax", "Medical & Health") else foldersList.map { it.name }
                    availableFolders.forEach { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetFolderName = folder }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (targetFolderName == folder) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (targetFolderName == folder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = folder, fontWeight = if (targetFolderName == folder) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBulkMoveToFolder(selectedIds.toList(), targetFolderName)
                        showMoveFolderModal = false
                        selectedIds.clear()
                        isBulkMode = false
                    }
                ) {
                    Text("Move Documents")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveFolderModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddTagModal) {
        AlertDialog(
            onDismissRequest = { showAddTagModal = false },
            title = { Text("Add Tag to ${selectedIds.size} Documents", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter tag name:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        placeholder = { Text("e.g. Verified2025") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagInput.isNotBlank()) {
                            onBulkAddTag(selectedIds.toList(), newTagInput.trim())
                            newTagInput = ""
                            showAddTagModal = false
                            selectedIds.clear()
                            isBulkMode = false
                        }
                    }
                ) {
                    Text("Apply Tag")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExplorerDropdownPill(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isOpen: Boolean,
    onClick: () -> Unit,
    dropdownMenu: @Composable () -> Unit
) {
    Box {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
            ),
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.5.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        dropdownMenu()
    }
}
