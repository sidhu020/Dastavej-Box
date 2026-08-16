package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.DocumentEntity
import com.example.data.notification.NotificationScheduler
import com.example.data.ocr.OnDeviceOcrEngine
import com.example.data.repository.DocumentRepository
import com.example.data.security.VaultSecurityManager
import com.example.data.storage.FileStorageManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val storageManager = FileStorageManager(application)
    private val ocrEngine = OnDeviceOcrEngine(application)
    private val notificationScheduler = NotificationScheduler(application)

    val repository = DocumentRepository(
        documentDao = db.documentDao(),
        folderDao = db.folderDao(),
        fileStorageManager = storageManager,
        ocrEngine = ocrEngine,
        notificationScheduler = notificationScheduler
    )

    val securityManager = VaultSecurityManager(application)

    init {
        // Initialize default folders if empty
        viewModelScope.launch {
            repository.createFolder("General", "#3F51B5", "All", "Default documents folder")
            repository.createFolder("Personal ID", "#4CAF50", "All", "Aadhaar, Passport, PAN, Licenses")
            repository.createFolder("Financial & Tax", "#FF9800", "All", "Bank statements, Tax returns, Bills")
            repository.createFolder("Medical & Health", "#E91E63", "All", "Prescriptions, Health insurance, Reports")
            repository.createFolder("Property & Vehicle", "#9C27B0", "All", "Vehicle RC, Property deeds, Agreements")
            repository.createFolder("Education & Career", "#00BCD4", "All", "Certificates, Marksheets, Resumes")
        }
    }

    // Security State
    private val _isVaultLocked = MutableStateFlow(true)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    val isLaunchingSystemPicker = MutableStateFlow(false)

    val isPinSet: StateFlow<Boolean> = securityManager.isPinSet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBiometricEnabled: StateFlow<Boolean> = securityManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoLockMinutes: StateFlow<String> = securityManager.autoLockMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1")

    val categoriesList: StateFlow<List<String>> = securityManager.categoriesList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.ui.components.DEFAULT_CATEGORIES)

    val familyMembersList: StateFlow<List<String>> = securityManager.familyMembersList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Self", "Spouse", "Father", "Mother", "Child", "Business"))

    val foldersList: StateFlow<List<com.example.data.database.FolderEntity>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter & Search State
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null) // null means "All"
    val selectedFileType = MutableStateFlow<String?>(null) // null means "All", or "PDF"/"IMAGE"
    val selectedFamilyMember = MutableStateFlow<String?>(null) // null means "All", e.g. "Self", "Spouse"
    val selectedFolder = MutableStateFlow<String?>(null) // null means "All"
    val showOnlyExpiring = MutableStateFlow(false)
    val showOnlyFavorites = MutableStateFlow(false)

    // UI Feedback Message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredDocuments: StateFlow<List<DocumentEntity>> = combine(
        searchQuery,
        selectedCategory,
        selectedFileType,
        selectedFamilyMember,
        selectedFolder,
        showOnlyExpiring,
        showOnlyFavorites
    ) { flows ->
        FilterState(
            query = flows[0] as String,
            category = flows[1] as String?,
            fileType = flows[2] as String?,
            member = flows[3] as String?,
            folder = flows[4] as String?,
            showExpiring = flows[5] as Boolean,
            favs = flows[6] as Boolean
        )
    }.flatMapLatest { filter ->
        if (filter.favs) {
            repository.favoriteDocuments
        } else if (filter.query.isNotBlank()) {
            repository.searchDocuments(filter.query)
        } else if (filter.folder != null) {
            repository.getDocumentsByFolder(filter.folder)
        } else if (filter.category != null) {
            repository.getDocumentsByCategory(filter.category)
        } else if (filter.member != null) {
            repository.getDocumentsByFamilyMember(filter.member)
        } else if (filter.fileType != null) {
            repository.getDocumentsByFileType(filter.fileType)
        } else if (filter.showExpiring) {
            val thirtyDaysMillis = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
            repository.getExpiringDocuments(thirtyDaysMillis)
        } else {
            repository.allDocuments
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocumentsList: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDocuments: StateFlow<List<DocumentEntity>> = repository.recentDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyOpenedDocuments: StateFlow<List<DocumentEntity>> = repository.recentlyOpenedDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<DocumentEntity>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedDocuments: StateFlow<List<DocumentEntity>> = repository.trashedDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDocumentCount: StateFlow<Int> = repository.totalDocumentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStorageUsed: StateFlow<Long?> = repository.totalStorageUsed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val expiringDocumentCount: StateFlow<Int> = repository.allDocuments.combine(searchQuery) { docs, _ ->
        val thirtyDaysMillis = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        docs.count { doc ->
            doc.expiryDate != null && doc.expiryDate <= thirtyDaysMillis
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _pendingCameraImageBytes = MutableStateFlow<ByteArray?>(null)
    val pendingCameraImageBytes: StateFlow<ByteArray?> = _pendingCameraImageBytes.asStateFlow()

    fun setPendingCameraImageBytes(bytes: ByteArray) {
        _pendingCameraImageBytes.value = bytes
    }

    fun clearPendingCameraImageBytes() {
        _pendingCameraImageBytes.value = null
    }

    fun createCategory(categoryName: String) {
        viewModelScope.launch {
            securityManager.addCategory(categoryName)
            _uiMessage.value = "Category '$categoryName' added"
        }
    }

    fun unlockVaultWithPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isValid = securityManager.verifyPin(pin)
            if (isValid) {
                _isVaultLocked.value = false
            }
            onResult(isValid)
        }
    }

    fun setupNewPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = securityManager.setPin(pin)
            if (success) {
                _isVaultLocked.value = false
                _uiMessage.value = "Vault PIN created successfully"
            }
            onResult(success)
        }
    }

    fun verifyOldPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isValid = securityManager.verifyPin(pin)
            onResult(isValid)
        }
    }

    fun resetPin(newPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = securityManager.setPin(newPin)
            if (success) {
                _uiMessage.value = "Vault PIN updated successfully"
            }
            onResult(success)
        }
    }

    fun unlockWithBiometrics() {
        _isVaultLocked.value = false
    }

    fun lockVault() {
        _isVaultLocked.value = true
    }

    fun markSystemPickerActive() {
        isLaunchingSystemPicker.value = true
    }

    fun resetSystemPickerActive() {
        isLaunchingSystemPicker.value = false
    }

    fun setUiMessage(message: String) {
        _uiMessage.value = message
    }

    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            securityManager.addCategory(categoryName)
            _uiMessage.value = "Category '$categoryName' added"
        }
    }

    fun removeCategory(categoryName: String) {
        viewModelScope.launch {
            securityManager.removeCategory(categoryName)
            _uiMessage.value = "Category '$categoryName' removed"
        }
    }

    fun addFamilyMember(memberName: String) {
        viewModelScope.launch {
            securityManager.addFamilyMember(memberName)
            _uiMessage.value = "Family profile '$memberName' added"
        }
    }

    fun removeFamilyMember(memberName: String) {
        viewModelScope.launch {
            securityManager.removeFamilyMember(memberName)
            _uiMessage.value = "Family profile '$memberName' removed"
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securityManager.setBiometricEnabled(enabled)
            _uiMessage.value = if (enabled) "Biometric login enabled" else "Biometric login disabled"
        }
    }

    fun setAutoLockMinutes(minutes: String) {
        viewModelScope.launch {
            securityManager.setAutoLockMinutes(minutes)
        }
    }

    fun createFolder(name: String, colorHex: String = "#3F51B5", familyMember: String = "All", description: String = "") {
        viewModelScope.launch {
            repository.createFolder(name, colorHex, familyMember, description)
            _uiMessage.value = "Folder '$name' created"
        }
    }

    fun editFolder(id: Long, name: String, colorHex: String = "#3F51B5", description: String = "") {
        viewModelScope.launch {
            repository.editFolder(id, name, colorHex, description)
            _uiMessage.value = "Folder '$name' updated"
        }
    }

    fun moveDocumentToFolder(docId: Long, targetFolder: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.moveDocumentToFolder(docId, targetFolder)
            _uiMessage.value = "Moved document to folder '$targetFolder'"
            onComplete()
        }
    }

    fun copyDocumentToFolder(docId: Long, targetFolder: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val newId = repository.copyDocumentToFolder(docId, targetFolder)
            if (newId != null) {
                _uiMessage.value = "Copied document to folder '$targetFolder'"
            } else {
                _uiMessage.value = "Failed to copy document"
            }
            onComplete()
        }
    }

    fun deleteFolder(id: Long, folderName: String) {
        viewModelScope.launch {
            repository.deleteFolder(id, folderName)
            _uiMessage.value = "Folder '$folderName' deleted"
        }
    }

    fun bulkMoveToFolder(ids: List<Long>, targetFolder: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.bulkMoveToFolder(ids, targetFolder)
            _uiMessage.value = "${ids.size} documents moved to folder '$targetFolder'"
            onComplete()
        }
    }

    fun bulkAddTag(ids: List<Long>, newTag: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.bulkAddTag(ids, newTag)
            _uiMessage.value = "Tag '$newTag' added to ${ids.size} documents"
            onComplete()
        }
    }

    fun importDocument(
        uri: Uri,
        title: String,
        category: String,
        isPdf: Boolean,
        expiryDate: Long?,
        issueDate: Long? = null,
        tags: String,
        notes: String,
        accountNumber: String = "",
        issuer: String = "",
        familyMember: String = "Self",
        folderName: String = "General",
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.importDocument(
                    uri = uri,
                    title = title,
                    category = category,
                    isPdf = isPdf,
                    expiryDate = expiryDate,
                    issueDate = issueDate,
                    tags = tags,
                    notes = notes,
                    accountNumber = accountNumber,
                    issuer = issuer,
                    familyMember = familyMember,
                    folderName = folderName
                )
                _uiMessage.value = "Document imported securely into vault"
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiMessage.value = "Failed to import document: ${e.localizedMessage}"
                onComplete(false)
            }
        }
    }

    fun addDocumentFromCamera(
        imageBytes: ByteArray,
        title: String,
        category: String,
        expiryDate: Long?,
        issueDate: Long? = null,
        tags: String,
        notes: String,
        accountNumber: String = "",
        issuer: String = "",
        familyMember: String = "Self",
        folderName: String = "General",
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addDocumentFromCapturedImage(
                    imageBytes = imageBytes,
                    title = title,
                    category = category,
                    expiryDate = expiryDate,
                    issueDate = issueDate,
                    tags = tags,
                    notes = notes,
                    accountNumber = accountNumber,
                    issuer = issuer,
                    familyMember = familyMember,
                    folderName = folderName
                )
                _uiMessage.value = "Document scanned and saved automatically in vault"
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiMessage.value = "Failed to save camera document"
                onComplete(false)
            }
        }
    }

    fun updateDocument(
        id: Long,
        title: String,
        category: String,
        expiryDate: Long?,
        issueDate: Long? = null,
        tags: String,
        notes: String,
        accountNumber: String = "",
        issuer: String = "",
        familyMember: String = "Self",
        folderName: String = "General",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateDocument(
                id = id,
                title = title,
                category = category,
                expiryDate = expiryDate,
                issueDate = issueDate,
                tags = tags,
                notes = notes,
                accountNumber = accountNumber,
                issuer = issuer,
                familyMember = familyMember,
                folderName = folderName
            )
            _uiMessage.value = "Document details updated"
            onComplete()
        }
    }

    fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentFavorite
            repository.updateFavorite(id, newStatus)
            _uiMessage.value = if (newStatus) "Added to Favorites ⭐" else "Removed from Favorites"
        }
    }

    fun markOpened(id: Long) {
        viewModelScope.launch {
            repository.markOpened(id)
        }
    }

    fun moveToTrash(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.moveToTrash(id)
            _uiMessage.value = "Moved document to Trash/Recycle Bin"
            onComplete()
        }
    }

    fun restoreFromTrash(id: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(id)
            _uiMessage.value = "Document restored to Vault"
        }
    }

    fun bulkMoveToTrash(ids: List<Long>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.bulkMoveToTrash(ids)
            _uiMessage.value = "${ids.size} documents moved to Trash"
            onComplete()
        }
    }

    fun bulkRestoreFromTrash(ids: List<Long>) {
        viewModelScope.launch {
            repository.bulkRestoreFromTrash(ids)
            _uiMessage.value = "${ids.size} documents restored"
        }
    }

    fun bulkDeletePermanent(ids: List<Long>) {
        viewModelScope.launch {
            repository.bulkDeletePermanent(ids)
            _uiMessage.value = "${ids.size} documents permanently deleted"
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            _uiMessage.value = "Recycle bin emptied"
        }
    }

    fun convertImageToPdf(docId: Long, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.convertImageToPdf(getApplication(), docId)
            if (ok) {
                _uiMessage.value = "Converted PDF saved to Vault! Look for it under 'All Documents' or filter by 'PDF'."
            } else {
                _uiMessage.value = "Failed to convert image to PDF"
            }
            onComplete(ok)
        }
    }

    fun addRenewalRecord(docId: Long, renewalNotes: String, newExpiryDate: Long?, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addRenewalRecord(docId, renewalNotes, newExpiryDate)
            _uiMessage.value = "Document renewal logged"
            onComplete()
        }
    }

    fun exportEncryptedBackup(passphrase: String, outputFile: File, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val docs = repository.getAllDocumentsSync()
            val folders = repository.allFolders.firstOrNull() ?: emptyList()
            val categories = securityManager.categoriesList.firstOrNull() ?: emptyList()
            val familyMembers = securityManager.familyMembersList.firstOrNull() ?: emptyList()

            if (docs.isEmpty() && folders.isEmpty()) {
                onResult(false, "Vault is empty. No documents or folders to export.")
                return@launch
            }

            val success = securityManager.exportEncryptedBackup(
                documents = docs,
                folders = folders,
                categories = categories,
                familyMembers = familyMembers,
                passphrase = passphrase,
                outputFile = outputFile
            )
            if (success) {
                var publicLocationNote = "File saved at: ${outputFile.name}"
                try {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val appBackupFolder = File(downloadsDir, "DastavejVault/Backups").apply { if (!exists()) mkdirs() }
                    val publicFile = File(appBackupFolder, outputFile.name)
                    outputFile.copyTo(publicFile, overwrite = true)
                    publicLocationNote = "File exported to Downloads/DastavejVault/Backups/${outputFile.name}"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                onResult(true, "Encrypted vault backup created successfully!\n$publicLocationNote")
            } else {
                onResult(false, "Failed to create encrypted backup. Check passphrase.")
            }
        }
    }

    fun importEncryptedBackup(backupFile: File, passphrase: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val count = securityManager.importEncryptedBackup(
                backupFile = backupFile,
                passphrase = passphrase,
                onRestoreCategory = { cat -> securityManager.addCategory(cat) },
                onRestoreFamilyMember = { member -> securityManager.addFamilyMember(member) },
                onRestoreFolder = { folder -> repository.createFolder(folder.name, folder.colorHex, folder.familyMember, folder.description) },
                onDocumentExtracted = { entity, tempFile ->
                    repository.insertRestoredDocument(entity, tempFile)
                }
            )

            if (count > 0) {
                _uiMessage.value = "Successfully restored $count documents, profiles and folders into your vault."
                onResult(true, "Successfully restored $count documents, profiles and folders into your vault.")
            } else if (count == 0) {
                onResult(false, "No valid documents found in backup archive.")
            } else {
                onResult(false, "Incorrect passphrase or corrupted backup file.")
            }
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    private data class FilterState(
        val query: String,
        val category: String?,
        val fileType: String?,
        val member: String?,
        val folder: String?,
        val showExpiring: Boolean,
        val favs: Boolean
    )
}
