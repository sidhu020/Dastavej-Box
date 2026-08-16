package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class FileStorageManager(private val context: Context) {

    private val documentsDir: File
        get() = File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }

    private val thumbnailsDir: File
        get() = File(context.filesDir, "thumbnails").apply { if (!exists()) mkdirs() }

    suspend fun saveFileFromUri(uri: Uri, isPdf: Boolean): StoredFileInfo = withContext(Dispatchers.IO) {
        val extension = if (isPdf) "pdf" else "jpg"
        val fileName = "doc_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension"
        val targetFile = File(documentsDir, fileName)

        var fileSize = 0L
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    fileSize += bytesRead
                }
            }
        } ?: throw IllegalStateException("Unable to open input stream for Uri: $uri")

        // Generate thumbnail
        val thumbnailPath = generateThumbnail(targetFile, isPdf)

        StoredFileInfo(
            filePath = targetFile.absolutePath,
            fileType = if (isPdf) "PDF" else "IMAGE",
            fileSize = fileSize,
            thumbnailPath = thumbnailPath
        )
    }

    suspend fun saveImageBytes(bytes: ByteArray): StoredFileInfo = withContext(Dispatchers.IO) {
        val fileName = "doc_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val targetFile = File(documentsDir, fileName)

        FileOutputStream(targetFile).use { output ->
            output.write(bytes)
        }

        val thumbnailPath = generateThumbnail(targetFile, isPdf = false)

        StoredFileInfo(
            filePath = targetFile.absolutePath,
            fileType = "IMAGE",
            fileSize = targetFile.length(),
            thumbnailPath = thumbnailPath
        )
    }

    suspend fun generateThumbnail(file: File, isPdf: Boolean): String? = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext null
        val thumbFile = File(thumbnailsDir, "thumb_${file.nameWithoutExtension}.webp")

        try {
            val bitmap = if (isPdf) {
                renderFirstPageOfPdf(file)
            } else {
                decodeAndScaleImage(file, maxDimension = 400)
            }

            if (bitmap != null) {
                FileOutputStream(thumbFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 80, out)
                }
                bitmap.recycle()
                return@withContext thumbFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e("FileStorageManager", "Error generating thumbnail for ${file.name}", e)
        }
        return@withContext null
    }

    private fun renderFirstPageOfPdf(pdfFile: File): Bitmap? {
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            if (renderer.pageCount == 0) {
                renderer.close()
                fileDescriptor.close()
                return null
            }
            val page = renderer.openPage(0)
            // Render at reasonable resolution
            val width = 400
            val height = (width * (page.height.toFloat() / page.width.toFloat())).toInt().coerceAtLeast(100)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            fileDescriptor.close()
            bitmap
        } catch (e: Exception) {
            Log.e("FileStorageManager", "Failed to render PDF page", e)
            null
        }
    }

    private fun decodeAndScaleImage(imageFile: File, maxDimension: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(imageFile.absolutePath, options)

            var sampleSize = 1
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(imageFile.absolutePath, decodeOptions)
        } catch (e: Exception) {
            Log.e("FileStorageManager", "Failed to decode scaled image", e)
            null
        }
    }

    suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(filePath)
        var deleted = false
        if (file.exists()) {
            deleted = file.delete()
        }
        // Also delete thumbnail if present
        val thumbFile = File(thumbnailsDir, "thumb_${file.nameWithoutExtension}.webp")
        if (thumbFile.exists()) {
            thumbFile.delete()
        }
        deleted
    }

    fun getPdfPageCount(filePath: String): Int {
        return try {
            val file = File(filePath)
            if (!file.exists()) return 0
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            val count = renderer.pageCount
            renderer.close()
            fd.close()
            count
        } catch (e: Exception) {
            0
        }
    }

    fun renderPdfPage(filePath: String, pageIndex: Int, targetWidth: Int): Bitmap? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                renderer.close()
                fd.close()
                return null
            }
            val page = renderer.openPage(pageIndex)
            val height = (targetWidth * (page.height.toFloat() / page.width.toFloat())).toInt().coerceAtLeast(100)
            val bitmap = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            fd.close()
            bitmap
        } catch (e: Exception) {
            Log.e("FileStorageManager", "Error rendering PDF page $pageIndex", e)
            null
        }
    }

    fun verifyFileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}

data class StoredFileInfo(
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val thumbnailPath: String?
)
