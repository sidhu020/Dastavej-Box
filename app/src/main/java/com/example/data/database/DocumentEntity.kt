package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // e.g., "Aadhaar Card", "PAN Card", "Driving Licence", "Passport", etc.
    val filePath: String, // Path inside app-private files directory
    val fileType: String, // "PDF" or "IMAGE"
    val dateAdded: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null, // Epoch millis or null
    val issueDate: Long? = null, // Epoch millis or null
    val tags: String = "", // Comma-separated or space-separated tags
    val notes: String = "",
    val ocrText: String = "",
    val accountNumber: String = "",
    val issuer: String = "", // e.g., UIDAI, Income Tax Dept, Bank, Transport Dept
    val familyMember: String = "Self", // e.g., Self, Spouse, Father, Mother, Child, Business
    val folderName: String = "General", // Folder assignment e.g. "General", "Personal ID", "Medical"
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false,
    val trashedAt: Long = 0,
    val lastOpenedAt: Long = 0,
    val renewalHistory: String = "", // Past renewal notes & log
    val thumbnailPath: String? = null, // Thumbnail image path
    val fileSize: Long = 0, // File size in bytes
    val isEncrypted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
