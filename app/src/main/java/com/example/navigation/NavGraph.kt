package com.example.navigation

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.components.TopToastBanner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.ui.screens.AboutAppScreen
import com.example.ui.screens.AboutDeveloperScreen
import com.example.ui.screens.AddDocumentScreen
import com.example.ui.screens.BackupRestoreScreen
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentDetailScreen
import com.example.ui.screens.DocumentListScreen
import com.example.ui.screens.FamilyProfilesScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.ImageViewerScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.PdfViewerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashBinScreen
import com.example.viewmodel.VaultViewModel
import kotlinx.coroutines.launch
import java.util.Locale

object Routes {
    const val LOCK = "lock"
    const val DASHBOARD = "dashboard"
    const val DOCUMENT_LIST = "document_list"
    const val ADD_DOCUMENT = "add_document?category={category}&familyMember={familyMember}&folder={folder}"
    const val DOCUMENT_DETAIL = "document_detail/{documentId}"
    const val PDF_VIEWER = "pdf_viewer/{documentId}"
    const val IMAGE_VIEWER = "image_viewer/{documentId}"
    const val CAMERA_CAPTURE = "camera_capture"
    const val BACKUP_RESTORE = "backup_restore"
    const val TRASH_BIN = "trash_bin"
    const val SETTINGS = "settings"
    const val FOLDERS = "folders"
    const val FAMILY_PROFILES = "family_profiles"
    const val ABOUT_APP = "about_app"
    const val ABOUT_DEVELOPER = "about_developer"

    fun addDocument(
        category: String? = null,
        familyMember: String? = null,
        folder: String? = null
    ): String {
        val queryParams = mutableListOf<String>()
        if (!category.isNullOrBlank()) queryParams.add("category=${Uri.encode(category)}")
        if (!familyMember.isNullOrBlank()) queryParams.add("familyMember=${Uri.encode(familyMember)}")
        if (!folder.isNullOrBlank()) queryParams.add("folder=${Uri.encode(folder)}")
        return if (queryParams.isEmpty()) "add_document" else "add_document?${queryParams.joinToString("&")}"
    }

    fun addDocumentWithCategory(category: String): String = addDocument(category = category)
    fun documentDetail(id: Long): String = "document_detail/$id"
    fun pdfViewer(id: Long): String = "pdf_viewer/$id"
    fun imageViewer(id: Long): String = "image_viewer/$id"
}

