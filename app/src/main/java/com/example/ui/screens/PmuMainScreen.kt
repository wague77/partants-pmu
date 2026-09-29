package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.FirestoreLicence
import com.example.ui.components.ActivationLockDialog
import com.example.ui.components.AdminDashboardDialog
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.CourseHeaderCard
import com.example.ui.components.EmptyProgrammeView
import com.example.ui.components.ErrorMessageView
import com.example.ui.components.LoadingView
import com.example.ui.components.PartantsListSection
import com.example.ui.components.PmuHeader
import com.example.ui.components.PronosticCard
import com.example.ui.components.ResponsibleGamingBanner
import com.example.ui.components.ReunionCourseSelectors
import com.example.ui.theme.PmuGreenPrimary
import com.example.ui.theme.TurfBackground
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.PmuViewModel

@Composable
fun PmuMainScreen(
    pmuViewModel: PmuViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // PMU State
    val selectedDate by pmuViewModel.selectedDate.collectAsStateWithLifecycle()
    val reunions by pmuViewModel.reunions.collectAsStateWithLifecycle()
    val selectedReunion by pmuViewModel.selectedReunion.collectAsStateWithLifecycle()
    val selectedCourse by pmuViewModel.selectedCourse.collectAsStateWithLifecycle()
    val participants by pmuViewModel.participants.collectAsStateWithLifecycle()
    val pronosticItems by pmuViewModel.pronosticItems.collectAsStateWithLifecycle()
    val commentaire by pmuViewModel.commentaire.collectAsStateWithLifecycle()
    val betCombinations by pmuViewModel.betCombinations.collectAsStateWithLifecycle()
    val isLoadingProgramme by pmuViewModel.isLoadingProgramme.collectAsStateWithLifecycle()
    val isLoadingCourseData by pmuViewModel.isLoadingCourseData.collectAsStateWithLifecycle()
    val errorMessage by pmuViewModel.errorMessage.collectAsStateWithLifecycle()
    val lastUpdatedTime by pmuViewModel.lastUpdatedTime.collectAsStateWithLifecycle()
    val sortByCote by pmuViewModel.sortByCote.collectAsStateWithLifecycle()

    // Admin & License State
    val activeLicense by adminViewModel.activeLicense.collectAsStateWithLifecycle()
    val isActivating by adminViewModel.isActivating.collectAsStateWithLifecycle()
    val isAdminAuthenticated by adminViewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val allCodes by adminViewModel.allCodes.collectAsStateWithLifecycle()
    val filteredCodes by adminViewModel.filteredCodes.collectAsStateWithLifecycle()
    val searchQuery by adminViewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by adminViewModel.statusFilter.collectAsStateWithLifecycle()
    val generatedCodePreview by adminViewModel.generatedCodePreview.collectAsStateWithLifecycle()
    val actionFeedbackMessage by adminViewModel.actionFeedbackMessage.collectAsStateWithLifecycle()
    val activationError by adminViewModel.activationError.collectAsStateWithLifecycle()
    val isActivationSuccess by adminViewModel.isActivationSuccess.collectAsStateWithLifecycle()

    // Dialog visibility states
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val dateDisplayStr = pmuViewModel.getFormattedDateDisplay()
    val isToday = pmuViewModel.isTodaySelected()

    val isLicensed = activeLicense != null &&
        activeLicense?.effectiveStatus == FirestoreLicence.STATUT_ACTIF

    // Show feedback messages
    LaunchedEffect(actionFeedbackMessage) {
        actionFeedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearActivationFeedback()
        }
    }

    LaunchedEffect(isActivationSuccess) {
        if (isActivationSuccess) {
            showLicenseDialog = false
        }
    }

    if (!isLicensed) {
        AccessLockScreen(
            deviceId = adminViewModel.deviceId,
            isActivating = isActivating,
            activationError = activationError,
            onActivateCode = { code -> adminViewModel.activateCodeOnDevice(code) },
            onOpenAdmin = {
                if (isAdminAuthenticated) {
                    showAdminDashboard = true
                } else {
                    showAdminLoginDialog = true
                }
            },
            onClearError = { adminViewModel.clearActivationFeedback() },
            modifier = modifier
        )
    } else {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("pmu_main_screen"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                ) {
                    PmuHeader(
                        selectedDate = selectedDate,
                        dateDisplayStr = dateDisplayStr,
                        isToday = isToday,
                        isLoading = isLoadingProgramme || isLoadingCourseData,
                        lastUpdatedTime = lastUpdatedTime,
                        activeLicense = activeLicense,
                        onOpenLicenseDialog = { showLicenseDialog = true },
                        onOpenAdminDialog = {
                            if (isAdminAuthenticated) {
                                showAdminDashboard = true
                            } else {
                                showAdminLoginDialog = true
                            }
                        },
                        onPreviousDay = { pmuViewModel.goToPreviousDay() },
                        onNextDay = { pmuViewModel.goToNextDay() },
                        onToday = { pmuViewModel.goToToday() },
                        onSelectDate = { pmuViewModel.setDate(it) },
                        onRefresh = { pmuViewModel.refresh() }
                    )

                    if (reunions.isNotEmpty()) {
                        ReunionCourseSelectors(
                            reunions = reunions,
                            selectedReunion = selectedReunion,
                            selectedCourse = selectedCourse,
                            onSelectReunion = { pmuViewModel.selectReunion(it) },
                            onSelectCourse = { pmuViewModel.selectCourse(it) }
                        )
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    ResponsibleGamingBanner()
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TurfBackground)
                    .padding(innerPadding)
            ) {
                when {
                    isLoadingProgramme -> {
                        LoadingView(message = "Chargement des courses PMU...")
                    }

                    errorMessage != null -> {
                        ErrorMessageView(
                            errorMessage = errorMessage ?: "Erreur inconnue",
                            onRetry = { pmuViewModel.refresh() }
                        )
                    }

                    reunions.isEmpty() -> {
                        EmptyProgrammeView(
                            dateDisplay = dateDisplayStr,
                            onGoToToday = { pmuViewModel.goToToday() },
                            onRefresh = { pmuViewModel.refresh() }
                        )
                    }

                    selectedCourse != null -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Course Header
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                CourseHeaderCard(
                                    course = selectedCourse!!,
                                    hippodromeNom = selectedReunion?.hippodromeNom ?: "HIPPODROME"
                                )
                            }

                            if (isLoadingCourseData) {
                                item {
                                    LoadingView(message = "Chargement des partants & pronostics...")
                                }
                            } else {
                                // Pronostics PMU Card
                                item {
                                    PronosticCard(
                                        pronosticItems = pronosticItems,
                                        commentaire = commentaire,
                                        betCombinations = betCombinations
                                    )
                                }

                                // Partants List Section
                                if (participants.isNotEmpty()) {
                                    item {
                                        PartantsListSection(
                                            participants = participants,
                                            sortByCote = sortByCote,
                                            onToggleSort = { pmuViewModel.toggleSortByCote() }
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Admin Login Dialog
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            onDismiss = { showAdminLoginDialog = false },
            onLoginSuccess = {
                showAdminLoginDialog = false
                showAdminDashboard = true
            },
            onVerifyPin = { adminViewModel.verifyAdminPin(it) }
        )
    }

    // Admin Dashboard Dialog / Screen
    if (showAdminDashboard) {
        AdminDashboardDialog(
            allCodes = allCodes,
            filteredCodes = filteredCodes,
            searchQuery = searchQuery,
            statusFilter = statusFilter,
            generatedCodePreview = generatedCodePreview,
            onSearchQueryChange = { adminViewModel.setSearchQuery(it) },
            onStatusFilterChange = { adminViewModel.setStatusFilter(it) },
            onRegeneratePreview = { adminViewModel.generateNewCodePreview() },
            onCreateCode = { name, days, notes ->
                adminViewModel.createCode(name, days, null, notes)
            },
            onExtendCode = { code, days ->
                adminViewModel.extendCode(code, days)
            },
            onToggleBlock = { adminViewModel.toggleBlockCode(it) },
            onRevokeCode = { adminViewModel.revokeCode(it) },
            onDeleteCode = { adminViewModel.deleteCode(it) },
            onActivateOnDevice = { adminViewModel.activateCodeOnDevice(it) },
            onLogout = {
                adminViewModel.logoutAdmin()
                showAdminDashboard = false
            },
            onDismiss = { showAdminDashboard = false }
        )
    }

    // User License & Activation Dialog
    if (showLicenseDialog) {
        ActivationLockDialog(
            activeLicense = activeLicense,
            activationError = activationError,
            onDismiss = {
                adminViewModel.clearActivationFeedback()
                showLicenseDialog = false
            },
            onActivateCode = { adminViewModel.activateCodeOnDevice(it) },
            onOpenAdmin = {
                showLicenseDialog = false
                if (isAdminAuthenticated) {
                    showAdminDashboard = true
                } else {
                    showAdminLoginDialog = true
                }
            },
            onDeactivateCurrent = { adminViewModel.deactivateCurrentDevice() }
        )
    }
}
