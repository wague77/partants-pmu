package com.example.data.repository

import com.example.data.api.PmuApiService
import com.example.data.model.CourseDto
import com.example.data.model.ParticipantDto
import com.example.data.model.ReunionDto
import com.example.data.model.UiBetCombination
import com.example.data.model.UiCourse
import com.example.data.model.UiParticipant
import com.example.data.model.UiPronosticItem
import com.example.data.model.UiReunion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PmuRepository(
    private val apiService: PmuApiService = PmuApiService.create()
) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

    suspend fun getProgramme(dateStr: String): Result<List<UiReunion>> {
        return try {
            val response = apiService.getProgramme(dateStr)
            if (response.isSuccessful) {
                val reunionsDto = response.body()?.programme?.reunions.orEmpty()
                val uiReunions = reunionsDto.map { it.toUiReunion() }
                Result.success(uiReunions)
            } else {
                Result.failure(Exception("Erreur serveur PMU (${response.code()}) : ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParticipants(dateStr: String, reunion: Int, course: Int): Result<List<UiParticipant>> {
        return try {
            val response = apiService.getParticipants(dateStr, reunion, course)
            if (response.isSuccessful) {
                val participantsDto = response.body()?.participants.orEmpty()
                val list = participantsDto.map { it.toUiParticipant() }
                Result.success(list)
            } else {
                Result.failure(Exception("Erreur lors du chargement des partants (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPronostics(dateStr: String, reunion: Int, course: Int): List<Pair<Int, String>> {
        return try {
            val response = apiService.getPronostics(dateStr, reunion, course)
            if (response.isSuccessful) {
                response.body()?.selection.orEmpty().map { item ->
                    Pair(item.num_partant, item.getCoteFormatted())
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCommentaire(dateStr: String, reunion: Int, course: Int): String? {
        return try {
            val response = apiService.getPronosticsDetailles(dateStr, reunion, course)
            if (response.isSuccessful) {
                response.body()?.commentaire?.texte
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun buildPronosticsUi(
        selection: List<Pair<Int, String>>,
        participants: List<UiParticipant>
    ): List<UiPronosticItem> {
        val partantsMap = participants.associateBy { it.numPmu }
        return selection.mapIndexed { index, (numPartant, cote) ->
            val cheval = partantsMap[numPartant]
            UiPronosticItem(
                rang = index + 1,
                numPartant = numPartant,
                nomCheval = cheval?.nom ?: "Cheval #$numPartant",
                cote = if (cote != "-") cote else cheval?.cote?.let { "%.1f".format(it) } ?: "-"
            )
        }
    }

    fun buildBetCombinations(
        codesParis: List<String>,
        selectionNumeros: List<Int>
    ): List<UiBetCombination> {
        if (selectionNumeros.isEmpty()) return emptyList()

        val combinations = mutableListOf<UiBetCombination>()
        val seenGames = mutableSetOf<String>()

        for (rawCode in codesParis) {
            val cleanCode = rawCode.removePrefix("E_")
            val (nomJeu, count) = getGameInfo(cleanCode)

            // Avoid repeating identical names if both E_ and non-E_ or variants exist
            if (seenGames.add(nomJeu)) {
                val selectedChevaux = selectionNumeros.take(count)
                combinations.add(
                    UiBetCombination(
                        codePari = cleanCode,
                        nomJeu = nomJeu,
                        nbChevaux = count,
                        chevaux = selectedChevaux
                    )
                )
            }
        }

        // Sort combinations logically: Simple, Couplé, 2 sur 4, Trio, Tiercé, Quarté+, Multi, Quinté+, Pick 5
        return combinations.sortedBy { it.nbChevaux }
    }

    private fun getGameInfo(code: String): Pair<String, Int> {
        return when (code.uppercase(Locale.FRANCE)) {
            "SIMPLE_GAGNANT" -> Pair("Simple Gagnant", 1)
            "SIMPLE_PLACE" -> Pair("Simple Placé", 1)
            "COUPLE_GAGNANT" -> Pair("Couplé Gagnant", 2)
            "COUPLE_PLACE" -> Pair("Couplé Placé", 2)
            "COUPLE_ORDRE" -> Pair("Couplé Ordre", 2)
            "2_SUR_4", "DEUX_SUR_QUATRE" -> Pair("2 sur 4", 2)
            "TRIO" -> Pair("Trio", 3)
            "TRIO_ORDRE" -> Pair("Trio Ordre", 3)
            "TIERCE" -> Pair("Tiercé", 3)
            "SUPER_4" -> Pair("Super 4", 4)
            "QUARTE_PLUS", "QUARTE" -> Pair("Quarté+", 4)
            "MULTI" -> Pair("Multi", 4)
            "QUINTE_PLUS", "QUINTE" -> Pair("Quinté+", 5)
            "PICK5", "PICK_5" -> Pair("Pick 5", 5)
            else -> {
                val formatted = code.replace("_", " ").lowercase(Locale.FRANCE)
                    .replaceFirstChar { it.uppercase(Locale.FRANCE) }
                val defaultCount = when {
                    code.contains("SIMPLE") -> 1
                    code.contains("COUPLE") || code.contains("2") -> 2
                    code.contains("TRIO") || code.contains("TIERCE") -> 3
                    code.contains("QUARTE") || code.contains("MULTI") -> 4
                    code.contains("QUINTE") || code.contains("PICK") -> 5
                    else -> 2
                }
                Pair(formatted, defaultCount)
            }
        }
    }

    private fun ReunionDto.toUiReunion(): UiReunion {
        val hippodromeNom = hippodrome?.libelleCourt ?: hippodrome?.libelleLong ?: "RÉUNION $numOfficiel"
        val uiCourses = courses.map { it.toUiCourse(numOfficiel) }
        return UiReunion(
            numOfficiel = numOfficiel,
            hippodromeNom = hippodromeNom.uppercase(Locale.FRANCE),
            courses = uiCourses
        )
    }

    private fun CourseDto.toUiCourse(reunionNum: Int): UiCourse {
        val heureStr = heureDepart?.let {
            timeFormat.format(Date(it))
        } ?: "--:--"

        val distanceFormatted = distance?.let { "$it m" } ?: "-- m"
        val disciplineFormatted = formatDiscipline(discipline)

        return UiCourse(
            reunionNum = reunionNum,
            numOrdre = numOrdre,
            libelle = libelle ?: "Course $numOrdre",
            heureDepart = heureDepart,
            heureDepartStr = heureStr,
            distance = distance,
            distanceStr = distanceFormatted,
            discipline = disciplineFormatted,
            nombrePartants = nombreDeclaresPartants ?: 0,
            codesParis = paris.mapNotNull { it.codePari }
        )
    }

    private fun formatDiscipline(discipline: String?): String {
        return when (discipline?.uppercase(Locale.FRANCE)) {
            "ATTELE" -> "Attelé"
            "MONTE" -> "Monté"
            "PLAT" -> "Plat"
            "HAIES" -> "Haies"
            "STEEPLE_CHASE" -> "Steeple-Chase"
            "CROSS_COUNTRY" -> "Cross-Country"
            "OBSTACLE" -> "Obstacle"
            null -> "Course"
            else -> discipline.replace("_", " ").lowercase(Locale.FRANCE)
                .replaceFirstChar { it.uppercase(Locale.FRANCE) }
        }
    }

    private fun ParticipantDto.toUiParticipant(): UiParticipant {
        val isNonPartant = statut?.uppercase(Locale.FRANCE) == "NON_PARTANT"
        val isPartant = !isNonPartant

        val sexeClean = when (sexe?.uppercase(Locale.FRANCE)) {
            "HONGRES" -> "Hongre"
            "FEMELLES" -> "Femelle"
            "MALES" -> "Mâle"
            null -> ""
            else -> sexe.lowercase(Locale.FRANCE).replaceFirstChar { it.uppercase(Locale.FRANCE) }
        }

        val ageStr = age?.let { "$it ans" } ?: ""
        val ageSexe = listOf(ageStr, sexeClean).filter { it.isNotBlank() }.joinToString(" • ")

        return UiParticipant(
            numPmu = numPmu,
            nom = nom.ifBlank { "Cheval #$numPmu" },
            age = age,
            sexe = sexe,
            ageSexeStr = ageSexe.ifBlank { "-" },
            driver = driver?.ifBlank { "-" } ?: "-",
            entraineur = entraineur?.ifBlank { "-" } ?: "-",
            statut = statut ?: "PARTANT",
            isPartant = isPartant,
            musique = musique?.ifBlank { "Inédit" } ?: "Inédit",
            cote = dernierRapportDirect?.rapport,
            isFavori = dernierRapportDirect?.favoris == true
        )
    }
}
