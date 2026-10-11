package com.example.maftuh.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.maftuh.ui.components.DetectiveHUD
import com.example.maftuh.ui.components.SceneCanvasView
import com.example.maftuh.ui.theme.NoirDark
import com.example.maftuh.ui.viewmodel.ActiveScreen
import com.example.maftuh.ui.viewmodel.GameViewModel

@Composable
fun DetectiveGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    // Handle back button on mobile: closes open sub-screens or modals
    BackHandler(
        enabled = state.activeScreen != ActiveScreen.GAME_EXPLORE ||
                state.inspectedEvidence != null ||
                state.activeSuspect != null ||
                state.isMapOpen ||
                state.isBriefingOpen
    ) {
        viewModel.closeModals()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NoirDark)
    ) {
        // 1. Primary 2D Interactive Scene
        val loc = state.progress.currentLocation
        val evidenceInLoc = state.caseData.evidenceList.filter { it.location == loc }
        val suspectsInLoc = state.caseData.suspects.filter { it.location == loc }

        SceneCanvasView(
            location = loc,
            playerX = state.playerX,
            playerY = state.playerY,
            playerDir = state.playerDir,
            flashlightOn = state.isFlashlightOn,
            evidenceList = evidenceInLoc,
            discoveredEvidenceIds = state.progress.discoveredEvidenceIds,
            suspects = suspectsInLoc,
            onMovePlayer = { x, y, dir -> viewModel.setPlayerPosition(x, y, dir) },
            onTapEvidence = { ev -> viewModel.inspectEvidence(ev) },
            onTapSuspect = { sus -> viewModel.startInterrogation(sus) }
        )

        // 2. Detective HUD & Controls
        DetectiveHUD(
            state = state,
            onOpenScreen = { screen -> viewModel.openScreen(screen) },
            onToggleMap = { viewModel.toggleMap(!state.isMapOpen) },
            onToggleBriefing = { viewModel.toggleBriefing(!state.isBriefingOpen) },
            onToggleFlashlight = { viewModel.toggleFlashlight() },
            onInteractEvidence = { ev -> viewModel.inspectEvidence(ev) },
            onInteractSuspect = { sus -> viewModel.startInterrogation(sus) },
            onMoveDirection = { dx, dy, dir ->
                val newX = state.playerX + dx
                val newY = state.playerY + dy
                viewModel.setPlayerPosition(newX, newY, dir)
            }
        )

        // 3. Modals & Screens
        when (state.activeScreen) {
            ActiveScreen.NOTEBOOK -> {
                NotebookScreen(
                    caseData = state.caseData,
                    progress = state.progress,
                    onClose = { viewModel.openScreen(ActiveScreen.GAME_EXPLORE) },
                    onInspectEvidence = { ev -> viewModel.inspectEvidence(ev) },
                    onInterrogateSuspect = { sus -> viewModel.startInterrogation(sus) },
                    onAddNote = { note -> viewModel.addCustomNote(note) }
                )
            }
            ActiveScreen.CORKBOARD -> {
                EvidenceBoardScreen(
                    caseData = state.caseData,
                    progress = state.progress,
                    onClose = { viewModel.openScreen(ActiveScreen.GAME_EXPLORE) },
                    onToggleLink = { from, to -> viewModel.toggleCorkboardLink(from, to) },
                    onInspectEvidence = { ev -> viewModel.inspectEvidence(ev) }
                )
            }
            ActiveScreen.ACCUSE -> {
                AccusationScreen(
                    caseData = state.caseData,
                    progress = state.progress,
                    onClose = { viewModel.openScreen(ActiveScreen.GAME_EXPLORE) },
                    onSubmitVerdict = { susId, motive, proofs ->
                        viewModel.submitVerdict(susId, motive, proofs)
                    }
                )
            }
            ActiveScreen.VERDICT_OUTCOME -> {
                state.progress.verdictResult?.let { eval ->
                    VerdictOutcomeScreen(
                        evaluation = eval,
                        onRestart = { viewModel.restartCase() },
                        onClose = { viewModel.openScreen(ActiveScreen.GAME_EXPLORE) }
                    )
                }
            }
            ActiveScreen.GAME_EXPLORE -> {
                // Main exploration view
            }
        }

        // Interrogation modal
        state.activeSuspect?.let { sus ->
            InterrogationDialog(
                suspect = sus,
                currentNodeId = state.currentNodeId,
                dialogueLog = state.dialogueLog,
                discoveredEvidenceIds = state.progress.discoveredEvidenceIds,
                brokenAlibiIds = state.progress.brokenAlibiSuspectIds,
                onSelectOption = { opt -> viewModel.selectDialogueOption(opt) },
                onClose = { viewModel.closeInterrogation() }
            )
        }

        // Evidence Zoom-In Inspection modal
        state.inspectedEvidence?.let { ev ->
            EvidenceInspectionDialog(
                evidence = ev,
                onClose = { viewModel.closeEvidenceInspection() }
            )
        }

        // Cairo Map Fast Travel modal
        if (state.isMapOpen) {
            MapDialog(
                currentLocation = state.progress.currentLocation,
                onSelectLocation = { loc -> viewModel.changeLocation(loc) },
                onClose = { viewModel.toggleMap(false) }
            )
        }

        // Case Briefing Overview modal
        if (state.isBriefingOpen) {
            CaseBriefingDialog(
                caseData = state.caseData,
                onClose = { viewModel.toggleBriefing(false) }
            )
        }
    }
}
