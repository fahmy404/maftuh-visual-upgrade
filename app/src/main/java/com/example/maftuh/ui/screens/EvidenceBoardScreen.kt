package com.example.maftuh.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maftuh.data.CaseData
import com.example.maftuh.data.CaseProgress
import com.example.maftuh.data.EvidenceItem
import com.example.maftuh.data.Suspect
import com.example.maftuh.ui.theme.AmberGold
import com.example.maftuh.ui.theme.CorkboardBeige
import com.example.maftuh.ui.theme.CorkboardDark
import com.example.maftuh.ui.theme.CrimeRed
import com.example.maftuh.ui.theme.ForensicGreen
import com.example.maftuh.ui.theme.NoirDark
import com.example.maftuh.ui.theme.NoirSurface
import com.example.maftuh.ui.theme.PolaroidWhite
import com.example.maftuh.ui.theme.RedYarn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceBoardScreen(
    caseData: CaseData,
    progress: CaseProgress,
    onClose: () -> Unit,
    onToggleLink: (fromId: String, toId: String) -> Unit,
    onInspectEvidence: (EvidenceItem) -> Unit
) {
    var selectedPinId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📌 لوحة التحقيق والربط الجنائي",
                            color = AmberGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "انقر على عنصرين متتاليين لربطهما بخيط أحمر",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("corkboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = AmberGold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = NoirSurface)
            )
        },
        containerColor = CorkboardDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CorkboardDark)
        ) {
            // Corkboard Background Texture
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background wood cork base
                drawRect(CorkboardBeige)

                // Crime Scene Yellow Caution Line
                drawRect(
                    color = AmberGold.copy(alpha = 0.85f),
                    topLeft = Offset(0f, 20f),
                    size = androidx.compose.ui.geometry.Size(size.width, 30f)
                )

                // Red Yarn Links
                // Simplified visualization of links
                for (link in progress.corkboardLinks) {
                    drawLine(
                        color = RedYarn,
                        start = Offset(size.width * 0.25f, size.height * 0.4f),
                        end = Offset(size.width * 0.75f, size.height * 0.6f),
                        strokeWidth = 3f
                    )
                }
            }

            // Pinned Cards Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP ROW: SUSPECT POLAROIDS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    caseData.suspects.forEach { sus ->
                        val isSelected = selectedPinId == sus.id
                        val isBroken = progress.brokenAlibiSuspectIds.contains(sus.id)

                        PolaroidCard(
                            title = sus.nameAr,
                            subtitle = if (isBroken) "حجة منهارة" else sus.roleAr,
                            emoji = sus.avatarEmoji,
                            isSelected = isSelected,
                            isWarning = isBroken,
                            onClick = {
                                if (selectedPinId == null) {
                                    selectedPinId = sus.id
                                } else {
                                    if (selectedPinId != sus.id) {
                                        onToggleLink(selectedPinId!!, sus.id)
                                    }
                                    selectedPinId = null
                                }
                            }
                        )
                    }
                }

                // CENTER BANNER: ACTIVE PIN STATUS
                Surface(
                    color = NoirSurface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = if (selectedPinId != null) CrimeRed else AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (selectedPinId != null)
                                "محدد للربط: $selectedPinId (اختر عنصراً آخر)"
                            else
                                "الروابط الجنائية النشطة: ${progress.corkboardLinks.size} خيط أحمر",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // BOTTOM ROW: DISCOVERED CLUES PINNED
                val discoveredClues = caseData.evidenceList.filter {
                    progress.discoveredEvidenceIds.contains(it.id)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (discoveredClues.isEmpty()) {
                        Text(
                            text = "لم تجمع أي أدلة بعد لتعليقها على اللوحة. فتش مسرح الجريمة أولاً!",
                            color = NoirDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        discoveredClues.take(4).forEach { ev ->
                            val isSelected = selectedPinId == ev.id
                            PolaroidCard(
                                title = ev.titleAr,
                                subtitle = ev.category.displayNameAr,
                                emoji = "🔎",
                                isSelected = isSelected,
                                isWarning = false,
                                onClick = {
                                    if (selectedPinId == null) {
                                        selectedPinId = ev.id
                                    } else {
                                        if (selectedPinId != ev.id) {
                                            onToggleLink(selectedPinId!!, ev.id)
                                        }
                                        selectedPinId = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PolaroidCard(
    title: String,
    subtitle: String,
    emoji: String,
    isSelected: Boolean,
    isWarning: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(82.dp)
            .clickable { onClick() }
            .rotate(if (isSelected) 0f else -2f),
        colors = CardDefaults.cardColors(containerColor = PolaroidWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) CrimeRed else Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pin head
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CrimeRed else AmberGold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Photo box
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(if (isWarning) CrimeRed.copy(alpha = 0.2f) else NoirDark),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = NoirDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = if (isWarning) CrimeRed else Color.DarkGray,
                fontSize = 8.sp,
                maxLines = 1
            )
        }
    }
}
