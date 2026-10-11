package com.example.maftuh.data

enum class LocationId(val displayNameAr: String, val subtitleAr: String) {
    APARTMENT("شقة ٤٢ (مسرح الجريمة)", "الدور الرابع - مدينة نصر"),
    STREET("شارع عباس العقاد", "مدخل العمارة وبقالة البركة"),
    STATION("قسم شرطة مدينة نصر", "غرفة التحقيق ومكتب الطب الشرعي")
}

enum class EvidenceCategory(val displayNameAr: String) {
    PHYSICAL("دليل مادي"),
    FORENSIC("أثر جنائي"),
    DOCUMENT("مستند رسمي"),
    DIGITAL("دليل رقمي"),
    TESTIMONY("شهادة عيان")
}

data class EvidenceItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val category: EvidenceCategory,
    val location: LocationId,
    val locationDetailAr: String,
    val descriptionAr: String,
    val forensicDetailAr: String,
    val relatedSuspectId: String? = null,
    val isKeyEvidence: Boolean = false,
    val posX: Float = 0f,
    val posY: Float = 0f
)

data class DialogueOption(
    val id: String,
    val textAr: String,
    val responseAr: String,
    val requiredEvidenceId: String? = null,
    val breaksAlibi: Boolean = false,
    val alibiBrokenTextAr: String? = null,
    val leadsToNodeId: String? = null
)

data class DialogueNode(
    val id: String,
    val suspectLineAr: String,
    val options: List<DialogueOption>
)

enum class AlibiStatus(val labelAr: String) {
    INTACT("سليم وموثق"),
    QUESTIONABLE("محل شك"),
    BROKEN("منهار تماماً")
}

data class Suspect(
    val id: String,
    val nameAr: String,
    val roleAr: String,
    val age: Int,
    val location: LocationId,
    val posX: Float,
    val posY: Float,
    val avatarEmoji: String,
    val initialAlibiAr: String,
    val motiveAr: String,
    val isGuilty: Boolean,
    val dialogueRootNodeId: String,
    val dialogueNodes: Map<String, DialogueNode>,
    val notesAr: List<String>
)

data class CaseEnding(
    val titleAr: String,
    val descriptionAr: String,
    val ratingAr: String,
    val stars: Int,
    val score: Int
)

data class CaseData(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val dateAr: String,
    val victimNameAr: String,
    val victimAge: Int,
    val briefingAr: String,
    val evidenceList: List<EvidenceItem>,
    val suspects: List<Suspect>,
    val correctCulpritId: String,
    val correctMotiveKey: String,
    val requiredProofIds: List<String>,
    val perfectEnding: CaseEnding,
    val partialEnding: CaseEnding,
    val wrongEnding: CaseEnding
)

data class DialogueHistoryItem(
    val speaker: String,
    val text: String,
    val isAlert: Boolean = false
)

data class CaseProgress(
    val discoveredEvidenceIds: Set<String> = emptySet(),
    val interrogatedSuspectIds: Set<String> = emptySet(),
    val brokenAlibiSuspectIds: Set<String> = emptySet(),
    val currentLocation: LocationId = LocationId.APARTMENT,
    val customNotes: List<String> = emptyList(),
    val flashlightOn: Boolean = false,
    val inspectedEvidence: EvidenceItem? = null,
    val activeSuspect: Suspect? = null,
    val currentNodeId: String? = null,
    val dialogueLog: List<DialogueHistoryItem> = emptyList(),
    val corkboardLinks: Set<Pair<String, String>> = emptySet(),
    val verdictSubmitted: Boolean = false,
    val verdictResult: VerdictEvaluation? = null,
    val score: Int = 0
)

data class VerdictEvaluation(
    val ending: CaseEnding,
    val accusedNameAr: String,
    val matchedProofsCount: Int,
    val totalRequiredCount: Int,
    val motiveCorrect: Boolean,
    val culpritCorrect: Boolean
)
