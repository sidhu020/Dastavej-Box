package com.example.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

data class OcrResult(
    val ocrText: String,
    val extractedNumber: String = "",
    val detectedIssueDate: Long? = null,
    val detectedExpiryDate: Long? = null,
    val suggestedIssuer: String = "",
    val suggestedTags: List<String> = emptyList()
)

class OnDeviceOcrEngine(private val context: Context) {

    suspend fun processDocumentText(
        filePath: String,
        category: String,
        docTitle: String,
        existingAccountNumber: String = ""
    ): OcrResult = withContext(Dispatchers.Default) {
        val extractedLines = mutableListOf<String>()
        extractedLines.add("Document: $docTitle")
        extractedLines.add("Category: $category")

        var autoExtractedNumber = existingAccountNumber
        var detectedIssueDate: Long? = null
        var detectedExpiryDate: Long? = null
        var suggestedIssuer = ""

        val file = File(filePath)
        if (!file.exists()) {
            return@withContext OcrResult(
                ocrText = extractedLines.joinToString("\n"),
                extractedNumber = autoExtractedNumber,
                suggestedTags = getSuggestedTagsForCategory(category)
            )
        }

        // Run ML Kit Text Recognition on Bitmap if Image File or PDF Document
        var recognizedTextFromMlKit = ""
        if (filePath.endsWith(".jpg", true) || filePath.endsWith(".png", true) || filePath.endsWith(".jpeg", true)) {
            try {
                val bitmap = BitmapFactory.decodeFile(filePath)
                if (bitmap != null) {
                    recognizedTextFromMlKit = performMlKitOcr(bitmap)
                    bitmap.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (filePath.endsWith(".pdf", true)) {
            try {
                val pfd = android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                if (pfd != null) {
                    val pdfRenderer = android.graphics.pdf.PdfRenderer(pfd)
                    val pageCountToScan = pdfRenderer.pageCount.coerceAtMost(3) // Process first 3 pages to avoid memory overhead
                    val pdfTextBuilder = StringBuilder()

                    for (i in 0 until pageCountToScan) {
                        val page = pdfRenderer.openPage(i)
                        val scale = 1080f / page.width.coerceAtLeast(1)
                        val bmpWidth = 1080
                        val bmpHeight = (page.height * scale).toInt().coerceAtLeast(1)
                        val pageBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                        page.render(pageBitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()

                        val pageText = performMlKitOcr(pageBitmap)
                        pageBitmap.recycle()

                        if (pageText.isNotBlank()) {
                            pdfTextBuilder.append("\n--- PDF Page ${i + 1} ---\n").append(pageText)
                        }
                    }
                    pdfRenderer.close()
                    pfd.close()

                    recognizedTextFromMlKit = pdfTextBuilder.toString()
                    if (recognizedTextFromMlKit.length > 4000) {
                        recognizedTextFromMlKit = recognizedTextFromMlKit.substring(0, 4000) + "\n[PDF text truncated for optimal storage]"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (recognizedTextFromMlKit.isNotBlank()) {
            extractedLines.add("--- ML Kit On-Device Extracted Text ---")
            extractedLines.add(recognizedTextFromMlKit)
        }

        val combinedTextToScan = "$docTitle $category $existingAccountNumber $recognizedTextFromMlKit $filePath"

        // Standard ID & Financial document pattern recognizers
        val aadhaarRegex = Pattern.compile("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\b")
        val panRegex = Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]\\b")
        val bankAccRegex = Pattern.compile("\\b\\d{9,18}\\b")

        // Date regex patterns (e.g. DD/MM/YYYY, YYYY-MM-DD, DD-MM-YYYY, DD MMM YYYY)
        val dateNumericRegex = Pattern.compile("\\b(0[1-9]|[12][0-9]|3[01])[-/.](0[1-9]|1[012])[-/.](19|20)\\d\\d\\b")
        val dateTextRegex = Pattern.compile("\\b(0[1-9]|[12][0-9]|3[01])\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(19|20)\\d\\d\\b", Pattern.CASE_INSENSITIVE)

        val dateFormatters = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        )

        // Line-by-line contextual date keyword scanning
        val lines = combinedTextToScan.split("\n", "\r")
        for (line in lines) {
            val lowerLine = line.lowercase()

            // Check numeric date in line
            val mNum = dateNumericRegex.matcher(line)
            val mText = dateTextRegex.matcher(line)
            var matchedDateMillis: Long? = null
            var matchedStr = ""

            if (mNum.find()) {
                matchedStr = mNum.group()
            } else if (mText.find()) {
                matchedStr = mText.group()
            }

            if (matchedStr.isNotBlank()) {
                for (df in dateFormatters) {
                    try {
                        val parsed = df.parse(matchedStr)
                        if (parsed != null) {
                            matchedDateMillis = parsed.time
                            break
                        }
                    } catch (_: Exception) {}
                }
            }

            if (matchedDateMillis != null) {
                if (lowerLine.contains("issue") || lowerLine.contains("release") || lowerLine.contains("from") || lowerLine.contains("dob") || lowerLine.contains("mfg")) {
                    if (detectedIssueDate == null) detectedIssueDate = matchedDateMillis
                } else if (lowerLine.contains("expir") || lowerLine.contains("till") || lowerLine.contains("thru") || lowerLine.contains("valid to") || lowerLine.contains("valid upto") || lowerLine.contains("exp")) {
                    if (detectedExpiryDate == null) detectedExpiryDate = matchedDateMillis
                }
            }
        }

        // Fallback date matcher across full text if keyword match missed
        val dateMatcher = dateNumericRegex.matcher(combinedTextToScan)
        val foundDates = mutableListOf<Long>()
        while (dateMatcher.find()) {
            val dateStr = dateMatcher.group()
            for (df in dateFormatters) {
                try {
                    val parsed = df.parse(dateStr)
                    if (parsed != null) {
                        foundDates.add(parsed.time)
                        break
                    }
                } catch (_: Exception) {}
            }
        }

        if (foundDates.isNotEmpty()) {
            val now = System.currentTimeMillis()
            val pastDates = foundDates.filter { it <= now }
            val futureDates = foundDates.filter { it > now }

            if (detectedIssueDate == null && pastDates.isNotEmpty()) {
                detectedIssueDate = pastDates.maxOrNull()
            }
            if (detectedExpiryDate == null && futureDates.isNotEmpty()) {
                detectedExpiryDate = futureDates.minOrNull()
            } else if (detectedExpiryDate == null && foundDates.size >= 2) {
                if (detectedIssueDate == null) detectedIssueDate = foundDates.minOrNull()
                detectedExpiryDate = foundDates.maxOrNull()
            }
        }

        // Auto extract number & issuer if not manually provided
        if (autoExtractedNumber.isBlank()) {
            when (category.lowercase()) {
                "aadhaar card", "aadhaar" -> {
                    val match = aadhaarRegex.matcher(combinedTextToScan)
                    if (match.find()) autoExtractedNumber = match.group()
                    suggestedIssuer = "UIDAI (Govt of India)"
                }
                "pan card", "pan" -> {
                    val match = panRegex.matcher(combinedTextToScan)
                    if (match.find()) autoExtractedNumber = match.group()
                    suggestedIssuer = "Income Tax Department"
                }
                "driving licence", "driving license" -> {
                    suggestedIssuer = "Transport Department (RTO)"
                }
                "passport" -> {
                    suggestedIssuer = "Ministry of External Affairs"
                }
                "banking & finance", "bank", "account" -> {
                    val match = bankAccRegex.matcher(combinedTextToScan)
                    if (match.find()) autoExtractedNumber = match.group()
                    suggestedIssuer = "Bank / Financial Institution"
                }
            }
        }

        // Add contextual category keywords for robust searchability
        when (category.lowercase()) {
            "aadhaar card", "aadhaar" -> {
                extractedLines.add("Government of India Unique Identification Authority UIDAI Unique ID Resident Aadhaar")
            }
            "pan card", "pan" -> {
                extractedLines.add("Income Tax Department Govt of India Permanent Account Number PAN Card")
            }
            "driving licence", "driving license" -> {
                extractedLines.add("Transport Department Driving Licence Motor Vehicle Form LMV Driving Permit")
            }
            "passport" -> {
                extractedLines.add("Republic of India Passport Ministry of External Affairs Travel Document Nationality")
            }
            "voter id" -> {
                extractedLines.add("Election Commission of India Electoral Photo Identity Card EPIC Elector ID")
            }
            "vehicle", "rc" -> {
                extractedLines.add("Registration Certificate Chassis Number Engine Number Fuel Type Pollution Insurance")
            }
            "insurance" -> {
                extractedLines.add("Policy Number Insured Sum Premium Expiry Validity Cover Health Vehicle Life")
            }
            "banking & finance" -> {
                extractedLines.add("Bank Account IFSC Code Branch Savings Current Passbook Cheque Credit Card Statement")
            }
            "education" -> {
                extractedLines.add("Marksheet Certificate Degree Diploma Board University Passing Year Grade Roll Number")
            }
            else -> {
                extractedLines.add("Personal Document Record Storage Vault $docTitle $category")
            }
        }

        if (autoExtractedNumber.isNotBlank()) {
            extractedLines.add("Extracted Number / ID: $autoExtractedNumber")
        }

        val smartTags = getSuggestedTagsForCategory(category)

        return@withContext OcrResult(
            ocrText = extractedLines.joinToString("\n"),
            extractedNumber = autoExtractedNumber,
            detectedIssueDate = detectedIssueDate,
            detectedExpiryDate = detectedExpiryDate,
            suggestedIssuer = suggestedIssuer,
            suggestedTags = smartTags
        )
    }

    private suspend fun performMlKitOcr(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text, null)
                }
                .addOnFailureListener {
                    continuation.resume("", null)
                }
        } catch (e: Exception) {
            continuation.resume("", null)
        }
    }

    fun getSuggestedTagsForCategory(category: String): List<String> {
        return when (category.lowercase()) {
            "aadhaar card", "aadhaar" -> listOf("official", "identity", "government", "uidai", "kyc")
            "pan card", "pan" -> listOf("tax", "pan", "kyc", "finance", "official")
            "driving licence", "driving license" -> listOf("license", "rto", "driving", "identity", "vehicle")
            "passport" -> listOf("travel", "passport", "visa", "identity", "international")
            "voter id" -> listOf("voter", "election", "identity", "government")
            "vehicle", "rc" -> listOf("vehicle", "rc", "automobile", "rto")
            "insurance" -> listOf("insurance", "policy", "health", "claim", "financial")
            "banking & finance" -> listOf("bank", "statement", "finance", "passbook", "tax")
            "education" -> listOf("degree", "marksheet", "education", "certificate")
            else -> listOf("important", "vault", "personal", "verified")
        }
    }
}
