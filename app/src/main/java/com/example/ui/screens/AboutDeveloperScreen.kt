package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AboutDeveloperScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Developer Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Curriculum Vitae & Project Portfolio",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_about_dev_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Profile Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_developer_profile"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_slogo),
                                contentDescription = "Siddharth Borisagar Logo",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SIDDHARTH B. BORISAGAR",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Rajkot, Gujarat, India",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "MCA Student • Full-Stack Developer",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Professional Summary Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = "MCA student passionate about full-stack development, backend engineering, cloud computing, and cybersecurity. Experienced in building secure, database-driven web & mobile applications. Enjoy learning new technologies through projects, hackathons, and hands-on technical workshops.",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Quick Contact Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ContactActionChip(
                                icon = Icons.Default.Call,
                                label = "+91 8460589338",
                                onClick = {
                                    openUrl(context, "tel:+918460589338")
                                }
                            )
                            ContactActionChip(
                                icon = Icons.Default.Email,
                                label = "Email",
                                onClick = {
                                    openUrl(context, "mailto:borisagarsiddharthb@gmail.com")
                                }
                            )
                            ContactActionChip(
                                icon = Icons.AutoMirrored.Filled.Launch,
                                label = "LinkedIn",
                                onClick = {
                                    openUrl(context, "https://linkedin.com/in/siddharthborisagar")
                                }
                            )
                            ContactActionChip(
                                icon = Icons.Default.Language,
                                label = "Portfolio",
                                onClick = {
                                    openUrl(context, "https://sidd.realsaleandservice.in")
                                }
                            )
                        }
                    }
                }
            }

            // Section: Projects Timeline (With Dastavej Box AUG-26 to OCT-26 highlighted)
            item {
                SectionHeader(
                    icon = Icons.Default.Work,
                    title = "PROJECTS & TIMELINE"
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Featured App: Dastavej Box (AUG-26 to OCT-26)
                    ProjectCard(
                        title = "Dastavej Box – Offline Security Document Vault",
                        timeline = "AUG-26 to OCT-26",
                        isFeatured = true,
                        technologies = "Android Studio • Kotlin • Jetpack Compose • Room Database • AndroidX Security Crypto • Biometric Auth • PDFRenderer",
                        bullets = listOf(
                            "Architected an offline-first, military-grade encrypted personal and family document vault.",
                            "Implemented AES-256-GCM encryption with Android Keystore hardware-backed MasterKey protection.",
                            "Designed multi-profile family document segregation, custom folders hub, and fast Move/Copy file management.",
                            "Built high-performance native PDF viewer and pinch-to-zoom multi-touch image viewer.",
                            "Developed password-protected encrypted ZIP backup & restore engine for zero-cloud data sovereignty.",
                            "Integrated real-time camera scanner, smart document expiry tracking, and permanent lifetime record toggles."
                        )
                    )

                    // Carpooling Platform (Odoo x KSV Hackathon 2026)
                    ProjectCard(
                        title = "Carpooling Platform",
                        timeline = "Odoo x KSV Hackathon 2026",
                        isFeatured = false,
                        technologies = "Core PHP (MVC) • MySQL • Bootstrap 5 • JavaScript • Leaflet.js • OSRM • Razorpay",
                        bullets = listOf(
                            "Developed a web-based carpooling platform for ride sharing and trip management.",
                            "Implemented user authentication, vehicle management, ride booking, and trip tracking.",
                            "Integrated interactive maps with live route visualization and ETA using Leaflet.js and OSRM.",
                            "Added wallet management and secure online payments using Razorpay.",
                            "Built ratings, reviews, travel reports, and analytics features using MVC architecture."
                        )
                    )

                    // PlateNotify (CS50x Final Project)
                    ProjectCard(
                        title = "PlateNotify (CS50x Final Project)",
                        timeline = "CS50x Final Project",
                        isFeatured = false,
                        technologies = "Python • Flask • PostgreSQL • SQLAlchemy • Telegram Bot API",
                        bullets = listOf(
                            "Built a privacy-focused anonymous vehicle notification platform.",
                            "Developed secure user authentication and vehicle management.",
                            "Integrated Telegram Bot API for instant notifications.",
                            "Implemented security hardening using Flask-Talisman and Flask-Limiter.",
                            "Designed responsive dashboard with dark mode and secure REST backend."
                        )
                    )

                    // NodeNet – Offline Lab Network File Manager
                    ProjectCard(
                        title = "NodeNet – Offline Lab Network File Manager",
                        timeline = "Academic / Independent",
                        isFeatured = false,
                        technologies = "PHP • MySQL • JavaScript",
                        bullets = listOf(
                            "Developed a decentralized browser-based file management simulator.",
                            "Built isolated user storage with secure session management.",
                            "Implemented folder hierarchy, file sharing, and built-in text editor.",
                            "Designed desktop-style interface using Vanilla JavaScript."
                        )
                    )

                    // FleetFlow – Fleet & Logistics Management System
                    ProjectCard(
                        title = "FleetFlow – Fleet & Logistics Management System",
                        timeline = "Hackathon Project",
                        isFeatured = false,
                        technologies = "Full-Stack • Analytics & Workflows",
                        bullets = listOf(
                            "Built logistics management platform with operational vehicle tracking.",
                            "Developed analytics dashboard and workflow automation.",
                            "Implemented role-based management (RBAC) modules."
                        )
                    )

                    // AI Content Studio (Android)
                    ProjectCard(
                        title = "AI Content Studio (Android)",
                        timeline = "Android Project",
                        isFeatured = false,
                        technologies = "Android Studio • Google AI Studio APIs",
                        bullets = listOf(
                            "Developed Android application for AI-powered content generation.",
                            "Integrated Google AI Studio APIs with prompt workflows.",
                            "Built responsive dark/light UI and optimized API interactions."
                        )
                    )

                    // Service Center Management System (SCMS)
                    ProjectCard(
                        title = "Service Center Management System (SCMS)",
                        timeline = "Web Project",
                        isFeatured = false,
                        technologies = "PHP • MySQL • Bootstrap",
                        bullets = listOf(
                            "Built complaint management platform with technician workflow.",
                            "Developed dashboards for administrators and dealers with CRUD operations."
                        )
                    )
                }
            }

            // Section: Education
            item {
                SectionHeader(
                    icon = Icons.Default.School,
                    title = "EDUCATION"
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EducationCard(
                        degree = "Master of Computer Applications (MCA)",
                        institution = "Saurashtra University, Rajkot",
                        duration = "2025 – 2027",
                        grade = "Grade: A"
                    )
                    EducationCard(
                        degree = "Bachelor of Computer Applications (BCA)",
                        institution = "T.N. Rao Institute of Management Research & Technology, Rajkot",
                        duration = "2022 – 2025",
                        grade = "First Class with Distinction"
                    )
                }
            }

            // Section: Technical Skills
            item {
                SectionHeader(
                    icon = Icons.Default.Code,
                    title = "TECHNICAL SKILLS"
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SkillCategoryGroup(
                            title = "Programming Languages",
                            skills = listOf("Python", "PHP", "JavaScript", "SQL", "C", "Kotlin")
                        )
                        SkillCategoryGroup(
                            title = "Frameworks & Technologies",
                            skills = listOf("Jetpack Compose", "Flask", "SQLAlchemy", "Bootstrap 5", "AJAX", "jQuery", "HTML5", "CSS3")
                        )
                        SkillCategoryGroup(
                            title = "Databases",
                            skills = listOf("PostgreSQL", "MySQL", "SQLite", "Room DB")
                        )
                        SkillCategoryGroup(
                            title = "Cloud & DevOps",
                            skills = listOf("AWS (EC2, IAM, VPC, S3 Fundamentals)", "Git", "GitHub")
                        )
                        SkillCategoryGroup(
                            title = "Development Tools",
                            skills = listOf("VS Code", "Android Studio", "XAMPP", "MySQL Workbench")
                        )
                        SkillCategoryGroup(
                            title = "Cybersecurity & System Concepts",
                            skills = listOf("Authentication & Authorization", "Role-Based Access Control (RBAC)", "Secure Backend Development", "OWASP Fundamentals", "Network Fundamentals", "Digital Forensics Basics", "PowerShell Forensics", "DFIR Fundamentals")
                        )
                        SkillCategoryGroup(
                            title = "AI & Emerging Technologies",
                            skills = listOf("Prompt Engineering", "Google AI Studio", "Generative AI Fundamentals", "AI Workflow Automation")
                        )
                    }
                }
            }

            // Section: Certifications
            item {
                SectionHeader(
                    icon = Icons.Default.CardMembership,
                    title = "CERTIFICATIONS"
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CertificationGroupCard(
                        category = "Programming & Software Development",
                        items = listOf(
                            "Harvard CS50x – Introduction to Computer Science",
                            "CS50 Introduction to Programming with Scratch",
                            "GitHub Bootcamp",
                            "Linux Mastery Course",
                            "Data Structures & Algorithms using C"
                        )
                    )
                    CertificationGroupCard(
                        category = "Artificial Intelligence",
                        items = listOf(
                            "Google AI Essentials (Specialization)",
                            "Google Prompting Essentials (Specialization)",
                            "Foundation Course on AI Readiness",
                            "AI For All Program",
                            "Introduction to AI"
                        )
                    )
                    CertificationGroupCard(
                        category = "Cloud Computing & Networking",
                        items = listOf(
                            "AWS Gujarat Students Builder Week 2026",
                            "Introduction to Network Automation (Cisco)",
                            "Introduction to Networking (NVIDIA)",
                            "Cisco HTML Essentials",
                            "Cisco Packet Tracer"
                        )
                    )
                    CertificationGroupCard(
                        category = "Cybersecurity",
                        items = listOf(
                            "Deloitte Cybersecurity Job Simulation",
                            "Cybersecurity (Tech Mahindra Foundation)",
                            "HP Introduction to Cybersecurity Awareness",
                            "Secure Mobile Practices (ISEA)"
                        )
                    )
                }
            }

            // Section: Hackathons & Achievements
            item {
                SectionHeader(
                    icon = Icons.Default.EmojiEvents,
                    title = "HACKATHONS & ACHIEVEMENTS"
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AchievementBullet(title = "Finalist, Odoo x KSV Hackathon 2026", subtitle = "Grand Finale Participant")
                        AchievementBullet(title = "AutonomousHacks 2026", subtitle = "Hackathon Participant")
                        AchievementBullet(title = "Shaastra 2K26", subtitle = "24-Hour National Hackathon")
                        AchievementBullet(title = "Build with AI", subtitle = "Code for Communities Hackathon")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "• Built multiple production-style full-stack & mobile applications.\n• Active participant in developer communities including AWS, GDG, and FlutterFlow.\n• Regular attendee of cloud, AI, cybersecurity, and software engineering workshops.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section: Workshops & Communities
            item {
                SectionHeader(
                    icon = Icons.Default.Groups,
                    title = "WORKSHOPS & TECHNICAL ACTIVITIES"
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "AWS Gujarat Students Builder Week 2026",
                        "Emerging Trends in Cloud Computing & AI (5-Day STTP)",
                        "GDG Cloud Rajkot – Build with AI",
                        "FlutterFlow Developer Group Rajkot",
                        "ISEA Cybersecurity Sessions",
                        "NFSU Digital Forensics & PowerShell Forensics",
                        "AWS User Group Rajkot Events",
                        "Google AI Studio Workshops"
                    ).forEach { workshop ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = workshop,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Section: Languages
            item {
                SectionHeader(
                    icon = Icons.Default.Language,
                    title = "LANGUAGES"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("English", "Hindi", "Gujarati").forEach { lang ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = lang,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Fluent",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun ContactActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
        },
        label = {
            Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        },
        shape = RoundedCornerShape(8.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ProjectCard(
    title: String,
    timeline: String,
    isFeatured: Boolean,
    technologies: String,
    bullets: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isFeatured) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isFeatured) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Featured App",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Timeline: $timeline",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = technologies,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            bullets.forEach { bullet ->
                Row(
                    modifier = Modifier.padding(vertical = 1.5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = bullet,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EducationCard(
    degree: String,
    institution: String,
    duration: String,
    grade: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = degree,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = institution,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = duration,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = grade,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillCategoryGroup(
    title: String,
    skills: List<String>
) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            skills.forEach { skill ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = skill,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CertificationGroupCard(
    category: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = category,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            items.forEach { cert ->
                Text(
                    text = "✓  $cert",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun AchievementBullet(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFA000),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}
