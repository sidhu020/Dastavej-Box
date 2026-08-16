package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE isTrashed = 0 ORDER BY dateAdded DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentDocuments(limit: Int = 10): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND lastOpenedAt > 0 ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentlyOpenedDocuments(limit: Int = 10): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 1 ORDER BY trashedAt DESC")
    fun getTrashedDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    fun getDocumentById(id: Long): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentByIdSync(id: Long): DocumentEntity?

    @Query("""
        SELECT * FROM documents 
        WHERE isTrashed = 0 AND (
           title LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%' 
           OR ocrText LIKE '%' || :query || '%' 
           OR accountNumber LIKE '%' || :query || '%'
           OR issuer LIKE '%' || :query || '%'
           OR familyMember LIKE '%' || :query || '%'
        )
        ORDER BY dateAdded DESC
    """)
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND category = :category ORDER BY dateAdded DESC")
    fun getDocumentsByCategory(category: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND folderName = :folderName ORDER BY dateAdded DESC")
    fun getDocumentsByFolder(folderName: String): Flow<List<DocumentEntity>>

    @Query("SELECT COUNT(*) FROM documents WHERE isTrashed = 0 AND folderName = :folderName")
    fun getFolderDocumentCount(folderName: String): Flow<Int>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND familyMember = :member ORDER BY dateAdded DESC")
    fun getDocumentsByFamilyMember(member: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND expiryDate IS NOT NULL AND expiryDate <= :thresholdDateMillis ORDER BY expiryDate ASC")
    fun getExpiringDocuments(thresholdDateMillis: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND fileType = :fileType ORDER BY dateAdded DESC")
    fun getDocumentsByFileType(fileType: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isTrashed = 0 AND (accountNumber = :accountNumber OR title = :title) AND id != :excludeId")
    suspend fun checkDuplicates(accountNumber: String, title: String, excludeId: Long = 0): List<DocumentEntity>

    @Query("SELECT COUNT(*) FROM documents WHERE isTrashed = 0 AND category = :category")
    suspend fun getCategoryCount(category: String): Int

    @Query("SELECT COUNT(*) FROM documents WHERE isTrashed = 0")
    fun getTotalDocumentCount(): Flow<Int>

    @Query("SELECT SUM(fileSize) FROM documents WHERE isTrashed = 0")
    fun getTotalStorageUsed(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET isTrashed = 1, trashedAt = :trashedAt WHERE id = :id")
    suspend fun moveToTrash(id: Long, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isTrashed = 0, trashedAt = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("UPDATE documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE documents SET lastOpenedAt = :openedAt WHERE id = :id")
    suspend fun updateLastOpened(id: Long, openedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM documents WHERE id IN (:ids)")
    suspend fun bulkDeletePermanent(ids: List<Long>)

    @Query("UPDATE documents SET folderName = :targetFolder WHERE id IN (:ids)")
    suspend fun bulkMoveToFolder(ids: List<Long>, targetFolder: String)

    @Query("UPDATE documents SET folderName = :newFolderName WHERE folderName = :oldFolderName")
    suspend fun updateFolderNameInDocuments(oldFolderName: String, newFolderName: String)

    @Query("UPDATE documents SET tags = CASE WHEN tags IS NULL OR tags = '' THEN :newTag ELSE tags || ', ' || :newTag END WHERE id IN (:ids)")
    suspend fun bulkAddTag(ids: List<Long>, newTag: String)

    @Query("UPDATE documents SET isTrashed = 1, trashedAt = :trashedAt WHERE id IN (:ids)")
    suspend fun bulkMoveToTrash(ids: List<Long>, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isTrashed = 0, trashedAt = 0 WHERE id IN (:ids)")
    suspend fun bulkRestoreFromTrash(ids: List<Long>)

    @Query("DELETE FROM documents WHERE isTrashed = 1")
    suspend fun emptyTrash()

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("SELECT * FROM documents")
    suspend fun getAllDocumentsSync(): List<DocumentEntity>
}
