package com.example.maftuh.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maftuh.data.AlibiStatus
import com.example.maftuh.data.CaseData
import com.example.maftuh.data.CaseProgress
import com.example.maftuh.data.EvidenceItem
import com.example.maftuh.data.Suspect
import com.example.maftuh.ui.theme.AmberGold
import com.example.maftuh.ui.theme.CrimeRed
import com.example.maftuh.ui.theme.ForensicGreen
import com.example.maftuh.ui.theme.NoirBorder
import com.example.maftuh.ui.theme.NoirDark
import com.example.maftuh.ui.theme.NoirSurface
import com.example.maftuh.ui.theme.NoirSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
    caseData: CaseData,
    progress: CaseProgress,
    onClose: () -> Unit,
    onInspectEvidence: (EvidenceItem) -> Unit,
    onInterrogateSuspect: (Suspect) -> Unit,
    onAddNote: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("الأدلة الجنائية", "المشتبه بهم", "الجدول الزمني", "ملف القضية")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "📒 دفتر تحقيق النقيب يوسف",
                        color = AmberGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("notebook_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = AmberGold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NoirSurface
                )
            )
        },
        containerColor = NoirDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = NoirSurface,
                contentColor = AmberGold,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .background(AmberGold)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) AmberGold else Color.Gray
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> EvidenceTabContent(
                        evidenceList = caseData.evidenceList,
                        discoveredIds = progress.discoveredEvidenceIds,
                        onInspectEvidence = onInspectEvidence
                    )
                    1 -> SuspectsTabContent(
                        suspects = caseData.suspects,
                        brokenAlibiIds = progress.brokenAlibiSuspectIds,
                        interrogatedIds = progress.interrogatedSuspectIds,
                        onInterrogateSuspect = onInterrogateSuspect
                    )
                    2 -> TimelineTabContent(
                        customNotes = progress.customNotes,
                        onAddNote = onAddNote
                    )
                    3 -> CaseBriefingTabContent(caseData = caseData)
                }
            }
        }
    }
}

@Composable
private fun EvidenceTabContent(
    evidenceList: List<EvidenceItem>,
    discoveredIds: Set<String>,
    onInspectEvidence: (EvidenceItem) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(evidenceList) { ev ->
            val isDiscovered = discoveredIds.contains(ev.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isDiscovered) { onInspectEvidence(ev) }
                    .testTag("evidence_item_${ev.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDiscovered) NoirSurface else NoirSurfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDiscovered) AmberGold.copy(alpha = 0.5f) else NoirBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Badge Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDiscovered) AmberGold.copy(alpha = 0.2f)
                                else Color.DarkGray.copy(alpha = 0.3f)
                            )
                            .border(
                                1.dp,
                                if (isDiscovered) AmberGold else Color.Gray,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDiscovered) {
                            Text(text = "🔍", fontSize = 18.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "غير مكتشف",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isDiscovered) ev.titleAr else "دليل مجهول لم يُكتشف بعد",
                                color = if (isDiscovered) AmberGold else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isDiscovered && ev.isKeyEvidence) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CrimeRed.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "دليل جوهري",
                                        color = CrimeRed,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDiscovered) "الموقع: ${ev.locationDetailAr}" else "ابحث في مسرح الجريمة أو الشارع",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    if (isDiscovered) {
                        Text(
                            text = "فحص ↵",
                            color = ForensicGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuspectsTabContent(
    suspects: List<Suspect>,
    brokenAlibiIds: Set<String>,
    interrogatedIds: Set<String>,
    onInterrogateSuspect: (Suspect) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(suspects) { sus ->
            val isBroken = brokenAlibiIds.contains(sus.id)
            val isInterrogated = interrogatedIds.contains(sus.id)
            val alibiStatus = when {
                isBroken -> AlibiStatus.BROKEN
                isInterrogated -> AlibiStatus.QUESTIONABLE
                else -> AlibiStatus.INTACT
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInterrogateSuspect(sus) }
                    .testTag("suspect_item_${sus.id}"),
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isBroken) CrimeRed else AmberGold.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = sus.avatarEmoji, fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sus.nameAr,
                                color = AmberGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sus.roleAr} (${sus.age} سنة)",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }

                        // Alibi Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (alibiStatus) {
                                AlibiStatus.BROKEN -> CrimeRed.copy(alpha = 0.25f)
                                AlibiStatus.QUESTIONABLE -> AmberGold.copy(alpha = 0.25f)
                                AlibiStatus.INTACT -> ForensicGreen.copy(alpha = 0.25f)
                            }
                        ) {
                            Text(
                                text = alibiStatus.labelAr,
                                color = when (alibiStatus) {
                                    AlibiStatus.BROKEN -> CrimeRed
                                    AlibiStatus.QUESTIONABLE -> AmberGold
                                    AlibiStatus.INTACT -> ForensicGreen
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "الادعاء الأولي: \"${sus.initialAlibiAr}\"",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الدافع المحتمل: ${sus.motiveAr}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onInterrogateSuspect(sus) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberGold, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "بدء جلسة الاستجواب والمواجهة بالدليل ↵",
                            color = AmberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineTabContent(
    customNotes: List<String>,
    onAddNote: (String) -> Unit
) {
    var newNoteText by remember { mutableStateOf("") }

    val timelineEvents = listOf(
        "8:00 م" to "الزوجة دينا تغادر الشقة لزيارة شقيقتها في مصر الجديدة (مثبت بفاتورة الصيدلية).",
        "10:00 م" to "شريف يزعم إغلاقه محله التجاري وتوجهه لمنزله بالنزهة دون المرور على أحمد.",
        "10:05 م" to "رسالة صوتية مرسلة من شريف لهاتف الضحية: 'أنا طالعلك الشقة حالاً يا أحمد'.",
        "10:12 م" to "كاميرا سوبرماركت البركة ترصد ركن سيارة شريف السوداء أمام العمارة.",
        "10:15 - 10:35 م" to "التوقيت البيولوجي الفعلي للوفاة طبقاً لتقرير الطبيبة الشرعية د. سلمى.",
        "10:38 م" to "شريف يفر مسرعاً ومرتبكاً من مدخل العمارة ويستقل سيارته.",
        "10:45 م" to "توقيت ساعة الحائط المحطمة عمداً بكسر تروسها الداخلية لتضليل المحققين!"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "⏱️ الجدول الزمني لإعادة تمثيل الجريمة",
                color = AmberGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(timelineEvents) { (time, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NoirSurface, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = AmberGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = time,
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = desc,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "✍️ ملاحظات المحقق الإضافية",
                color = AmberGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(customNotes) { note ->
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "• $note",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    placeholder = { Text("أضف ملاحظة تحقيق جديدة...", fontSize = 11.sp, color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = NoirBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Button(
                    onClick = {
                        if (newNoteText.isNotBlank()) {
                            onAddNote(newNoteText)
                            newNoteText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة", tint = NoirDark)
                }
            }
        }
    }
}

@Composable
private fun CaseBriefingTabContent(caseData: CaseData) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = caseData.titleAr,
                        color = AmberGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${caseData.subtitleAr} | ${caseData.dateAr}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "المجني عليه: ${caseData.victimNameAr} (${caseData.victimAge} سنة)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = caseData.briefingAr,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
