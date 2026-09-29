package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PmuDarkGreen
import com.example.ui.theme.PmuGold
import com.example.ui.theme.PmuGoldDark
import com.example.ui.theme.PmuGoldLight
import com.example.ui.theme.PmuGreenPrimary

@Composable
fun GenerateCodeDialog(
    generatedCodePreview: String,
    onRegenerateCode: () -> Unit,
    onDismiss: () -> Unit,
    onCreateCode: (clientName: String, durationDays: Int, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var clientName by remember { mutableStateOf("") }
    var selectedDurationDays by remember { mutableIntStateOf(30) }
    var customDaysInput by remember { mutableStateOf("30") }
    var isCustomDuration by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var copiedToClipboard by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    val presetDurations = listOf(
        Pair("24h", 1),
        Pair("7 Jours", 7),
        Pair("30 Jours", 30),
        Pair("90 Jours", 90),
        Pair("1 An", 365)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PmuGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PmuDarkGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Générer un Code d'Accès",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PmuGreenPrimary
                    )
                    Text(
                        text = "Code cryptographique généré par ordinateur",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Code preview box with regenerate button
                Text(
                    text = "CODE UNIQUE GÉNÉRÉ PAR L'ORDINATEUR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = PmuGoldDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PmuDarkGreen,
                    border = BorderStroke(1.5.dp, PmuGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = generatedCodePreview,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = PmuGoldLight
                        )

                        Row {
                            IconButton(
                                onClick = onRegenerateCode,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Générer un autre code",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedCodePreview))
                                    copiedToClipboard = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copier",
                                    tint = if (copiedToClipboard) PmuGold else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (copiedToClipboard) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Code copié dans le presse-papier !",
                        style = MaterialTheme.typography.labelSmall,
                        color = PmuGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nom du client
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nom du client / Bénéficiaire") },
                    placeholder = { Text("Ex: Hubert Wague") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PmuGreenPrimary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_code_client_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Durée de validité
                Text(
                    text = "DURÉE DE VALIDITÉ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetDurations.forEach { (label, days) ->
                        val isSelected = !isCustomDuration && selectedDurationDays == days
                        Surface(
                            onClick = {
                                selectedDurationDays = days
                                isCustomDuration = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PmuGreenPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PmuGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Bouton Personnalisé
                    Surface(
                        onClick = { isCustomDuration = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCustomDuration) PmuGreenPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isCustomDuration) PmuGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "Personnalisé",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isCustomDuration) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isCustomDuration) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                if (isCustomDuration) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDaysInput,
                        onValueChange = {
                            customDaysInput = it.filter { char -> char.isDigit() }
                            selectedDurationDays = customDaysInput.toIntOrNull() ?: 1
                        },
                        label = { Text("Nombre de jours personnalisés") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes facultatives
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Téléphone / Réf (facultatif)") },
                    placeholder = { Text("Ex: Client VIP WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDays = if (isCustomDuration) {
                        customDaysInput.toIntOrNull()?.coerceAtLeast(1) ?: 30
                    } else {
                        selectedDurationDays
                    }
                    onCreateCode(clientName, finalDays, notes)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PmuGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_create_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Créer le code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color.Gray)
            }
        },
        modifier = modifier
    )
}
