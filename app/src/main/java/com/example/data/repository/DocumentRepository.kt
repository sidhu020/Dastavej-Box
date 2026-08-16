package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.data.database.DocumentDao
import com.example.data.database.DocumentEntity
import com.example.data.database.FolderDao
import com.example.data.database.FolderEntity
import com.example.data.notification.NotificationScheduler
import com.example.data.ocr.OnDeviceOcrEngine
import com.example.data.storage.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val folderDao: FolderDao,
    private val fileStorageManager: FileStorageManager,
    private val ocrEngine: OnDeviceOcrEngine,
    private val notificationScheduler: NotificationScheduler
) {

    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()
    val recentDocuments: Flow<List<DocumentEntity>> = documentDao.getRecentDocuments(10)
    val recentlyOpenedDocuments: Flow<List<DocumentEntity>> = documentDao.getRecentlyOpenedDocuments(10)
    val favoriteDocuments: Flow<List<DocumentEntity>> = documentDao.getFavoriteDocuments()
    val trashedDocuments: Flow<List<DocumentEntity>> = documentDao.getTrashedDocuments()
    val totalDocumentCount: Flow<Int> = documentDao.getTotalDocumentCount()
    val totalStorageUsed: Flow<Long?> = documentDao.getTotalStorageUsed()

    fun getDocumentById(id: Long): Flow<DocumentEntity?> = documentDao.getDocumentById(id)

    suspend fun getDocumentByIdSync(id: Long): DocumentEntity? = documentDao.getDocumentByIdSync(id)

    fun searchDocuments(query: String): Flow<List<DocumentEntity>> = documentDao.searchDocuments(query)

    fun getDocumentsByCategory(category: String): Flow<List<DocumentEntity>> = documentDao.getDocumentsByCategory(category)

    fun getDocumentsByFolder(folderName: String): Flow<List<DocumentEntity>> = documentDao.getDocumentsByFolder(folderName)

    fun getFolderDocumentCount(folderName: String): Flow<Int> = documentDao.getFolderDocumentCount(folderName)

    fun getDocumentsByFamilyMember(member: String): Flow<List<DocumentEntity>> = documentDao.getDocumentsByFamilyMember(member)

    suspend fun createFolder(name: String, colorHex: String = "#3F51B5", familyMember: String = "All", description: String = ""): Long {
        val existing = folderDao.getFolderByName(name)
        if (existing != null) return existing.id
        val folder = FolderEntity(name = name, colorHex = colorHex, familyMember = familyMember, description = description)
        return folderDao.insertFolder(folder)
    }

    suspend fun updateFolder(folder: FolderEntity) {
        val oldFolder = folderDao.getFolderById(folder.id)
        if (oldFolder != null && oldFolder.name != folder.name) {
            documentDao.updateFolderNameInDocuments(oldFolder.name, folder.name)
        }
        folderDao.updateFolder(folder)
    }

    suspend fun editFolder(id: Long, name: String, colorHex: String, description: String) {
        val existing = folderDao.getFolderById(id) ?: return
        if (existing.name != name) {
            documentDao.updateFolderNameInDocuments(existing.name, name)
        }
        val updated = existing.copy(name = name, colorHex = colorHex, description = description)
        folderDao.updateFolder(updated)
    }

    suspend fun moveDocumentToFolder(docId: Long, targetFolder: String) {
        documentDao.bulkMoveToFolder(listOf(docId), targetFolder)
    }

    suspend fun copyDocumentToFolder(docId: Long, targetFolder: String): Long? {
        val doc = documentDao.getDocumentByIdSync(docId) ?: return null
        val sourceFile = File(doc.filePath)
        if (!sourceFile.exists()) return null

        val isPdf = doc.fileType == "PDF"
        val extension = if (isPdf) "pdf" else "jpg"
        val newFileName = "doc_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}.$extension"
        val targetFile = File(sourceFile.parentFile, newFileName)
        sourceFile.copyTo(targetFile, overwrite = true)

        val newThumbnail = fileStorageManager.generateThumbnail(targetFile, isPdf)

        val copyDoc = doc.copy(
            id = 0,
            title = if (doc.folderName == targetFolder) "${doc.title} (Copy)" else doc.title,
            filePath = targetFile.absolutePath,
            thumbnailPath = newThumbnail ?: doc.thumbnailPath,
            folderName = targetFolder,
            dateAdded = System.currentTimeMillis(),
            lastOpenedAt = 0,
            updatedAt = System.currentTimeMillis()
        )
        return documentDao.insertDocument(copyDoc)
    }

    suspend fun deleteFolder(id: Long, folderName: String) {
        documentDao.updateFolderNameInDocuments(folderName, "General")
        folderDao.deleteFolderById(id)
    }

    suspend fun bulkMoveToFolder(ids: List<Long>, targetFolder: String) {
        documentDao.bulkMoveToFolder(ids, targetFolder)
    }

    suspend fun bulkAddTag(ids: List<Long>, newTag: String) {
        documentDao.bulkAddTag(ids, newTag)
    }

    fun getDocumentsByFileType(fileType: String): Flow<List<DocumentEntity>> = documentDao.getDocumentsByFileType(fileType)

    fun getExpiringDocuments(thresholdDateMillis: Long): Flow<List<DocumentEntity>> =
        documentDao.getExpiringDocuments(thresholdDateMillis)

    suspend fun checkDuplicates(accountNumber: String, title: String, excludeId: Long = 0): List<DocumentEntity> {
        if (accountNumber.isBlank() && title.isBlank()) return emptyList()
        return documentDao.checkDuplicates(accountNumber, title, excludeId)
    }

    suspend fun importDocument(
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
        folderName: String = "General"
    ): Long {
        val storedFile = fileStorageManager.saveFileFromUri(uri, isPdf)

        val ocrRes = ocrEngine.processDocumentText(
            filePath = storedFile.filePath,
            category = category,
            docTitle = title,
            existingAccountNumber = accountNumber
        )

        val finalAccNumber = if (accountNumber.isNotBlank()) accountNumber else ocrRes.extractedNumber
        val finalIssueDate = issueDate ?: ocrRes.detectedIssueDate
        val finalExpiryDate = expiryDate ?: ocrRes.detectedExpiryDate
        val finalIssuer = if (issuer.isNotBlank()) issuer else ocrRes.suggestedIssuer

        val document = DocumentEntity(
            title = title,
            category = category,
            filePath = storedFile.filePath,
            fileType = storedFile.fileType,
            dateAdded = System.currentTimeMillis(),
            expiryDate = finalExpiryDate,
            issueDate = finalIssueDate,
            tags = if (tags.isNotBlank()) tags else ocrRes.suggestedTags.joinToString(", "),
            notes = notes,
            ocrText = ocrRes.ocrText,
            accountNumber = finalAccNumber,
            issuer = finalIssuer,
            familyMember = if (familyMember.isNotBlank()) familyMember else "Self",
            folderName = if (folderName.isNotBlank()) folderName else "General",
            thumbnailPath = storedFile.thumbnailPath,
            fileSize = storedFile.fileSize
        )

        val id = documentDao.insertDocument(document)

        if (finalExpiryDate != null) {
            scheduleExpiryReminders(document.copy(id = id), finalExpiryDate)
        }

        return id
    }

    suspend fun addDocumentFromCapturedImage(
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
        folderName: String = "General"
    ): Long {
        val storedFile = fileStorageManager.saveImageBytes(imageBytes)

        val ocrRes = ocrEngine.processDocumentText(
            filePath = storedFile.filePath,
            category = category,
            docTitle = title,
            existingAccountNumber = accountNumber
        )

        val finalAccNumber = if (accountNumber.isNotBlank()) accountNumber else ocrRes.extractedNumber
        val finalIssueDate = issueDate ?: ocrRes.detectedIssueDate
        val finalExpiryDate = expiryDate ?: ocrRes.detectedExpiryDate
        val finalIssuer = if (issuer.isNotBlank()) issuer else ocrRes.suggestedIssuer

        val document = DocumentEntity(
            title = title,
            category = category,
            filePath = storedFile.filePath,
            fileType = "IMAGE",
            dateAdded = System.currentTimeMillis(),
            expiryDate = finalExpiryDate,
            issueDate = finalIssueDate,
            tags = if (tags.isNotBlank()) tags else ocrRes.suggestedTags.joinToString(", "),
            notes = notes,
            ocrText = ocrRes.ocrText,
            accountNumber = finalAccNumber,
            issuer = finalIssuer,
            familyMember = if (familyMember.isNotBlank()) familyMember else "Self",
            folderName = if (folderName.isNotBlank()) folderName else "General",
            thumbnailPath = storedFile.thumbnailPath,
            fileSize = storedFile.fileSize
        )

        val id = documentDao.insertDocument(document)

        if (finalExpiryDate != null) {
            scheduleExpiryReminders(document.copy(id = id), finalExpiryDate)
        }

        return id
    }

    suspend fun updateDocument(
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
        folderName: String = "General"
    ) {
        val existing = documentDao.getDocumentByIdSync(id) ?: return
        val ocrRes = ocrEngine.processDocumentText(
            filePath = existing.filePath,
            category = category,
            docTitle = title,
            existingAccountNumber = accountNumber
        )

        val finalAccNumber = if (accountNumber.isNotBlank()) accountNumber else ocrRes.extractedNumber
        val finalIssuer = if (issuer.isNotBlank()) issuer else ocrRes.suggestedIssuer

        val updated = existing.copy(
            title = title,
            category = category,
            expiryDate = expiryDate,
            issueDate = issueDate,
            tags = tags,
            notes = notes,
            ocrText = if (ocrRes.ocrText.isNotBlank()) ocrRes.ocrText else existing.ocrText,
            accountNumber = finalAccNumber,
            issuer = finalIssuer,
            familyMember = familyMember,
            folderName = folderName,
            updatedAt = System.currentTimeMillis()
        )
        documentDao.updateDocument(updated)

        if (expiryDate != null) {
            scheduleExpiryReminders(updated, expiryDate)
        }
    }

    suspend fun addRenewalRecord(id: Long, renewalNotes: String, newExpiryDate: Long?) {
        val doc = documentDao.getDocumentByIdSync(id) ?: return
        val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormatter.format(Date())
        val newEntry = "• [$dateStr] Renewed: $renewalNotes"
        val updatedHistory = if (doc.renewalHistory.isBlank()) newEntry else "${doc.renewalHistory}\n$newEntry"

        val updated = doc.copy(
            renewalHistory = updatedHistory,
            expiryDate = newExpiryDate ?: doc.expiryDate,
            updatedAt = System.currentTimeMillis()
        )
        documentDao.updateDocument(updated)
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        documentDao.updateFavorite(id, !currentStatus)
    }

    suspend fun updateFavorite(id: Long, isFavorite: Boolean) {
        documentDao.updateFavorite(id, isFavorite)
    }

    suspend fun markOpened(id: Long) {
        documentDao.updateLastOpened(id, System.currentTimeMillis())
    }

    suspend fun moveToTrash(id: Long) {
        documentDao.moveToTrash(id)
    }

    suspend fun restoreFromTrash(id: Long) {
        documentDao.restoreFromTrash(id)
    }

    suspend fun bulkMoveToTrash(ids: List<Long>) {
        documentDao.bulkMoveToTrash(ids)
    }

    suspend fun bulkRestoreFromTrash(ids: List<Long>) {
        documentDao.bulkRestoreFromTrash(ids)
    }

    suspend fun bulkDeletePermanent(ids: List<Long>) {
        ids.forEach { id ->
            val doc = documentDao.getDocumentByIdSync(id)
            if (doc != null) {
                fileStorageManager.deleteFile(doc.filePath)
            }
        }
        documentDao.bulkDeletePermanent(ids)
    }

    suspend fun emptyTrash() {
        val trashed = documentDao.getTrashedDocuments()
        documentDao.emptyTrash()
    }

    suspend fun convertImageToPdf(context: Context, docId: Long): Boolean = withContext(Dispatchers.IO) {
        val doc = documentDao.getDocumentByIdSync(docId) ?: return@withContext false
        if (doc.fileType == "PDF") return@withContext true

        val imageFile = File(doc.filePath)
        if (!imageFile.exists()) return@withContext false

        try {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath) ?: return@withContext false
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val pdfFileName = "pdf_${System.currentTimeMillis()}_${doc.id}.pdf"
            val pdfFile = File(imageFile.parentFile, pdfFileName)
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            bitmap.recycle()

            val updatedDoc = doc.copy(
                filePath = pdfFile.absolutePath,
                fileType = "PDF",
                fileSize = pdfFile.length(),
                updatedAt = System.currentTimeMillis()
            )
            documentDao.updateDocument(updatedDoc)
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    private fun scheduleExpiryReminders(document: DocumentEntity, expiryDate: Long) {
        val millisRemaining = expiryDate - System.currentTimeMillis()
        val daysRemaining = millisRemaining / (1000 * 60 * 60 * 24)
        if (daysRemaining <= 90) {
            notificationScheduler.showExpiryNotification(document, daysRemaining)
        }
    }

    suspend fun deleteDocument(document: DocumentEntity) {
        fileStorageManager.deleteFile(document.filePath)
        documentDao.deleteDocument(document)
    }

    suspend fun deleteDocumentById(id: Long) {
        val doc = documentDao.getDocumentByIdSync(id)
        if (doc != null) {
            fileStorageManager.deleteFile(doc.filePath)
            documentDao.deleteDocumentById(id)
        }
    }

    suspend fun getAllDocumentsSync(): List<DocumentEntity> = documentDao.getAllDocumentsSync()

    suspend fun insertRestoredDocument(doc: DocumentEntity, tempFile: File) {
        val storedFile = fileStorageManager.saveFileFromUri(Uri.fromFile(tempFile), doc.fileType == "PDF")
        val restoredDoc = doc.copy(
            id = 0,
            filePath = storedFile.filePath,
            thumbnailPath = storedFile.thumbnailPath,
            fileSize = storedFile.fileSize
        )
        val id = documentDao.insertDocument(restoredDoc)
        if (restoredDoc.expiryDate != null) {
            scheduleExpiryReminders(restoredDoc.copy(id = id), restoredDoc.expiryDate)
        }
    }

    fun renderPdfPage(filePath: String, pageIndex: Int, width: Int) =
        fileStorageManager.renderPdfPage(filePath, pageIndex, width)

    fun getPdfPageCount(filePath: String): Int =
        fileStorageManager.getPdfPageCount(filePath)

    fun verifyFileExists(filePath: String): Boolean =
        fileStorageManager.verifyFileExists(filePath)
}
