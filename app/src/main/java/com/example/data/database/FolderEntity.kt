package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#3F51B5",
    val familyMember: String = "All",
    val iconName: String = "Folder",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
