package com.example.maftuh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.maftuh.data.CaseData
import com.example.maftuh.data.CaseProgress
import com.example.maftuh.data.CaseRepository
import com.example.maftuh.data.DialogueHistoryItem
import com.example.maftuh.data.DialogueOption
import com.example.maftuh.data.EvidenceItem
import com.example.maftuh.data.LocationId
import com.example.maftuh.data.Suspect
import com.example.maftuh.data.VerdictEvaluation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ActiveScreen {
    GAME_EXPLORE,
    NOTEBOOK,
    CORKBOARD,
    ACCUSE,
    VERDICT_OUTCOME
}

data class GameUiState(
    val caseData: CaseData = CaseRepository.CASE_ONE,
    val progress: CaseProgress = CaseProgress(),
    val activeScreen: ActiveScreen = ActiveScreen.GAME_EXPLORE,
    val isMapOpen: Boolean = false,
    val isBriefingOpen: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val playerX: Float = 265f,
    val playerY: Float = 510f,
    val playerDir: String = "up",
    val activeNarrativeQuote: String = "يبدو أن هناك فوضى على الطاولة... ربما كان هناك شجار هنا. يجب أن أفتش كل الغرفة بدقة يا حضرة المحقق.",
    val inspectedEvidence: EvidenceItem? = null,
    val activeSuspect: Suspect? = null,
    val currentNodeId: String? = null,
    val dialogueLog: List<DialogueHistoryItem> = emptyList(),
    val nearbyEvidence: EvidenceItem? = null,
    val nearbySuspect: Suspect? = null,
    val alertBannerMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadSavedProgress()
    }

    private fun loadSavedProgress() {
        val saved = CaseRepository.loadProgress(getApplication())
        val defaultQuote = getQuoteForLocation(saved.currentLocation)
        _uiState.update { state ->
            state.copy(
                progress = saved,
                activeNarrativeQuote = defaultQuote,
                playerX = getSpawnX(saved.currentLocation),
                playerY = getSpawnY(saved.currentLocation)
            )
        }
    }

    private fun saveProgress() {
        CaseRepository.saveProgress(getApplication(), _uiState.value.progress)
    }

    fun openScreen(screen: ActiveScreen) {
        _uiState.update { it.copy(activeScreen = screen) }
    }

    fun closeModals() {
        _uiState.update {
            it.copy(
                activeScreen = ActiveScreen.GAME_EXPLORE,
                inspectedEvidence = null,
                activeSuspect = null,
                isMapOpen = false,
                isBriefingOpen = false,
                alertBannerMessage = null
            )
        }
    }

    fun toggleMap(open: Boolean) {
        _uiState.update { it.copy(isMapOpen = open) }
    }

    fun toggleBriefing(open: Boolean) {
        _uiState.update { it.copy(isBriefingOpen = open) }
    }

    fun toggleFlashlight() {
        _uiState.update { state ->
            val newState = !state.isFlashlightOn
            state.copy(
                isFlashlightOn = newState,
                progress = state.progress.copy(flashlightOn = newState),
                activeNarrativeQuote = if (newState) "شغّلت الكشاف الجنائي.. الآن تظهر الآثار الدقيقة وبصمات الأصابع بوضوح." else "أغلقت الكشاف الجنائي."
            )
        }
        saveProgress()
    }

    fun changeLocation(newLocation: LocationId) {
        _uiState.update { state ->
            val newProgress = state.progress.copy(currentLocation = newLocation)
            state.copy(
                progress = newProgress,
                playerX = getSpawnX(newLocation),
                playerY = getSpawnY(newLocation),
                activeNarrativeQuote = getQuoteForLocation(newLocation),
                isMapOpen = false,
                nearbyEvidence = null,
                nearbySuspect = null
            )
        }
        saveProgress()
    }

    fun setPlayerPosition(x: Float, y: Float, dir: String) {
        _uiState.update { state ->
            val loc = state.progress.currentLocation
            val evidenceInLoc = state.caseData.evidenceList.filter { it.location == loc }
            val suspectsInLoc = state.caseData.suspects.filter { it.location == loc }

            var nearbyEv: EvidenceItem? = null
            var minEvDist = Float.MAX_VALUE
            for (ev in evidenceInLoc) {
                val dist = Math.hypot((x - ev.posX).toDouble(), (y - ev.posY).toDouble()).toFloat()
                if (dist < 75f && dist < minEvDist) {
                    minEvDist = dist
                    nearbyEv = ev
                }
            }

            var nearbySus: Suspect? = null
            var minSusDist = Float.MAX_VALUE
            for (sus in suspectsInLoc) {
                val dist = Math.hypot((x - sus.posX).toDouble(), (y - sus.posY).toDouble()).toFloat()
                if (dist < 85f && dist < minSusDist) {
                    minSusDist = dist
                    nearbySus = sus
                }
            }

            state.copy(
                playerX = x,
                playerY = y,
                playerDir = dir,
                nearbyEvidence = nearbyEv,
                nearbySuspect = nearbySus
            )
        }
    }

    fun inspectEvidence(evidence: EvidenceItem) {
        _uiState.update { state ->
            val discovered = state.progress.discoveredEvidenceIds.toMutableSet()
            val wasAlreadyDiscovered = discovered.contains(evidence.id)
            discovered.add(evidence.id)

            val newScore = if (!wasAlreadyDiscovered) state.progress.score + 50 else state.progress.score
            val updatedProgress = state.progress.copy(
                discoveredEvidenceIds = discovered,
                score = newScore
            )

            state.copy(
                progress = updatedProgress,
                inspectedEvidence = evidence,
                activeNarrativeQuote = "عثرت على: ${evidence.titleAr}. سأفحصه بعناية في المختبر."
            )
        }
        saveProgress()
    }

    fun closeEvidenceInspection() {
        _uiState.update { it.copy(inspectedEvidence = null) }
    }

    fun startInterrogation(suspect: Suspect) {
        val rootNodeId = suspect.dialogueRootNodeId
        val rootNode = suspect.dialogueNodes[rootNodeId]
        val initialLog = listOf(
            DialogueHistoryItem(
                speaker = suspect.nameAr,
                text = rootNode?.suspectLineAr ?: "أهلاً يا حضرة المحقق، خير؟"
            )
        )

        _uiState.update { state ->
            val interrogated = state.progress.interrogatedSuspectIds.toMutableSet()
            interrogated.add(suspect.id)
            val updatedProgress = state.progress.copy(interrogatedSuspectIds = interrogated)

            state.copy(
                progress = updatedProgress,
                activeSuspect = suspect,
                currentNodeId = rootNodeId,
                dialogueLog = initialLog
            )
        }
        saveProgress()
    }

    fun selectDialogueOption(option: DialogueOption) {
        val suspect = _uiState.value.activeSuspect ?: return
        val currentLog = _uiState.value.dialogueLog.toMutableList()

        currentLog.add(
            DialogueHistoryItem(
                speaker = "النقيب يوسف",
                text = option.textAr
            )
        )

        currentLog.add(
            DialogueHistoryItem(
                speaker = suspect.nameAr,
                text = option.responseAr
            )
        )

        var banner: String? = null
        val brokenAlibis = _uiState.value.progress.brokenAlibiSuspectIds.toMutableSet()
        var newScore = _uiState.value.progress.score

        if (option.breaksAlibi) {
            brokenAlibis.add(suspect.id)
            banner = option.alibiBrokenTextAr ?: "انكسرت حجة غياب ${suspect.nameAr}!"
            newScore += 100
            currentLog.add(
                DialogueHistoryItem(
                    speaker = "🚨 تنبيه جنائي",
                    text = banner,
                    isAlert = true
                )
            )
        }

        _uiState.update { state ->
            val updatedProgress = state.progress.copy(
                brokenAlibiSuspectIds = brokenAlibis,
                score = newScore
            )
            state.copy(
                progress = updatedProgress,
                dialogueLog = currentLog,
                currentNodeId = option.leadsToNodeId ?: state.currentNodeId,
                alertBannerMessage = banner,
                activeNarrativeQuote = if (option.breaksAlibi) "حجة غياب ${suspect.nameAr} انهارت بالكامل!" else state.activeNarrativeQuote
            )
        }
        saveProgress()
    }

    fun closeInterrogation() {
        _uiState.update {
            it.copy(
                activeSuspect = null,
                currentNodeId = null,
                dialogueLog = emptyList(),
                alertBannerMessage = null
            )
        }
    }

    fun toggleCorkboardLink(fromId: String, toId: String) {
        _uiState.update { state ->
            val links = state.progress.corkboardLinks.toMutableSet()
            val pair = if (fromId < toId) fromId to toId else toId to fromId
            if (links.contains(pair)) {
                links.remove(pair)
            } else {
                links.add(pair)
            }
            state.copy(progress = state.progress.copy(corkboardLinks = links))
        }
        saveProgress()
    }

    fun addCustomNote(note: String) {
        if (note.isBlank()) return
        _uiState.update { state ->
            val notes = state.progress.customNotes + note.trim()
            state.copy(progress = state.progress.copy(customNotes = notes))
        }
        saveProgress()
    }

    fun submitVerdict(accusedSuspectId: String, motiveKey: String, selectedProofIds: List<String>) {
        val case = _uiState.value.caseData
        val isCulpritCorrect = (accusedSuspectId == case.correctCulpritId)
        val isMotiveCorrect = (motiveKey == case.correctMotiveKey)
        val matchedProofs = selectedProofIds.filter { case.requiredProofIds.contains(it) }

        val ending = when {
            isCulpritCorrect && isMotiveCorrect && matchedProofs.size >= 3 -> case.perfectEnding
            isCulpritCorrect -> case.partialEnding
            else -> case.wrongEnding
        }

        val suspect = case.suspects.find { it.id == accusedSuspectId }
        val eval = VerdictEvaluation(
            ending = ending,
            accusedNameAr = suspect?.nameAr ?: "المشتبه به",
            matchedProofsCount = matchedProofs.size,
            totalRequiredCount = case.requiredProofIds.size,
            motiveCorrect = isMotiveCorrect,
            culpritCorrect = isCulpritCorrect
        )

        _uiState.update { state ->
            state.copy(
                progress = state.progress.copy(
                    verdictSubmitted = true,
                    verdictResult = eval,
                    score = state.progress.score + ending.score
                ),
                activeScreen = ActiveScreen.VERDICT_OUTCOME
            )
        }
        saveProgress()
    }

    fun restartCase() {
        CaseRepository.clearProgress(getApplication())
        _uiState.update {
            GameUiState(
                progress = CaseProgress(),
                activeScreen = ActiveScreen.GAME_EXPLORE,
                playerX = 265f,
                playerY = 510f,
                activeNarrativeQuote = getQuoteForLocation(LocationId.APARTMENT)
            )
        }
    }

    private fun getSpawnX(loc: LocationId): Float = when (loc) {
        LocationId.APARTMENT -> 265f
        LocationId.STREET -> 600f
        LocationId.STATION -> 600f
    }

    private fun getSpawnY(loc: LocationId): Float = when (loc) {
        LocationId.APARTMENT -> 510f
        LocationId.STREET -> 560f
        LocationId.STATION -> 660f
    }

    private fun getQuoteForLocation(loc: LocationId): String = when (loc) {
        LocationId.APARTMENT -> "مسرح الجريمة: شقة ٤٢ بمدينة نصر. يجب فحص كل ركن بعناية، من الصالة للمطبخ وغرفة النوم."
        LocationId.STREET -> "شارع عباس العقاد: كاميرات المراقبة الخارجية وشهود الشارع قد يحملون مفتاح حل اللغز."
        LocationId.STATION -> "قسم شرطة مدينة نصر: مكتب الطب الشرعي وغرفة التحقيقات الجنائية جاهزان."
    }
}
