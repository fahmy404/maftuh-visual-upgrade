package com.example.maftuh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maftuh.data.EvidenceItem
import com.example.maftuh.data.LocationId
import com.example.maftuh.data.Suspect
import com.example.maftuh.ui.theme.AmberGold
import com.example.maftuh.ui.theme.CrimeRed
import com.example.maftuh.ui.theme.ForensicGreen
import com.example.maftuh.ui.theme.NoirDark
import com.example.maftuh.ui.theme.NoirSurface
import com.example.maftuh.ui.viewmodel.ActiveScreen
import com.example.maftuh.ui.viewmodel.GameUiState

@Composable
fun DetectiveHUD(
    state: GameUiState,
    onOpenScreen: (ActiveScreen) -> Unit,
    onToggleMap: () -> Unit,
    onToggleBriefing: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onInteractEvidence: (EvidenceItem) -> Unit,
    onInteractSuspect: (Suspect) -> Unit,
    onMoveDirection: (dx: Float, dy: Float, dir: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // TOP BAR HUD: Location, Score, Flashlight, Map, Case Briefing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NoirSurface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🌙 11:15 م",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "|",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = state.progress.currentLocation.displayNameAr,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Top Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Flashlight Toggle
                IconButton(
                    onClick = onToggleFlashlight,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.isFlashlightOn) AmberGold.copy(alpha = 0.25f)
                            else NoirSurface.copy(alpha = 0.9f)
                        )
                        .border(
                            1.dp,
                            if (state.isFlashlightOn) AmberGold else Color.DarkGray,
                            CircleShape
                        )
                        .testTag("flashlight_button")
                ) {
                    Icon(
                        imageVector = if (state.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "الكشاف الجنائي",
                        tint = if (state.isFlashlightOn) AmberGold else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Map Fast Travel
                IconButton(
                    onClick = onToggleMap,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NoirSurface.copy(alpha = 0.9f))
                        .border(1.dp, AmberGold.copy(alpha = 0.4f), CircleShape)
                        .testTag("map_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "الخريطة",
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Case Briefing
                IconButton(
                    onClick = onToggleBriefing,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NoirSurface.copy(alpha = 0.9f))
                        .border(1.dp, AmberGold.copy(alpha = 0.4f), CircleShape)
                        .testTag("briefing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "ملف القضية",
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // TOP-RIGHT QUEST OBJECTIVES CARD
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 12.dp)
                .widthIn(max = 210.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NoirSurface.copy(alpha = 0.92f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 مهام التحقيق",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.progress.score} نقطة",
                        color = ForensicGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Objective 1: Collect Evidence
                val evCount = state.progress.discoveredEvidenceIds.size
                ObjectiveItem(
                    text = "جمع الأدلة ($evCount/10)",
                    isCompleted = evCount >= 6
                )

                // Objective 2: Interrogate Suspects
                val susCount = state.progress.interrogatedSuspectIds.size
                ObjectiveItem(
                    text = "استجواب المشتبه بهم ($susCount/4)",
                    isCompleted = susCount >= 2
                )

                // Objective 3: Break Alibi
                val brokenCount = state.progress.brokenAlibiSuspectIds.size
                ObjectiveItem(
                    text = "كسر حجة غياب ($brokenCount)",
                    isCompleted = brokenCount >= 1
                )
            }
        }

        // PROXIMITY INTERACTION POPUP BANNER (Near Evidence or Suspect)
        AnimatedVisibility(
            visible = state.nearbyEvidence != null || state.nearbySuspect != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp)
        ) {
            when {
                state.nearbyEvidence != null -> {
                    val ev = state.nearbyEvidence
                    Button(
                        onClick = { onInteractEvidence(ev) },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .testTag("inspect_action_button")
                            .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NoirDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فحص: ${ev.titleAr}",
                            color = NoirDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                state.nearbySuspect != null -> {
                    val sus = state.nearbySuspect
                    Button(
                        onClick = { onInteractSuspect(sus) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForensicGreen),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .testTag("interrogate_action_button")
                            .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                    ) {
                        Text(text = sus.avatarEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "استجواب: ${sus.nameAr}",
                            color = NoirDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // BOTTOM-LEFT CAPTAIN YOUSSEF MONOLOGUE THOUGHT BOX
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp)
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NoirSurface.copy(alpha = 0.95f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Detective Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberGold.copy(alpha = 0.2f))
                        .border(1.dp, AmberGold, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🕵️‍♂️", fontSize = 22.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "النقيب يوسف المصري",
                        color = AmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.activeNarrativeQuote,
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // BOTTOM-RIGHT PRIMARY DETECTIVE TOOLS (Notebook, Corkboard, Accuse)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Case Notebook
            BadgedBox(
                badge = {
                    Badge(containerColor = AmberGold) {
                        Text(
                            text = "${state.progress.discoveredEvidenceIds.size}",
                            color = NoirDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) {
                IconButton(
                    onClick = { onOpenScreen(ActiveScreen.NOTEBOOK) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NoirSurface.copy(alpha = 0.95f))
                        .border(1.5.dp, AmberGold, CircleShape)
                        .testTag("open_notebook_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "دفتر القضية",
                        tint = AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Evidence Board / Corkboard
            IconButton(
                onClick = { onOpenScreen(ActiveScreen.CORKBOARD) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NoirSurface.copy(alpha = 0.95f))
                    .border(1.5.dp, AmberGold, CircleShape)
                    .testTag("open_corkboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "لوحة التحقيق",
                    tint = AmberGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Indictment / Accuse Verdict Trial
            IconButton(
                onClick = { onOpenScreen(ActiveScreen.ACCUSE) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CrimeRed)
                    .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                    .testTag("open_accuse_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "توجيه الاتهام",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // TOUCH D-PAD CONTROLS FOR MOBILE NAVIGATION (CENTER-LEFT OF SCREEN)
        DPadController(
            onMove = onMoveDirection,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        )
    }
}

@Composable
private fun ObjectiveItem(
    text: String,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isCompleted) ForensicGreen.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                    1.dp,
                    if (isCompleted) ForensicGreen else Color.Gray,
                    RoundedCornerShape(3.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ForensicGreen,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        Text(
            text = text,
            color = if (isCompleted) ForensicGreen else Color.LightGray,
            fontSize = 9.5.sp,
            fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DPadController(
    onMove: (dx: Float, dy: Float, dir: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val step = 35f
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NoirSurface.copy(alpha = 0.85f))
            .border(1.dp, AmberGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // UP
        IconButton(
            onClick = { onMove(0f, -step, "up") },
            modifier = Modifier
                .size(36.dp)
                .testTag("dpad_up")
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "أعلى", tint = AmberGold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            // LEFT
            IconButton(
                onClick = { onMove(-step, 0f, "left") },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("dpad_left")
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "يسار", tint = AmberGold)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AmberGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🚶", fontSize = 14.sp)
            }
            // RIGHT
            IconButton(
                onClick = { onMove(step, 0f, "right") },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("dpad_right")
            ) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "يمين", tint = AmberGold)
            }
        }
        // DOWN
        IconButton(
            onClick = { onMove(0f, step, "down") },
            modifier = Modifier
                .size(36.dp)
                .testTag("dpad_down")
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "أسفل", tint = AmberGold)
        }
    }
}
