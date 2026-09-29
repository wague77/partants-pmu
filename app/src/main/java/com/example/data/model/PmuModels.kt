package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- DTOs from PMU turfinfo API ---

@JsonClass(generateAdapter = true)
data class ProgrammeWrapper(
    @Json(name = "programme") val programme: ProgrammeDto? = null
)

@JsonClass(generateAdapter = true)
data class ProgrammeDto(
    @Json(name = "date") val date: Long? = null,
    @Json(name = "reunions") val reunions: List<ReunionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ReunionDto(
    @Json(name = "numOfficiel") val numOfficiel: Int = 0,
    @Json(name = "hippodrome") val hippodrome: HippodromeDto? = null,
    @Json(name = "courses") val courses: List<CourseDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HippodromeDto(
    @Json(name = "libelleCourt") val libelleCourt: String? = null,
    @Json(name = "libelleLong") val libelleLong: String? = null
)

@JsonClass(generateAdapter = true)
data class CourseDto(
    @Json(name = "numOrdre") val numOrdre: Int = 0,
    @Json(name = "libelle") val libelle: String? = null,
    @Json(name = "heureDepart") val heureDepart: Long? = null,
    @Json(name = "distance") val distance: Int? = null,
    @Json(name = "discipline") val discipline: String? = null,
    @Json(name = "nombreDeclaresPartants") val nombreDeclaresPartants: Int? = null,
    @Json(name = "paris") val paris: List<PariDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PariDto(
    @Json(name = "codePari") val codePari: String? = null
)

@JsonClass(generateAdapter = true)
data class ParticipantsWrapper(
    @Json(name = "participants") val participants: List<ParticipantDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ParticipantDto(
    @Json(name = "numPmu") val numPmu: Int = 0,
    @Json(name = "nom") val nom: String = "",
    @Json(name = "age") val age: Int? = null,
    @Json(name = "sexe") val sexe: String? = null,
    @Json(name = "driver") val driver: String? = null,
    @Json(name = "entraineur") val entraineur: String? = null,
    @Json(name = "statut") val statut: String? = null,
    @Json(name = "musique") val musique: String? = null,
    @Json(name = "dernierRapportDirect") val dernierRapportDirect: RapportDirectDto? = null
)

@JsonClass(generateAdapter = true)
data class RapportDirectDto(
    @Json(name = "rapport") val rapport: Double? = null,
    @Json(name = "typePari") val typePari: String? = null,
    @Json(name = "favoris") val favoris: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class PronosticWrapper(
    @Json(name = "selection") val selection: List<PronosticItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PronosticItemDto(
    @Json(name = "rang") val rang: Int = 0,
    @Json(name = "num_partant") val num_partant: Int = 0,
    @Json(name = "cote_prob") val cote_prob: Any? = null
) {
    fun getCoteFormatted(): String {
        return when (val cp = cote_prob) {
            is String -> cp
            is Number -> cp.toString()
            else -> "-"
        }
    }
}

@JsonClass(generateAdapter = true)
data class PronosticsDetaillesWrapper(
    @Json(name = "commentaire") val commentaire: CommentaireDto? = null
)

@JsonClass(generateAdapter = true)
data class CommentaireDto(
    @Json(name = "texte") val texte: String? = null
)

// --- Domain UI Models ---

data class UiReunion(
    val numOfficiel: Int,
    val hippodromeNom: String,
    val courses: List<UiCourse>
) {
    val badgeLabel: String get() = "R$numOfficiel · $hippodromeNom"
}

data class UiCourse(
    val reunionNum: Int,
    val numOrdre: Int,
    val libelle: String,
    val heureDepart: Long?,
    val heureDepartStr: String,
    val distance: Int?,
    val distanceStr: String,
    val discipline: String,
    val nombrePartants: Int,
    val codesParis: List<String>
) {
    val badgeLabel: String get() = "C$numOrdre · $heureDepartStr"
    val fullName: String get() = "C$numOrdre - $libelle"
}

data class UiParticipant(
    val numPmu: Int,
    val nom: String,
    val age: Int?,
    val sexe: String?,
    val ageSexeStr: String,
    val driver: String,
    val entraineur: String,
    val statut: String,
    val isPartant: Boolean,
    val musique: String,
    val cote: Double?,
    val isFavori: Boolean
)

data class UiPronosticItem(
    val rang: Int,
    val numPartant: Int,
    val nomCheval: String,
    val cote: String
)

data class UiBetCombination(
    val codePari: String,
    val nomJeu: String,
    val nbChevaux: Int,
    val chevaux: List<Int>
)
