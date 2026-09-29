package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FirestoreLicence
import com.example.data.repository.FirestoreLicenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = FirestoreLicenceRepository(application)

    companion object {
        const val MASTER_ADMIN_PIN = "2026"
        const val MASTER_ADMIN_CODE = "PMU-ADMIN-2026"
    }

    val activeLicense: StateFlow<FirestoreLicence?> = repository.observeActiveDeviceLicence()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL") // "ALL", "ACTIVE", "BLOCKED", "EXPIRED"
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _generatedCodePreview = MutableStateFlow("")
    val generatedCodePreview: StateFlow<String> = _generatedCodePreview.asStateFlow()

    private val _actionFeedbackMessage = MutableStateFlow<String?>(null)
    val actionFeedbackMessage: StateFlow<String?> = _actionFeedbackMessage.asStateFlow()

    val deviceId: String get() = repository.deviceId

    private val _isActivating = MutableStateFlow(false)
    val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

    private val _activationError = MutableStateFlow<String?>(null)
    val activationError: StateFlow<String?> = _activationError.asStateFlow()

    private val _isActivationSuccess = MutableStateFlow(false)
    val isActivationSuccess: StateFlow<Boolean> = _isActivationSuccess.asStateFlow()

    val allCodes: StateFlow<List<FirestoreLicence>> = repository.observeAllLicences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredCodes: StateFlow<List<FirestoreLicence>> = combine(
        allCodes,
        _searchQuery,
        _statusFilter
    ) { codes, query, filter ->
        codes.filter { entity ->
            val matchesQuery = query.isBlank() ||
                entity.code.contains(query, ignoreCase = true) ||
                entity.client_name.contains(query, ignoreCase = true) ||
                entity.notes.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "ACTIVE" -> entity.effectiveStatus == FirestoreLicence.STATUT_ACTIF
                "BLOCKED" -> entity.effectiveStatus == FirestoreLicence.STATUT_BLOQUE || entity.effectiveStatus == FirestoreLicence.STATUT_REVOQUE
                "EXPIRED" -> entity.effectiveStatus == FirestoreLicence.STATUT_EXPIRE
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initDefaultCodesIfEmpty()
            generateNewCodePreview()
        }
    }

    fun verifyAdminPin(input: String): Boolean {
        val clean = input.trim()
        val success = clean == MASTER_ADMIN_PIN || clean.equals(MASTER_ADMIN_CODE, ignoreCase = true)
        if (success) {
            _isAdminAuthenticated.value = true
        }
        return success
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun generateNewCodePreview() {
        _generatedCodePreview.value = repository.generateComputerCode()
    }

    fun createCode(
        clientName: String,
        durationDays: Int,
        customCode: String? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val codeToUse = customCode?.ifBlank { null } ?: _generatedCodePreview.value
            repository.createLicence(
                clientName = clientName,
                durationDays = durationDays,
                customCode = codeToUse,
                notes = notes
            ).onSuccess { entity ->
                _actionFeedbackMessage.value = "Code créé sur Firestore : ${entity.code}"
                generateNewCodePreview()
            }.onFailure {
                _actionFeedbackMessage.value = "Erreur création Firestore: ${it.localizedMessage}"
            }
        }
    }

    fun extendCode(code: String, additionalDays: Int) {
        viewModelScope.launch {
            repository.extendLicence(code, additionalDays)
                .onSuccess {
                    _actionFeedbackMessage.value = "Durée prolongée de +$additionalDays jours sur Firestore pour $code"
                }
                .onFailure {
                    _actionFeedbackMessage.value = "Erreur: ${it.localizedMessage}"
                }
        }
    }

    fun toggleBlockCode(entity: FirestoreLicence) {
        viewModelScope.launch {
            if (entity.statut == FirestoreLicence.STATUT_BLOQUE) {
                repository.unblockLicence(entity.code)
                _actionFeedbackMessage.value = "Code débloqué : ${entity.code}"
            } else {
                repository.blockLicence(entity.code)
                _actionFeedbackMessage.value = "Code bloqué : ${entity.code}"
            }
        }
    }

    fun revokeCode(code: String) {
        viewModelScope.launch {
            repository.revokeLicence(code)
            _actionFeedbackMessage.value = "Code révoqué sur Firestore : $code"
        }
    }

    fun deleteCode(code: String) {
        viewModelScope.launch {
            repository.deleteLicence(code)
            _actionFeedbackMessage.value = "Code supprimé de Firestore."
        }
    }

    fun activateCodeOnDevice(code: String) {
        viewModelScope.launch {
            _activationError.value = null
            _isActivating.value = true
            try {
                repository.activateOnDevice(code)
                    .onSuccess {
                        _isActivationSuccess.value = true
                        _actionFeedbackMessage.value = "Licence validée et activée en ligne !"
                    }
                    .onFailure {
                        _activationError.value = it.localizedMessage ?: "Code invalide"
                        _isActivationSuccess.value = false
                    }
            } finally {
                _isActivating.value = false
            }
        }
    }

    fun clearActivationFeedback() {
        _activationError.value = null
        _isActivationSuccess.value = false
        _actionFeedbackMessage.value = null
    }

    fun deactivateCurrentDevice() {
        repository.deactivateCurrentDevice()
        _actionFeedbackMessage.value = "Appareil déconnecté."
    }
}
