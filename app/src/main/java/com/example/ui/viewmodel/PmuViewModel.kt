package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UiBetCombination
import com.example.data.model.UiCourse
import com.example.data.model.UiParticipant
import com.example.data.model.UiPronosticItem
import com.example.data.model.UiReunion
import com.example.data.repository.PmuRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class PmuViewModel(
    private val repository: PmuRepository = PmuRepository()
) : ViewModel() {

    private val apiDateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.FRANCE)

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _reunions = MutableStateFlow<List<UiReunion>>(emptyList())
    val reunions: StateFlow<List<UiReunion>> = _reunions.asStateFlow()

    private val _selectedReunion = MutableStateFlow<UiReunion?>(null)
    val selectedReunion: StateFlow<UiReunion?> = _selectedReunion.asStateFlow()

    private val _selectedCourse = MutableStateFlow<UiCourse?>(null)
    val selectedCourse: StateFlow<UiCourse?> = _selectedCourse.asStateFlow()

    private val _participants = MutableStateFlow<List<UiParticipant>>(emptyList())
    val participants: StateFlow<List<UiParticipant>> = _participants.asStateFlow()

    private val _pronosticItems = MutableStateFlow<List<UiPronosticItem>>(emptyList())
    val pronosticItems: StateFlow<List<UiPronosticItem>> = _pronosticItems.asStateFlow()

    private val _commentaire = MutableStateFlow<String?>(null)
    val commentaire: StateFlow<String?> = _commentaire.asStateFlow()

    private val _betCombinations = MutableStateFlow<List<UiBetCombination>>(emptyList())
    val betCombinations: StateFlow<List<UiBetCombination>> = _betCombinations.asStateFlow()

    private val _isLoadingProgramme = MutableStateFlow<Boolean>(false)
    val isLoadingProgramme: StateFlow<Boolean> = _isLoadingProgramme.asStateFlow()

    private val _isLoadingCourseData = MutableStateFlow<Boolean>(false)
    val isLoadingCourseData: StateFlow<Boolean> = _isLoadingCourseData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastUpdatedTime = MutableStateFlow<String?>(null)
    val lastUpdatedTime: StateFlow<String?> = _lastUpdatedTime.asStateFlow()

    private val _sortByCote = MutableStateFlow<Boolean>(false)
    val sortByCote: StateFlow<Boolean> = _sortByCote.asStateFlow()

    init {
        loadProgrammeForDate(_selectedDate.value)
    }

    fun getFormattedDateDisplay(): String {
        val date = _selectedDate.value
        val today = LocalDate.now()
        val formatted = date.format(displayDateFormatter)
            .replaceFirstChar { it.uppercase(Locale.FRANCE) }

        return when {
            date.isEqual(today) -> "Aujourd'hui · $formatted"
            date.isEqual(today.minusDays(1)) -> "Hier · $formatted"
            date.isEqual(today.plusDays(1)) -> "Demain · $formatted"
            else -> formatted
        }
    }

    fun isTodaySelected(): Boolean {
        return _selectedDate.value.isEqual(LocalDate.now())
    }

    fun toggleSortByCote() {
        _sortByCote.value = !_sortByCote.value
    }

    fun setDate(date: LocalDate) {
        if (_selectedDate.value != date) {
            _selectedDate.value = date
            loadProgrammeForDate(date)
        }
    }

    fun goToPreviousDay() {
        setDate(_selectedDate.value.minusDays(1))
    }

    fun goToNextDay() {
        setDate(_selectedDate.value.plusDays(1))
    }

    fun goToToday() {
        setDate(LocalDate.now())
    }

    fun refresh() {
        loadProgrammeForDate(_selectedDate.value, preserveSelection = true)
    }

    fun selectReunion(reunion: UiReunion) {
        if (_selectedReunion.value?.numOfficiel != reunion.numOfficiel) {
            _selectedReunion.value = reunion
            val firstCourse = reunion.courses.firstOrNull()
            _selectedCourse.value = firstCourse
            if (firstCourse != null) {
                loadCourseDetails(reunion.numOfficiel, firstCourse)
            } else {
                clearCourseData()
            }
        }
    }

    fun selectCourse(course: UiCourse) {
        if (_selectedCourse.value?.numOrdre != course.numOrdre ||
            _selectedCourse.value?.reunionNum != course.reunionNum
        ) {
            _selectedCourse.value = course
            _selectedReunion.value?.let { reunion ->
                loadCourseDetails(reunion.numOfficiel, course)
            }
        }
    }

    private fun loadProgrammeForDate(date: LocalDate, preserveSelection: Boolean = false) {
        viewModelScope.launch {
            _isLoadingProgramme.value = true
            _errorMessage.value = null

            val dateStr = date.format(apiDateFormatter)
            val result = repository.getProgramme(dateStr)

            result.onSuccess { reunionsList ->
                _reunions.value = reunionsList
                _lastUpdatedTime.value = timeFormat.format(Date())

                if (reunionsList.isEmpty()) {
                    _selectedReunion.value = null
                    _selectedCourse.value = null
                    clearCourseData()
                } else {
                    val prevReunionNum = _selectedReunion.value?.numOfficiel
                    val targetReunion = if (preserveSelection && prevReunionNum != null) {
                        reunionsList.find { it.numOfficiel == prevReunionNum } ?: reunionsList.first()
                    } else {
                        reunionsList.first()
                    }
                    _selectedReunion.value = targetReunion

                    val prevCourseNum = _selectedCourse.value?.numOrdre
                    val targetCourse = if (preserveSelection && prevCourseNum != null) {
                        targetReunion.courses.find { it.numOrdre == prevCourseNum } ?: targetReunion.courses.firstOrNull()
                    } else {
                        targetReunion.courses.firstOrNull()
                    }
                    _selectedCourse.value = targetCourse

                    if (targetCourse != null) {
                        loadCourseDetails(targetReunion.numOfficiel, targetCourse)
                    } else {
                        clearCourseData()
                    }
                }
            }.onFailure { error ->
                _errorMessage.value = error.localizedMessage ?: "Impossible de récupérer le programme PMU."
                _reunions.value = emptyList()
                _selectedReunion.value = null
                _selectedCourse.value = null
                clearCourseData()
            }

            _isLoadingProgramme.value = false
        }
    }

    private fun loadCourseDetails(reunionNum: Int, course: UiCourse) {
        viewModelScope.launch {
            _isLoadingCourseData.value = true
            val dateStr = _selectedDate.value.format(apiDateFormatter)

            val participantsDeferred = async { repository.getParticipants(dateStr, reunionNum, course.numOrdre) }
            val pronosticsDeferred = async { repository.getPronostics(dateStr, reunionNum, course.numOrdre) }
            val commentaireDeferred = async { repository.getCommentaire(dateStr, reunionNum, course.numOrdre) }

            val participantsResult = participantsDeferred.await()
            val pronosticsRaw = pronosticsDeferred.await()
            val commentaireText = commentaireDeferred.await()

            participantsResult.onSuccess { partantsList ->
                _participants.value = partantsList

                // Format pronostics items
                val pronosUi = repository.buildPronosticsUi(pronosticsRaw, partantsList)
                _pronosticItems.value = pronosUi

                // Format bet combinations
                val selectionNums = pronosticsRaw.map { it.first }
                val combinations = repository.buildBetCombinations(course.codesParis, selectionNums)
                _betCombinations.value = combinations
            }.onFailure {
                _participants.value = emptyList()
                _pronosticItems.value = emptyList()
                _betCombinations.value = emptyList()
            }

            _commentaire.value = commentaireText
            _isLoadingCourseData.value = false
        }
    }

    private fun clearCourseData() {
        _participants.value = emptyList()
        _pronosticItems.value = emptyList()
        _commentaire.value = null
        _betCombinations.value = emptyList()
    }
}
