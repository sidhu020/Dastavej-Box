package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AboutAppScreen(
    onNavigateToDeveloper: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "About Dastavej Box",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Architecture, Features & Technologies",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("btn_about_app_drawer")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                        }
                    } else {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("btn_about_app_back")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToDeveloper,
                        modifier = Modifier.testTag("btn_top_about_dev")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "About Developer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero App Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_about_app_hero"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_dastavej_logo),
                                contentDescription = "Dastavej Box Logo",
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "DASTAVEJ BOX",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Offline Military-Grade Security Document Vault",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("v2.4 Pro Edition", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text("100% Offline & Private", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Dastavej Box is an air-gapped, zero-cloud personal document vault engineered to securely store, organize, and safeguard critical government IDs, financial contracts, family health records, and vehicle documents with on-device 256-bit AES encryption.",
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Developer Banner CTA
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_about_dev_cta"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Developed by Siddharth Borisagar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "MCA Student • Full-Stack & Security Enthusiast",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onNavigateToDeveloper,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_about_developer_profile")
                        ) {
                            Text("View CV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Section: Key Features
            item {
                Text(
                    text = "KEY FEATURES & CAPABILITIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureDetailItem(
                        icon = Icons.Default.Security,
                        iconColor = Color(0xFF1E88E5),
                        title = "Military-Grade Local Encryption",
                        description = "Every file is encrypted at rest using AES-256-GCM cryptography. Encryption keys are securely derived and stored inside Android Keystore Hardware."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.Fingerprint,
                        iconColor = Color(0xFF43A047),
                        title = "Biometric & Master PIN Shield",
                        description = "Quick and secure unlocking via fingerprint, face recognition, or 4-digit Master PIN. Includes verified PIN reset and biometric safety fallbacks."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.Folder,
                        iconColor = Color(0xFFFB8C00),
                        title = "Multi-Folder Hub & File Organization",
                        description = "Create customizable folders, organize documents by hierarchy, and seamlessly Move or Copy files across folders with live stats."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.FamilyRestroom,
                        iconColor = Color(0xFF8E24AA),
                        title = "Family Profile Segregation",
                        description = "Separate records for each family member (Self, Spouse, Children, Parents). Switch profiles instantly with one tap to inspect linked documents."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.PictureAsPdf,
                        iconColor = Color(0xFFE53935),
                        title = "Built-in PDF & High-Res Image Viewers",
                        description = "Native hardware-accelerated PDF multi-page rendering and interactive image viewer with full pinch-to-zoom and pan controls."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.CameraAlt,
                        iconColor = Color(0xFF00ACC1),
                        title = "In-App Document Camera Scanner",
                        description = "Directly capture identity cards, receipts, and physical papers with auto-timestamping and instant vault storage."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.EventNote,
                        iconColor = Color(0xFF3949AB),
                        title = "Expiry Alerts & Lifetime Records",
                        description = "Track upcoming document renewals with status banners or mark permanent documents (Aadhaar, PAN, degrees) with No-Expiry flags."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.Archive,
                        iconColor = Color(0xFF5E35B1),
                        title = "Encrypted ZIP Backup & Restore",
                        description = "Export and import your entire vault (documents, folders, categories, and profiles) as a password-protected encrypted archive."
                    )
                    FeatureDetailItem(
                        icon = Icons.Default.DeleteSweep,
                        iconColor = Color(0xFFD81B60),
                        title = "Safe Recycle Bin & Shredding",
                        description = "Accidental deletion protection with two-stage trash management. Restore documents anytime or permanently shred them."
                    )
                }
            }

            // Section: Technologies & Architecture
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "TECHNOLOGY STACK & ARCHITECTURE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Layer / Component",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(0.38f)
                            )
                            Text(
                                text = "Technology & Implementation",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(0.62f)
                            )
                        }

                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        val techStack = listOf(
                            "UI Framework" to "Jetpack Compose (100% Declarative)",
                            "Design System" to "Material Design 3 (M3) Dynamic Theming",
                            "Core Language" to "Kotlin 2.0+ with Coroutines & StateFlow",
                            "Local Database" to "Room SQLite ORM with KSP & Migrations",
                            "Security Engine" to "AndroidX Security Crypto (MasterKeys, AES-256 GCM)",
                            "Biometrics" to "AndroidX Biometric API (Class 3 Hardware)",
                            "PDF Engine" to "Android Native PdfRenderer (Zero External Bloat)",
                            "Image Engine" to "Coil Image Pipeline + Custom Gesture Matrix",
                            "Architecture" to "MVVM + Clean Architecture Pattern",
                            "Backup System" to "Deflater ZipOutputStream with Key-Derived Cipher"
                        )

                        techStack.forEachIndexed { index, (label, value) ->
                            TechStackTableRow(
                                label = label,
                                value = value,
                                isEven = index % 2 == 0
                            )
                            if (index < techStack.size - 1) {
                                androidx.compose.material3.HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }
            }

            // Section: Target Use Cases
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "TARGET USE CASES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UseCaseChip(title = "Government IDs", subtitle = "Aadhaar, PAN, Passport, Voter ID")
                    UseCaseChip(title = "Vehicle Documents", subtitle = "RC Book, Driving License, Insurance, PUC")
                    UseCaseChip(title = "Academic Records", subtitle = "Degree Certificates, Transcripts, Diplomas")
                    UseCaseChip(title = "Healthcare & Medical", subtitle = "Prescriptions, Vaccine Records, Lab Reports")
                    UseCaseChip(title = "Financial & Tax", subtitle = "IT Returns, Bank Statements, Mutual Funds")
                    UseCaseChip(title = "Family Records", subtitle = "Birth Certificates, Marriage Deeds, Wills")
                }
            }

            // Section: Security & Privacy Guarantee
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zero-Cloud Sovereign Privacy Policy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "• 100% Offline: No servers, no tracking, no external API telemetry.\n• Sandboxed Storage: Document files are saved inside isolated private application internal memory.\n• Cryptographic Integrity: Master PIN and encryption keys never leave your physical Android device.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bottom CTA
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToDeveloper,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_bottom_meet_developer"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About Developer & Professional CV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FeatureDetailItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TechStackTableRow(label: String, value: String, isEven: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isEven) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.62f)
        )
    }
}

@Composable
private fun UseCaseChip(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