@Composable
fun NavGraph(
    viewModel: VaultViewModel,
    navController: NavHostController = rememberNavController()
) {
    val isVaultLocked by viewModel.isVaultLocked.collectAsState()
    val isPinSet by viewModel.isPinSet.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    val filteredDocs by viewModel.filteredDocuments.collectAsState()
    val allDocs by viewModel.allDocumentsList.collectAsState()
    val recentDocs by viewModel.recentDocuments.collectAsState()
    val totalDocCount by viewModel.totalDocumentCount.collectAsState()
    val totalStorageBytes by viewModel.totalStorageUsed.collectAsState()
    val expiringCount by viewModel.expiringDocumentCount.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedFileType by viewModel.selectedFileType.collectAsState()
    val showOnlyExpiring by viewModel.showOnlyExpiring.collectAsState()
    val selectedFamilyMember by viewModel.selectedFamilyMember.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()
    val trashedDocs by viewModel.trashedDocuments.collectAsState()

    val categoriesList by viewModel.categoriesList.collectAsState()
    val familyMembersList by viewModel.familyMembersList.collectAsState()
    val foldersList by viewModel.foldersList.collectAsState()
    val pendingCameraBytes by viewModel.pendingCameraImageBytes.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val uiMessage by viewModel.uiMessage.collectAsState()
    var activeToastMessage by remember { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isVaultLocked) {
        if (drawerState.isOpen) {
            drawerState.snapTo(DrawerValue.Closed)
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            activeToastMessage = msg
            viewModel.clearUiMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isVaultLocked) {
            LockScreen(
                isPinSet = isPinSet,
                isBiometricEnabled = isBiometricEnabled,
                onPinEntered = { pin, callback ->
                    viewModel.unlockVaultWithPin(pin, callback)
                },
                onPinSetup = { pin, callback ->
                    viewModel.setupNewPin(pin, callback)
                },
                onBiometricClick = {
                    val activity = context as? androidx.fragment.app.FragmentActivity
                    if (activity != null) {
                        com.example.ui.security.BiometricPromptHelper.showPrompt(
                            activity = activity,
                            onSuccess = { viewModel.unlockWithBiometrics() },
                            onError = { err -> viewModel.setUiMessage(err) }
                        )
                    } else {
                        viewModel.unlockWithBiometrics()
                    }
                },
                onBiometricResetClick = { onVerified ->
                    val activity = context as? androidx.fragment.app.FragmentActivity
                    if (activity != null) {
                        com.example.ui.security.BiometricPromptHelper.showPrompt(
                            activity = activity,
                            title = "Reset Master PIN",
                            subtitle = "Verify your biometric identity to set a new vault PIN",
                            onSuccess = { onVerified(true) },
                            onError = { err ->
                                viewModel.setUiMessage(err)
                                onVerified(false)
                            }
                        )
                    } else {
                        onVerified(false)
                    }
                },
                onPinReset = { newPin, callback ->
                    viewModel.resetPin(newPin) { success ->
                        if (success) {
                            viewModel.unlockWithBiometrics()
                        }
                        callback(success)
                    }
                }
            )
        } else {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val topLevelRoutes = listOf(
                Routes.DASHBOARD,
                Routes.FOLDERS,
                Routes.FAMILY_PROFILES,
                Routes.DOCUMENT_LIST
            )

            val shouldShowBottomBar = currentRoute in topLevelRoutes

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = shouldShowBottomBar && !isVaultLocked,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerTonalElevation = 0.dp,
                        windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.width(310.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                                .statusBarsPadding()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 24.dp)
                        ) {
                            // Drawer Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_dastavej_logo),
                                            contentDescription = "Dastavej Box Logo",
                                            modifier = Modifier.size(52.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "DASTAVEJ BOX",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Offline Security Vault",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "$totalDocCount Docs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                val mbStr = String.format(Locale.US, "%.1f MB", (totalStorageBytes ?: 0L) / (1024.0 * 1024.0))
                                                Text(text = mbStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "VAULT NAVIGATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                label = { Text("Dashboard", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.DASHBOARD,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    if (currentRoute != Routes.DASHBOARD) {
                                        navController.navigate(Routes.DASHBOARD) {
                                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                label = { Text("Folders Hub", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.FOLDERS,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    if (currentRoute != Routes.FOLDERS) {
                                        viewModel.selectedFolder.value = null
                                        navController.navigate(Routes.FOLDERS) {
                                            popUpTo(Routes.DASHBOARD)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text("Family Profiles", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.FAMILY_PROFILES,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    if (currentRoute != Routes.FAMILY_PROFILES) {
                                        viewModel.selectedFamilyMember.value = null
                                        navController.navigate(Routes.FAMILY_PROFILES) {
                                            popUpTo(Routes.DASHBOARD)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Description, contentDescription = null) },
                                label = { Text("Document Explorer", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.DOCUMENT_LIST,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    if (currentRoute != Routes.DOCUMENT_LIST) {
                                        viewModel.searchQuery.value = ""
                                        viewModel.selectedCategory.value = null
                                        viewModel.selectedFileType.value = null
                                        viewModel.selectedFolder.value = null
                                        viewModel.selectedFamilyMember.value = null
                                        viewModel.showOnlyExpiring.value = false
                                        viewModel.showOnlyFavorites.value = false
                                        navController.navigate(Routes.DOCUMENT_LIST) {
                                            popUpTo(Routes.DASHBOARD)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                label = { Text("Add New Document", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.ADD_DOCUMENT,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate(Routes.ADD_DOCUMENT)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "VAULT TOOLS & SECURITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                label = { Text("Favorites", fontWeight = FontWeight.SemiBold) },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    viewModel.showOnlyFavorites.value = true
                                    navController.navigate(Routes.DOCUMENT_LIST)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                label = { Text("Encrypted Backup & Restore", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.BACKUP_RESTORE,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate(Routes.BACKUP_RESTORE)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                label = { Text("Recycle Bin (${trashedDocs.size})", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.TRASH_BIN,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate(Routes.TRASH_BIN)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                label = { Text("Vault Settings", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.SETTINGS,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate(Routes.SETTINGS)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                label = { Text("About App", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.ABOUT_APP,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate(Routes.ABOUT_APP)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                label = { Text("About Developer", fontWeight = FontWeight.SemiBold) },
                                selected = currentRoute == Routes.ABOUT_DEVELOPER,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navController.navigate(Routes.ABOUT_DEVELOPER)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                label = { Text("Lock Vault Now", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    viewModel.lockVault()
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔒 256-Bit AES Encrypted • 100% Offline",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            ) {
                Scaffold(
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("sticky_bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == Routes.DASHBOARD,
                                    onClick = {
                                        if (currentRoute != Routes.DASHBOARD) {
                                            navController.navigate(Routes.DASHBOARD) {
                                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Dashboard"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Dashboard",
                                            fontWeight = if (currentRoute == Routes.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == Routes.FOLDERS,
                                    onClick = {
                                        if (currentRoute != Routes.FOLDERS) {
                                            viewModel.selectedFolder.value = null
                                            navController.navigate(Routes.FOLDERS) {
                                                popUpTo(Routes.DASHBOARD)
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = "Folders Hub"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Folders",
                                            fontWeight = if (currentRoute == Routes.FOLDERS) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == Routes.FAMILY_PROFILES,
                                    onClick = {
                                        if (currentRoute != Routes.FAMILY_PROFILES) {
                                            viewModel.selectedFamilyMember.value = null
                                            navController.navigate(Routes.FAMILY_PROFILES) {
                                                popUpTo(Routes.DASHBOARD)
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Family Profiles"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Family",
                                            fontWeight = if (currentRoute == Routes.FAMILY_PROFILES) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == Routes.DOCUMENT_LIST,
                                    onClick = {
                                        if (currentRoute != Routes.DOCUMENT_LIST) {
                                            viewModel.searchQuery.value = ""
                                            viewModel.selectedCategory.value = null
                                            viewModel.selectedFileType.value = null
                                            viewModel.selectedFolder.value = null
                                            viewModel.selectedFamilyMember.value = null
                                            viewModel.showOnlyExpiring.value = false
                                            viewModel.showOnlyFavorites.value = false
                                            navController.navigate(Routes.DOCUMENT_LIST) {
                                                popUpTo(Routes.DASHBOARD)
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = "Document Explorer"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Explorer",
                                            fontWeight = if (currentRoute == Routes.DOCUMENT_LIST) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.DASHBOARD
                        ) {
                            composable(Routes.DASHBOARD) {
                                DashboardScreen(
                                    totalDocuments = totalDocCount,
                                    totalStorageBytes = totalStorageBytes ?: 0L,
                                    expiringCount = expiringCount,
                                    favoritesCount = filteredDocs.count { it.isFavorite },
                                    trashedCount = trashedDocs.size,
                                    recentDocuments = recentDocs,
                                    allDocuments = filteredDocs,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onSearchClick = {
                                        viewModel.searchQuery.value = ""
                                        navController.navigate(Routes.DOCUMENT_LIST)
                                    },
                                    onCategoryClick = { category ->
                                        viewModel.selectedCategory.value = category
                                        navController.navigate(Routes.DOCUMENT_LIST)
                                    },
                                    onFavoritesClick = {
                                        viewModel.showOnlyFavorites.value = true
                                        navController.navigate(Routes.DOCUMENT_LIST)
                                    },
                                    onTrashBinClick = {
                                        navController.navigate(Routes.TRASH_BIN)
                                    },
                                    onBackupRestoreClick = {
                                        navController.navigate(Routes.BACKUP_RESTORE)
                                    },
                                    onFoldersClick = {
                                        navController.navigate(Routes.FOLDERS)
                                    },
                                    onFamilyProfilesClick = {
                                        navController.navigate(Routes.FAMILY_PROFILES)
                                    },
                                    onDocumentClick = { id ->
                                        navController.navigate(Routes.documentDetail(id))
                                    },
                                    onToggleFavorite = { id, fav ->
                                        viewModel.toggleFavorite(id, fav)
                                    },
                                    onAddDocumentClick = {
                                        navController.navigate(Routes.ADD_DOCUMENT)
                                    },
                                    onLockClick = {
                                        viewModel.lockVault()
                                    },
                                    onSettingsClick = {
                                        navController.navigate(Routes.SETTINGS)
                                    },
                                    onExpiringBannerClick = {
                                        viewModel.showOnlyExpiring.value = true
                                        navController.navigate(Routes.DOCUMENT_LIST)
                                    }
                                )
                            }

                            composable(Routes.DOCUMENT_LIST) {
                                DocumentListScreen(
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                                    selectedCategory = selectedCategory,
                                    onCategorySelect = { viewModel.selectedCategory.value = it },
                                    selectedFileType = selectedFileType,
                                    onFileTypeSelect = { viewModel.selectedFileType.value = it },
                                    showOnlyExpiring = showOnlyExpiring,
                                    onToggleExpiring = { viewModel.showOnlyExpiring.value = it },
                                    selectedFamilyMember = selectedFamilyMember,
                                    onFamilyMemberSelect = { viewModel.selectedFamilyMember.value = it },
                                    showOnlyFavorites = showOnlyFavorites,
                                    onToggleFavorites = { viewModel.showOnlyFavorites.value = it },
                                    categoriesList = categoriesList,
                                    familyMembersList = familyMembersList,
                                    foldersList = foldersList,
                                    documents = filteredDocs,
                                    onDocumentClick = { id ->
                                        navController.navigate(Routes.documentDetail(id))
                                    },
                                    onToggleFavorite = { id, fav ->
                                        viewModel.toggleFavorite(id, fav)
                                    },
                                    onBulkMoveToFolder = { ids, folderName ->
                                        viewModel.bulkMoveToFolder(ids, folderName)
                                    },
                                    onBulkAddTag = { ids, tag ->
                                        viewModel.bulkAddTag(ids, tag)
                                    },
                                    onBulkDelete = { ids ->
                                        viewModel.bulkDeletePermanent(ids)
                                    },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(
                                route = Routes.ADD_DOCUMENT,
                                arguments = listOf(
                                    navArgument("category") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    },
                                    navArgument("familyMember") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    },
                                    navArgument("folder") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { backStackEntry ->
                                val categoryArg = backStackEntry.arguments?.getString("category")
                                val familyMemberArg = backStackEntry.arguments?.getString("familyMember")
                                val folderArg = backStackEntry.arguments?.getString("folder")
                                AddDocumentScreen(
                                    initialCategory = categoryArg,
                                    initialFamilyMember = familyMemberArg,
                                    initialFolder = folderArg,
                                    capturedImageBytes = pendingCameraBytes,
                                    onClearCapturedImage = { viewModel.clearPendingCameraImageBytes() },
                                    categoriesList = categoriesList,
                                    familyMembersList = familyMembersList,
                                    foldersList = foldersList,
                                    onCreateCategory = { viewModel.createCategory(it) },
                                    onCreateFamilyMember = { viewModel.addFamilyMember(it) },
                                    onCreateFolder = { viewModel.createFolder(it) },
                                    onMarkPickerActive = { viewModel.markSystemPickerActive() },
                                    onImportFile = { uri, title, cat, isPdf, expiry, issueDate, tags, notes, accNum, issuer, familyMember, folderName ->
                                        viewModel.importDocument(
                                            uri = uri,
                                            title = title,
                                            category = cat,
                                            isPdf = isPdf,
                                            expiryDate = expiry,
                                            issueDate = issueDate,
                                            tags = tags,
                                            notes = notes,
                                            accountNumber = accNum,
                                            issuer = issuer,
                                            familyMember = familyMember,
                                            folderName = folderName
                                        ) { success ->
                                            if (success) navController.popBackStack()
                                        }
                                    },
                                    onImportCameraBytes = { bytes, title, cat, expiry, issueDate, tags, notes, accNum, issuer, familyMember, folderName ->
                                        viewModel.addDocumentFromCamera(
                                            imageBytes = bytes,
                                            title = title,
                                            category = cat,
                                            expiryDate = expiry,
                                            issueDate = issueDate,
                                            tags = tags,
                                            notes = notes,
                                            accountNumber = accNum,
                                            issuer = issuer,
                                            familyMember = familyMember,
                                            folderName = folderName
                                        ) { success ->
                                            if (success) {
                                                viewModel.clearPendingCameraImageBytes()
                                                navController.popBackStack()
                                            }
                                        }
                                    },
                                    onLaunchCamera = {
                                        navController.navigate(Routes.CAMERA_CAPTURE)
                                    },
                                    onBackClick = {
                                        viewModel.clearPendingCameraImageBytes()
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(Routes.CAMERA_CAPTURE) {
                                CameraCaptureScreen(
                                    onImageCaptured = { bytes ->
                                        viewModel.setPendingCameraImageBytes(bytes)
                                        navController.popBackStack()
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(
                                route = Routes.DOCUMENT_DETAIL,
                                arguments = listOf(navArgument("documentId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val docId = backStackEntry.arguments?.getLong("documentId") ?: 0L
                                val doc = filteredDocs.find { it.id == docId } ?: recentDocs.find { it.id == docId }

                                LaunchedEffect(docId) {
                                    viewModel.markOpened(docId)
                                }

                                if (doc != null) {
                                    DocumentDetailScreen(
                                        document = doc,
                                        categoriesList = categoriesList,
                                        familyMembersList = familyMembersList,
                                        foldersList = foldersList,
                                        onOpenViewer = {
                                            if (doc.fileType == "PDF") {
                                                navController.navigate(Routes.pdfViewer(doc.id))
                                            } else {
                                                navController.navigate(Routes.imageViewer(doc.id))
                                            }
                                        },
                                        onEditDocument = { updatedDoc ->
                                            viewModel.updateDocument(
                                                id = updatedDoc.id,
                                                title = updatedDoc.title,
                                                category = updatedDoc.category,
                                                expiryDate = updatedDoc.expiryDate,
                                                issueDate = updatedDoc.issueDate,
                                                tags = updatedDoc.tags,
                                                notes = updatedDoc.notes,
                                                accountNumber = updatedDoc.accountNumber,
                                                issuer = updatedDoc.issuer,
                                                familyMember = updatedDoc.familyMember,
                                                folderName = updatedDoc.folderName
                                            ) {}
                                        },
                                        onToggleFavorite = { id, fav ->
                                            viewModel.toggleFavorite(id, fav)
                                        },
                                        onConvertToPdf = { id ->
                                            viewModel.convertImageToPdf(id) {}
                                        },
                                        onLogRenewal = { id, note, newExpiry ->
                                            viewModel.addRenewalRecord(id, note, newExpiry) {}
                                        },
                                        onDeleteDocument = {
                                            viewModel.moveToTrash(doc.id) {
                                                navController.popBackStack()
                                            }
                                        },
                                        onBackClick = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }

                            composable(
                                route = Routes.PDF_VIEWER,
                                arguments = listOf(navArgument("documentId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val docId = backStackEntry.arguments?.getLong("documentId") ?: 0L
                                val doc = filteredDocs.find { it.id == docId } ?: recentDocs.find { it.id == docId }

                                if (doc != null) {
                                    val pageCount = viewModel.repository.getPdfPageCount(doc.filePath)
                                    PdfViewerScreen(
                                        title = doc.title,
                                        filePath = doc.filePath,
                                        pageCount = pageCount,
                                        onRenderPage = { index, width ->
                                            viewModel.repository.renderPdfPage(doc.filePath, index, width)
                                        },
                                        onBackClick = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }

                            composable(
                                route = Routes.IMAGE_VIEWER,
                                arguments = listOf(navArgument("documentId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val docId = backStackEntry.arguments?.getLong("documentId") ?: 0L
                                val doc = filteredDocs.find { it.id == docId } ?: recentDocs.find { it.id == docId }

                                if (doc != null) {
                                    ImageViewerScreen(
                                        title = doc.title,
                                        filePath = doc.filePath,
                                        onBackClick = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }

                            composable(Routes.BACKUP_RESTORE) {
                                BackupRestoreScreen(
                                    onExportBackup = { passphrase, file, callback ->
                                        viewModel.exportEncryptedBackup(passphrase, file, callback)
                                    },
                                    onImportBackup = { file, passphrase, callback ->
                                        viewModel.importEncryptedBackup(file, passphrase, callback)
                                    },
                                    onMarkPickerActive = {
                                        viewModel.markSystemPickerActive()
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(Routes.TRASH_BIN) {
                                val trashedDocs by viewModel.trashedDocuments.collectAsState()
                                TrashBinScreen(
                                    trashedDocuments = trashedDocs,
                                    onRestoreDocument = { id -> viewModel.restoreFromTrash(id) },
                                    onPermanentDelete = { id -> viewModel.bulkDeletePermanent(listOf(id)) },
                                    onEmptyTrash = { viewModel.emptyTrash() },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.FOLDERS) {
                                FoldersScreen(
                                    folders = foldersList,
                                    allDocuments = allDocs,
                                    onCreateFolder = { name, colorHex, description ->
                                        viewModel.createFolder(name, colorHex, "All", description)
                                    },
                                    onEditFolder = { id, name, colorHex, description ->
                                        viewModel.editFolder(id, name, colorHex, description)
                                    },
                                    onDeleteFolder = { id, folderName ->
                                        viewModel.deleteFolder(id, folderName)
                                    },
                                    onMoveDocumentToFolder = { docId, targetFolder ->
                                        viewModel.moveDocumentToFolder(docId, targetFolder)
                                    },
                                    onCopyDocumentToFolder = { docId, targetFolder ->
                                        viewModel.copyDocumentToFolder(docId, targetFolder)
                                    },
                                    onDocumentClick = { id ->
                                        navController.navigate(Routes.documentDetail(id))
                                    },
                                    onAddDocumentToFolder = { folderName ->
                                        navController.navigate(Routes.addDocument(folder = folderName))
                                    },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    }
                                )
                            }

                            composable(Routes.FAMILY_PROFILES) {
                                FamilyProfilesScreen(
                                    familyMembers = familyMembersList,
                                    allDocuments = allDocs,
                                    onAddFamilyMember = { member ->
                                        viewModel.addFamilyMember(member)
                                    },
                                    onAddDocumentForMember = { member ->
                                        navController.navigate(Routes.addDocument(familyMember = member))
                                    },
                                    onDocumentClick = { id ->
                                        navController.navigate(Routes.documentDetail(id))
                                    },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(Routes.SETTINGS) {
                                SettingsScreen(
                                    isBiometricEnabled = isBiometricEnabled,
                                    onToggleBiometrics = { viewModel.setBiometricEnabled(it) },
                                    totalDocuments = totalDocCount,
                                    totalStorageBytes = totalStorageBytes ?: 0L,
                                    categoriesList = categoriesList,
                                    familyMembersList = familyMembersList,
                                    onAddCategory = { viewModel.addCategory(it) },
                                    onRemoveCategory = { viewModel.removeCategory(it) },
                                    onAddFamilyMember = { viewModel.addFamilyMember(it) },
                                    onRemoveFamilyMember = { viewModel.removeFamilyMember(it) },
                                    onVerifyOldPin = { oldPin, callback ->
                                        viewModel.verifyOldPin(oldPin, callback)
                                    },
                                    onResetPin = { newPin, callback ->
                                        viewModel.resetPin(newPin, callback)
                                    },
                                    onBiometricAuthForReset = { onVerified ->
                                        val activity = context as? androidx.fragment.app.FragmentActivity
                                        if (activity != null) {
                                            com.example.ui.security.BiometricPromptHelper.showPrompt(
                                                activity = activity,
                                                title = "Reset Master PIN",
                                                subtitle = "Verify biometric authentication to change vault PIN",
                                                onSuccess = { onVerified(true) },
                                                onError = { err ->
                                                    viewModel.setUiMessage(err)
                                                    onVerified(false)
                                                }
                                            )
                                        } else {
                                            onVerified(false)
                                        }
                                    },
                                    onOpenBackupRestore = {
                                        navController.navigate(Routes.BACKUP_RESTORE)
                                    },
                                    onOpenAboutApp = {
                                        navController.navigate(Routes.ABOUT_APP)
                                    },
                                    onOpenAboutDeveloper = {
                                        navController.navigate(Routes.ABOUT_DEVELOPER)
                                    },
                                    onLockVault = {
                                        viewModel.lockVault()
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(Routes.ABOUT_APP) {
                                AboutAppScreen(
                                    onNavigateToDeveloper = {
                                        navController.navigate(Routes.ABOUT_DEVELOPER)
                                    },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(Routes.ABOUT_DEVELOPER) {
                                AboutDeveloperScreen(
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        TopToastBanner(
            message = activeToastMessage,
            onDismiss = { activeToastMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
