package com.example.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.database.DocumentEntity
import com.example.data.database.FolderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

private val Context.dataStore by preferencesDataStore(name = "vault_security_prefs")

class VaultSecurityManager(private val context: Context) {

    private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
    private val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
    private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    private val KEY_AUTO_LOCK_MINUTES = stringPreferencesKey("auto_lock_minutes")
    private val KEY_CATEGORIES = stringPreferencesKey("custom_categories_v1")
    private val KEY_FAMILY_MEMBERS = stringPreferencesKey("custom_family_members_v1")

    val isPinSet: Flow<Boolean> = context.dataStore.data.map { prefs ->
        !prefs[KEY_PIN_HASH].isNullOrEmpty()
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val autoLockMinutes: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_LOCK_MINUTES] ?: "1" // Default 1 min
    }

    val categoriesList: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_CATEGORIES]
        if (!raw.isNullOrEmpty()) {
            raw.split("|||").filter { it.isNotBlank() }
        } else {
            com.example.ui.components.DEFAULT_CATEGORIES
        }
    }

    val familyMembersList: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_FAMILY_MEMBERS]
        if (!raw.isNullOrEmpty()) {
            raw.split("|||").filter { it.isNotBlank() }
        } else {
            listOf("Self", "Spouse", "Father", "Mother", "Child", "Business")
        }
    }

    suspend fun addCategory(categoryName: String) {
        val current = categoriesList.first().toMutableList()
        val trimmed = categoryName.trim()
        if (trimmed.isNotEmpty() && !current.contains(trimmed)) {
            current.add(trimmed)
            context.dataStore.edit { prefs ->
                prefs[KEY_CATEGORIES] = current.joinToString("|||")
            }
        }
    }

    suspend fun removeCategory(categoryName: String) {
        val current = categoriesList.first().toMutableList()
        if (current.size > 1 && current.contains(categoryName)) {
            current.remove(categoryName)
            context.dataStore.edit { prefs ->
                prefs[KEY_CATEGORIES] = current.joinToString("|||")
            }
        }
    }

    suspend fun addFamilyMember(memberName: String) {
        val current = familyMembersList.first().toMutableList()
        val trimmed = memberName.trim()
        if (trimmed.isNotEmpty() && !current.contains(trimmed)) {
            current.add(trimmed)
            context.dataStore.edit { prefs ->
                prefs[KEY_FAMILY_MEMBERS] = current.joinToString("|||")
            }
        }
    }

    suspend fun removeFamilyMember(memberName: String) {
        val current = familyMembersList.first().toMutableList()
        if (current.size > 1 && current.contains(memberName)) {
            current.remove(memberName)
            context.dataStore.edit { prefs ->
                prefs[KEY_FAMILY_MEMBERS] = current.joinToString("|||")
            }
        }
    }

    suspend fun setPin(pin: String): Boolean {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hash = hashPin(pin, saltBytes)

        // Check if biometric is supported on device to auto-enable by default
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        val deviceSupportsBio = (canAuth == BiometricManager.BIOMETRIC_SUCCESS)

        context.dataStore.edit { prefs ->
            prefs[KEY_PIN_HASH] = hash
            prefs[KEY_PIN_SALT] = saltBase64
            // Default biometric login to ON if device supports it
            if (!prefs.contains(KEY_BIOMETRIC_ENABLED) && deviceSupportsBio) {
                prefs[KEY_BIOMETRIC_ENABLED] = true
            }
        }
        return true
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val storedHash = prefs[KEY_PIN_HASH] ?: return false
        val storedSaltBase64 = prefs[KEY_PIN_SALT] ?: return false
        val saltBytes = Base64.decode(storedSaltBase64, Base64.NO_WRAP)

        val computedHash = hashPin(pin, saltBytes)
        return computedHash == storedHash
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setAutoLockMinutes(minutes: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_LOCK_MINUTES] = minutes
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    // Encrypted Vault Backup (.dastavej format)
    suspend fun exportEncryptedBackup(
        documents: List<DocumentEntity>,
        folders: List<FolderEntity>,
        categories: List<String>,
        familyMembers: List<String>,
        passphrase: String,
        outputFile: File
    ): Boolean {
        return try {
            val zipBytesStream = ByteArrayOutputStream()
            ZipOutputStream(zipBytesStream).use { zos ->
                // 1. Serialize documents and write files to docs/
                val documentsArray = JSONArray()
                documents.forEach { doc ->
                    val file = File(doc.filePath)
                    if (file.exists()) {
                        val entryName = "docs/${file.name}"
                        zos.putNextEntry(ZipEntry(entryName))
                        FileInputStream(file).use { fis -> fis.copyTo(zos) }
                        zos.closeEntry()

                        val jsonObj = JSONObject().apply {
                            put("id", doc.id)
                            put("title", doc.title)
                            put("category", doc.category)
                            put("fileName", file.name)
                            put("fileType", doc.fileType)
                            put("dateAdded", doc.dateAdded)
                            put("expiryDate", doc.expiryDate ?: -1)
                            put("issueDate", doc.issueDate ?: -1)
                            put("tags", doc.tags)
                            put("notes", doc.notes)
                            put("ocrText", doc.ocrText)
                            put("accountNumber", doc.accountNumber)
                            put("issuer", doc.issuer)
                            put("familyMember", doc.familyMember)
                            put("folderName", doc.folderName)
                            put("isFavorite", doc.isFavorite)
                            put("renewalHistory", doc.renewalHistory)
                            put("fileSize", doc.fileSize)
                        }
                        documentsArray.put(jsonObj)
                    }
                }

                // 2. Serialize folders
                val foldersArray = JSONArray()
                folders.forEach { folder ->
                    val fObj = JSONObject().apply {
                        put("name", folder.name)
                        put("colorHex", folder.colorHex)
                        put("familyMember", folder.familyMember)
                        put("description", folder.description)
                    }
                    foldersArray.put(fObj)
                }

                // 3. Serialize categories and family members
                val categoriesArray = JSONArray()
                categories.forEach { categoriesArray.put(it) }

                val familyArray = JSONArray()
                familyMembers.forEach { familyArray.put(it) }

                val rootBackupObj = JSONObject().apply {
                    put("version", 2)
                    put("exportTimestamp", System.currentTimeMillis())
                    put("categories", categoriesArray)
                    put("familyMembers", familyArray)
                    put("folders", foldersArray)
                    put("documents", documentsArray)
                }

                // Write metadata.json into zip
                val metadataBytes = rootBackupObj.toString(2).toByteArray(Charsets.UTF_8)
                zos.putNextEntry(ZipEntry("metadata.json"))
                zos.write(metadataBytes)
                zos.closeEntry()
            }

            val zipData = zipBytesStream.toByteArray()

            // Key derivation using PBKDF2
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val iv = ByteArray(12) // GCM IV length
            SecureRandom().nextBytes(iv)

            val secretKey = deriveKey(passphrase, salt)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            val encryptedBytes = cipher.doFinal(zipData)

            // File format: [16 bytes salt][12 bytes iv][encryptedBytes]
            FileOutputStream(outputFile).use { fos ->
                fos.write(salt)
                fos.write(iv)
                fos.write(encryptedBytes)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importEncryptedBackup(
        backupFile: File,
        passphrase: String,
        onRestoreCategory: suspend (String) -> Unit,
        onRestoreFamilyMember: suspend (String) -> Unit,
        onRestoreFolder: suspend (FolderEntity) -> Unit,
        onDocumentExtracted: suspend (DocumentEntity, File) -> Unit
    ): Int {
        val fileBytes = try {
            backupFile.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
        if (fileBytes.size < 28) return -1 // Invalid header

        val salt = fileBytes.copyOfRange(0, 16)
        val iv = fileBytes.copyOfRange(16, 28)
        val encryptedBytes = fileBytes.copyOfRange(28, fileBytes.size)

        // 1. Decrypt ZIP archive. If this fails, it is guaranteed to be a wrong passphrase or corrupted file!
        val decryptedZipBytes = try {
            val secretKey = deriveKey(passphrase, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            cipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1 // Decryption failure
        }

        // 2. Extract contents to temporary directory
        val tempExtractedDir = File(context.cacheDir, "backup_restore_temp_${System.currentTimeMillis()}").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        var metadataJsonString: String? = null

        try {
            ZipInputStream(ByteArrayInputStream(decryptedZipBytes)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val entryFile = File(tempExtractedDir, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        FileOutputStream(entryFile).use { fos -> zis.copyTo(fos) }
                        if (entry.name == "metadata.json") {
                            metadataJsonString = entryFile.readText(Charsets.UTF_8)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tempExtractedDir.deleteRecursively()
            return -1
        }

        if (metadataJsonString.isNullOrEmpty()) {
            tempExtractedDir.deleteRecursively()
            return 0
        }

        var restoredCount = 0

        try {
            // Check if format is JSONObject (Version 2) or legacy JSONArray (Version 1)
            val trimmedJson = metadataJsonString!!.trim()
            if (trimmedJson.startsWith("{")) {
                val rootObj = JSONObject(trimmedJson)

                // Restore custom categories
                if (rootObj.has("categories")) {
                    val catArray = rootObj.getJSONArray("categories")
                    for (c in 0 until catArray.length()) {
                        try {
                            onRestoreCategory(catArray.getString(c))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Restore custom family members
                if (rootObj.has("familyMembers")) {
                    val famArray = rootObj.getJSONArray("familyMembers")
                    for (f in 0 until famArray.length()) {
                        try {
                            onRestoreFamilyMember(famArray.getString(f))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Restore folders
                if (rootObj.has("folders")) {
                    val foldersArray = rootObj.getJSONArray("folders")
                    for (f in 0 until foldersArray.length()) {
                        try {
                            val fObj = foldersArray.getJSONObject(f)
                            val folder = FolderEntity(
                                name = fObj.optString("name", "General"),
                                colorHex = fObj.optString("colorHex", "#3F51B5"),
                                familyMember = fObj.optString("familyMember", "All"),
                                description = fObj.optString("description", "")
                            )
                            onRestoreFolder(folder)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Restore documents
                if (rootObj.has("documents")) {
                    val docsArray = rootObj.getJSONArray("documents")
                    for (i in 0 until docsArray.length()) {
                        try {
                            val item = docsArray.getJSONObject(i)
                            val fileName = item.getString("fileName")
                            val sourceDocFile = File(tempExtractedDir, "docs/$fileName")

                            if (sourceDocFile.exists()) {
                                val expiry = if (item.has("expiryDate") && item.getLong("expiryDate") != -1L) {
                                    item.getLong("expiryDate")
                                } else null

                                val issue = if (item.has("issueDate") && item.getLong("issueDate") != -1L) {
                                    item.getLong("issueDate")
                                } else null

                                val docEntity = DocumentEntity(
                                    title = item.optString("title", "Restored Document"),
                                    category = item.optString("category", "General"),
                                    filePath = "",
                                    fileType = item.optString("fileType", if (fileName.endsWith(".pdf", ignoreCase = true)) "PDF" else "IMAGE"),
                                    dateAdded = item.optLong("dateAdded", System.currentTimeMillis()),
                                    expiryDate = expiry,
                                    issueDate = issue,
                                    tags = item.optString("tags", ""),
                                    notes = item.optString("notes", ""),
                                    ocrText = item.optString("ocrText", ""),
                                    accountNumber = item.optString("accountNumber", ""),
                                    issuer = item.optString("issuer", ""),
                                    familyMember = item.optString("familyMember", "Self"),
                                    folderName = item.optString("folderName", "General"),
                                    isFavorite = item.optBoolean("isFavorite", false),
                                    renewalHistory = item.optString("renewalHistory", ""),
                                    fileSize = item.optLong("fileSize", sourceDocFile.length())
                                )

                                onDocumentExtracted(docEntity, sourceDocFile)
                                restoredCount++
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                // Legacy JSONArray format
                val metadataArray = JSONArray(trimmedJson)
                for (i in 0 until metadataArray.length()) {
                    try {
                        val item = metadataArray.getJSONObject(i)
                        val fileName = item.getString("fileName")
                        val sourceDocFile = File(tempExtractedDir, "docs/$fileName")

                        if (sourceDocFile.exists()) {
                            val expiry = if (item.has("expiryDate") && item.getLong("expiryDate") != -1L) {
                                item.getLong("expiryDate")
                            } else null

                            val docEntity = DocumentEntity(
                                title = item.optString("title", "Restored Document"),
                                category = item.optString("category", "General"),
                                filePath = "",
                                fileType = item.optString("fileType", if (fileName.endsWith(".pdf", ignoreCase = true)) "PDF" else "IMAGE"),
                                dateAdded = item.optLong("dateAdded", System.currentTimeMillis()),
                                expiryDate = expiry,
                                tags = item.optString("tags", ""),
                                notes = item.optString("notes", ""),
                                ocrText = item.optString("ocrText", ""),
                                accountNumber = item.optString("accountNumber", ""),
                                issuer = item.optString("issuer", ""),
                                familyMember = item.optString("familyMember", "Self"),
                                folderName = item.optString("folderName", "General"),
                                fileSize = item.optLong("fileSize", sourceDocFile.length())
                            )

                            onDocumentExtracted(docEntity, sourceDocFile)
                            restoredCount++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            tempExtractedDir.deleteRecursively()
        }

        return restoredCount
    }

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKeyBytes = factory.generateSecret(keySpec).encoded
        return SecretKeySpec(secretKeyBytes, "AES")
    }
}

