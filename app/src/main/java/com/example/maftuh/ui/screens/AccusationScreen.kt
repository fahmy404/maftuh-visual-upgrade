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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.maftuh.data.CaseData
import com.example.maftuh.data.CaseProgress
import com.example.maftuh.data.VerdictEvaluation
import com.example.maftuh.ui.theme.AmberGold
import com.example.maftuh.ui.theme.CrimeRed
import com.example.maftuh.ui.theme.ForensicGreen
import com.example.maftuh.ui.theme.NoirBorder
import com.example.maftuh.ui.theme.NoirDark
import com.example.maftuh.ui.theme.NoirSurface
import com.example.maftuh.ui.theme.NoirSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccusationScreen(
    caseData: CaseData,
    progress: CaseProgress,
    onClose: () -> Unit,
    onSubmitVerdict: (suspectId: String, motiveKey: String, proofIds: List<String>) -> Unit
) {
    var selectedSuspectId by remember { mutableStateOf(caseData.suspects.first().id) }
    var selectedMotiveKey by remember { mutableStateOf("embezzlement") }
    val selectedProofIds = remember { mutableStateListOf<String>() }

    val motives = listOf(
        "embezzlement" to "اختلاس مالي وافتضاح أمره وتهديده بالحبس والنيابة",
        "family_dispute" to "خلافات عائلية وشخصية حول الإرث والعلاقات",
        "robbery" to "سطو بدافع سرقة أموال الخزينة بالصدفة",
        "accidental" to "شجار عفوي وتدافع مفاجئ دون تخطيط مسبق"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "⚖️ جلسة توجيه الاتهام الرسمي",
                        color = CrimeRed,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("accuse_back_button")
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
        containerColor = NoirDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "١. حدد المتهم الرئيسي بالجريمة:",
                    color = AmberGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(caseData.suspects) { sus ->
                val isSelected = (selectedSuspectId == sus.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSuspectId = sus.id }
                        .testTag("accuse_suspect_${sus.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CrimeRed.copy(alpha = 0.2f) else NoirSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CrimeRed else NoirBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedSuspectId = sus.id },
                            colors = RadioButtonDefaults.colors(selectedColor = CrimeRed)
                        )
                        Text(text = sus.avatarEmoji, fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sus.nameAr,
                                color = if (isSelected) CrimeRed else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = sus.roleAr,
                                color = Color.LightGray,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "٢. حدد الدافع الجنائي المباشر:",
                    color = AmberGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(motives) { (key, label) ->
                val isSelected = (selectedMotiveKey == key)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMotiveKey = key },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AmberGold.copy(alpha = 0.15f) else NoirSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AmberGold else NoirBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedMotiveKey = key },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberGold)
                        )
                        Text(
                            text = label,
                            color = if (isSelected) AmberGold else Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "٣. اختر الأدلة الدامغة المؤيدة للإدانة:",
                    color = AmberGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val discoveredClues = caseData.evidenceList.filter {
                progress.discoveredEvidenceIds.contains(it.id)
            }

            if (discoveredClues.isEmpty()) {
                item {
                    Text(
                        text = "لم تجمع أدلة بعد! ابحث في مسرح الجريمة أولاً لتملك أدلة تقدمها للنيابة.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            } else {
                items(discoveredClues) { ev ->
                    val isChecked = selectedProofIds.contains(ev.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedProofIds.remove(ev.id)
                                else selectedProofIds.add(ev.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) ForensicGreen.copy(alpha = 0.15f) else NoirSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isChecked) ForensicGreen else NoirBorder
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedProofIds.add(ev.id)
                                    else selectedProofIds.remove(ev.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = ForensicGreen)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.titleAr,
                                    color = if (isChecked) ForensicGreen else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ev.category.displayNameAr,
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onSubmitVerdict(selectedSuspectId, selectedMotiveKey, selectedProofIds.toList())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimeRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_verdict_button")
                ) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إصدار قرار الإحالة للمحكمة الجنائية ↵",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VerdictOutcomeScreen(
    evaluation: VerdictEvaluation,
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NoirSurface),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (evaluation.culpritCorrect) AmberGold else CrimeRed
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Stars & Rating
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(evaluation.ending.stars) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = evaluation.ending.ratingAr,
                    color = AmberGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = evaluation.ending.titleAr,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Detailed Narrative Resolution
                Surface(
                    color = NoirSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = evaluation.ending.descriptionAr,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Score Earned
                Surface(
                    color = ForensicGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "النقاط المكتسبة: +${evaluation.ending.score} نقطة",
                        color = ForensicGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restart_case_button")
                    ) {
                        Text(
                            text = "إعادة التحقيق ↺",
                            color = NoirDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = NoirSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "العودة للمسرح",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
