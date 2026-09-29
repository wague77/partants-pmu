package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FirestoreLicence
import com.example.ui.theme.PmuDarkGreen
import com.example.ui.theme.PmuGold
import com.example.ui.theme.PmuGoldDark
import com.example.ui.theme.PmuGoldLight
import com.example.ui.theme.PmuGreenPrimary
import com.example.ui.theme.PmuGreenSecondary

@Composable
fun AdminDashboardDialog(
    allCodes: List<FirestoreLicence>,
    filteredCodes: List<FirestoreLicence>,
    searchQuery: String,
    statusFilter: String,
    generatedCodePreview: String,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onRegeneratePreview: () -> Unit,
    onCreateCode: (clientName: String, durationDays: Int, notes: String) -> Unit,
    onExtendCode: (code: String, additionalDays: Int) -> Unit,
    onToggleBlock: (FirestoreLicence) -> Unit,
    onRevokeCode: (code: String) -> Unit,
    onDeleteCode: (code: String) -> Unit,
    onActivateOnDevice: (code: String) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGenerateDialog by remember { mutableStateOf(false) }
    var codeToExtend by remember { mutableStateOf<FirestoreLicence?>(null) }
    var codeToRevoke by remember { mutableStateOf<FirestoreLicence?>(null) }
    var codeToDelete by remember { mutableStateOf<FirestoreLicence?>(null) }

    val clipboardManager = LocalClipboardManager.current

    // Metrics
    val totalCount = allCodes.size
    val activeCount = allCodes.count { it.effectiveStatus == FirestoreLicence.STATUT_ACTIF }
    val blockedCount = allCodes.count { it.effectiveStatus == FirestoreLicence.STATUT_BLOQUE || it.effectiveStatus == FirestoreLicence.STATUT_REVOQUE }
    val expiredCount = allCodes.count { it.effectiveStatus == FirestoreLicence.STATUT_EXPIRE }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("admin_dashboard_screen"),
            topBar = {
                Surface(
                    color = PmuDarkGreen,
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PmuDarkGreen, PmuGreenPrimary, PmuGreenSecondary)
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(PmuGold, PmuGoldLight))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = PmuDarkGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "ADMIN",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FIRESTORE",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            ),
                                            color = PmuGold
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Cloud,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Collection : licences",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = onLogout,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Déconnexion admin",
                                        tint = PmuGoldLight
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .testTag("admin_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer le tableau de bord",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        onRegeneratePreview()
                        showGenerateDialog = true
                    },
                    containerColor = PmuGold,
                    contentColor = PmuDarkGreen,
                    modifier = Modifier.testTag("admin_fab_generate_code")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nouveau Code", fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // KPI Metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "Total",
                        count = totalCount,
                        color = PmuGreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Actifs",
                        count = activeCount,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Bloqués",
                        count = blockedCount,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Expirés",
                        count = expiredCount,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Rechercher par code, client, note...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = PmuGreenPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        Pair("Tous ($totalCount)", "ALL"),
                        Pair("Actifs ($activeCount)", "ACTIVE"),
                        Pair("Bloqués/Rév. ($blockedCount)", "BLOCKED"),
                        Pair("Expirés ($expiredCount)", "EXPIRED")
                    )

                    filters.forEach { (label, key) ->
                        val isSelected = statusFilter == key
                        Surface(
                            onClick = { onStatusFilterChange(key) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PmuGreenPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PmuGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Code List
                if (filteredCodes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = PmuGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Aucune licence trouvée",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Créez un nouveau code d'accès Firestore avec le bouton ci-dessous.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCodes, key = { it.code }) { licence ->
                            AdminCodeCard(
                                licence = licence,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(licence.code))
                                },
                                onExtend = { codeToExtend = licence },
                                onToggleBlock = { onToggleBlock(licence) },
                                onRevoke = { codeToRevoke = licence },
                                onDelete = { codeToDelete = licence },
                                onActivateHere = { onActivateOnDevice(licence.code) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(70.dp))
                        }
                    }
                }
            }
        }
    }

    // Generator Dialog
    if (showGenerateDialog) {
        GenerateCodeDialog(
            generatedCodePreview = generatedCodePreview,
            onRegenerateCode = onRegeneratePreview,
            onDismiss = { showGenerateDialog = false },
            onCreateCode = { name, days, notes ->
                onCreateCode(name, days, notes)
            }
        )
    }

    // Extend Duration Dialog
    codeToExtend?.let { entity ->
        ExtendCodeDialog(
            licence = entity,
            onDismiss = { codeToExtend = null },
            onConfirmExtend = { days ->
                onExtendCode(entity.code, days)
                codeToExtend = null
            }
        )
    }

    // Confirm Revoke Dialog
    codeToRevoke?.let { entity ->
        AlertDialog(
            onDismissRequest = { codeToRevoke = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
            },
            title = { Text("Révoquer cette licence dans Firestore ?") },
            text = {
                Text(
                    "Le code ${entity.code} pour ${entity.client_name.ifBlank { "le client" }} passera au statut 'revoque' dans Firestore et ne pourra plus débloquer l'application."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRevokeCode(entity.code)
                        codeToRevoke = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Oui, Révoquer")
                }
            },
            dismissButton = {
                TextButton(onClick = { codeToRevoke = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Confirm Delete Dialog
    codeToDelete?.let { entity ->
        AlertDialog(
            onDismissRequest = { codeToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
            },
            title = { Text("Supprimer de Firestore ?") },
            text = {
                Text("Supprimer définitivement le document ${entity.code} de la collection 'licences' ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCode(entity.code)
                        codeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { codeToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                ),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdminCodeCard(
    licence: FirestoreLicence,
    onCopy: () -> Unit,
    onExtend: () -> Unit,
    onToggleBlock: () -> Unit,
    onRevoke: () -> Unit,
    onDelete: () -> Unit,
    onActivateHere: () -> Unit,
    modifier: Modifier = Modifier
) {
    var justCopied by remember { mutableStateOf(false) }
    val effectiveStatus = licence.effectiveStatus

    val statusBadgeColor = when (effectiveStatus) {
        FirestoreLicence.STATUT_ACTIF -> Color(0xFF16A34A)
        FirestoreLicence.STATUT_BLOQUE -> Color(0xFFDC2626)
        FirestoreLicence.STATUT_REVOQUE -> Color(0xFF7C3AED)
        FirestoreLicence.STATUT_EXPIRE -> Color(0xFFD97706)
        else -> Color.Gray
    }

    val statusBadgeText = when (effectiveStatus) {
        FirestoreLicence.STATUT_ACTIF -> "ACTIF"
        FirestoreLicence.STATUT_BLOQUE -> "BLOQUÉ"
        FirestoreLicence.STATUT_REVOQUE -> "RÉVOQUÉ"
        FirestoreLicence.STATUT_EXPIRE -> "EXPIRÉ"
        else -> effectiveStatus.uppercase()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Code and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = licence.code,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = PmuGreenPrimary
                    )

                    IconButton(
                        onClick = {
                            onCopy()
                            justCopied = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copier",
                            tint = if (justCopied) PmuGoldDark else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBadgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusBadgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = statusBadgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusBadgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Client and Notes
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PmuGoldDark,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = licence.client_name.ifBlank { "Client VIP" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (licence.notes.isNotBlank()) {
                    Text(
                        text = " • ${licence.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dates and Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Créé : ${licence.formattedCreatedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (effectiveStatus == FirestoreLicence.STATUT_ACTIF) {
                        "Expire dans ${licence.remainingDays}j (${licence.formattedExpirationDate})"
                    } else {
                        "Expiré le ${licence.formattedExpirationDate}"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (effectiveStatus == FirestoreLicence.STATUT_ACTIF) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (effectiveStatus == FirestoreLicence.STATUT_ACTIF) PmuGreenPrimary else Color.Gray
                )
            }

            if (!licence.device_id.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Appareil associé : ${licence.device_id.take(16)}...",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Strip (Prolonger, Bloquer, Révoquer, Supprimer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Prolonger
                OutlinedButton(
                    onClick = onExtend,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prolonger", style = MaterialTheme.typography.labelSmall)
                }

                // Bloquer / Débloquer
                OutlinedButton(
                    onClick = onToggleBlock,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = if (licence.statut == FirestoreLicence.STATUT_BLOQUE) Icons.Default.LockOpen else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (licence.statut == FirestoreLicence.STATUT_BLOQUE) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (licence.statut == FirestoreLicence.STATUT_BLOQUE) "Débloquer" else "Bloquer",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (licence.statut == FirestoreLicence.STATUT_BLOQUE) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }

                // Révoquer
                if (licence.statut != FirestoreLicence.STATUT_REVOQUE) {
                    OutlinedButton(
                        onClick = onRevoke,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Révoquer",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }

                // Supprimer
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Supprimer", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                // Activer sur cet appareil (test direct)
                if (effectiveStatus == FirestoreLicence.STATUT_ACTIF) {
                    Button(
                        onClick = onActivateHere,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PmuGold,
                            contentColor = PmuDarkGreen
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activer ici", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
