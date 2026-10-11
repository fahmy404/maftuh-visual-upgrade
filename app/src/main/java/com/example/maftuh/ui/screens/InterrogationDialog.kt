package com.example.maftuh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maftuh.data.DialogueHistoryItem
import com.example.maftuh.data.DialogueOption
import com.example.maftuh.data.Suspect
import com.example.maftuh.ui.theme.AmberGold
import com.example.maftuh.ui.theme.CrimeRed
import com.example.maftuh.ui.theme.ForensicGreen
import com.example.maftuh.ui.theme.NoirDark
import com.example.maftuh.ui.theme.NoirSurface
import com.example.maftuh.ui.theme.NoirSurfaceVariant

@Composable
fun InterrogationDialog(
    suspect: Suspect,
    currentNodeId: String?,
    dialogueLog: List<DialogueHistoryItem>,
    discoveredEvidenceIds: Set<String>,
    brokenAlibiIds: Set<String>,
    onSelectOption: (DialogueOption) -> Unit,
    onClose: () -> Unit
) {
    val isAlibiBroken = brokenAlibiIds.contains(suspect.id)
    val currentNode = suspect.dialogueNodes[currentNodeId ?: suspect.dialogueRootNodeId]
    val options = currentNode?.options ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .testTag("interrogation_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NoirSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isAlibiBroken) CrimeRed else AmberGold
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // HEADER: SUSPECT PROFILE & CLOSE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isAlibiBroken) CrimeRed.copy(alpha = 0.2f) else AmberGold.copy(alpha = 0.2f))
                                .border(1.5.dp, if (isAlibiBroken) CrimeRed else AmberGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = suspect.avatarEmoji, fontSize = 24.sp)
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = suspect.nameAr,
                                    color = AmberGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isAlibiBroken) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CrimeRed.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "🚨 منهار",
                                            color = CrimeRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "جلسة تحقيق رسمية | ${suspect.roleAr}",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_interrogation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // DIALOGUE LOG SCROLL
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(NoirSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dialogueLog) { item ->
                        if (item.isAlert) {
                            Surface(
                                color = CrimeRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CrimeRed)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimeRed, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = item.text,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            val isDetective = (item.speaker == "النقيب يوسف")
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isDetective) Alignment.Start else Alignment.End
                            ) {
                                Text(
                                    text = item.speaker,
                                    color = if (isDetective) AmberGold else ForensicGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = if (isDetective) AmberGold.copy(alpha = 0.15f) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = item.text,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // QUESTIONS & CONFRONTATIONS
                Text(
                    text = "اختر سؤالاً أو واجه المشتبه به بالأدلة المكتشفة:",
                    color = AmberGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { opt ->
                        val hasEvidence = opt.requiredEvidenceId == null || discoveredEvidenceIds.contains(opt.requiredEvidenceId)
                        val isConfrontation = opt.requiredEvidenceId != null

                        Button(
                            onClick = { if (hasEvidence) onSelectOption(opt) },
                            enabled = hasEvidence,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConfrontation) CrimeRed.copy(alpha = 0.85f) else NoirSurfaceVariant,
                                disabledContainerColor = Color.DarkGray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (hasEvidence) {
                                        if (isConfrontation) CrimeRed else AmberGold.copy(alpha = 0.5f)
                                    } else Color.Gray,
                                    RoundedCornerShape(10.dp)
                                )
                                .testTag("dialogue_opt_${opt.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (hasEvidence) opt.textAr else "🔒 يتطلب العثور على دليل إضافي لمواجهته",
                                    color = if (hasEvidence) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!hasEvidence) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
