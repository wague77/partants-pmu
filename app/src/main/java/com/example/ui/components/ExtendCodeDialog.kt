package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FirestoreLicence
import com.example.ui.theme.PmuGold
import com.example.ui.theme.PmuGreenPrimary

@Composable
fun ExtendCodeDialog(
    licence: FirestoreLicence,
    onDismiss: () -> Unit,
    onConfirmExtend: (additionalDays: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDays by remember { mutableIntStateOf(30) }
    var customDaysInput by remember { mutableStateOf("30") }
    var isCustom by remember { mutableStateOf(false) }

    // Preset options per prompt: +30j / +90j / +1 an, +7j
    val options = listOf(
        Pair("+7 Jours", 7),
        Pair("+30 Jours", 30),
        Pair("+90 Jours", 90),
        Pair("+1 An", 365)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Prolonger la Durée (Firestore)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PmuGreenPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Code concerné :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = licence.code,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PmuGreenPrimary
                )
                Text(
                    text = "Client : ${licence.client_name.ifBlank { "Client VIP" }} · Expire le ${licence.formattedExpirationDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "AJOUTER DES JOURS À EXPIRE_LE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEach { (label, days) ->
                        val isSelected = !isCustom && selectedDays == days
                        Surface(
                            onClick = {
                                selectedDays = days
                                isCustom = false
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

                    Surface(
                        onClick = { isCustom = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCustom) PmuGreenPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isCustom) PmuGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "Personnalisé",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isCustom) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                if (isCustom) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDaysInput,
                        onValueChange = {
                            customDaysInput = it.filter { char -> char.isDigit() }
                            selectedDays = customDaysInput.toIntOrNull() ?: 1
                        },
                        label = { Text("Jours additionnels") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysToAdd = if (isCustom) {
                        customDaysInput.toIntOrNull()?.coerceAtLeast(1) ?: 30
                    } else {
                        selectedDays
                    }
                    onConfirmExtend(daysToAdd)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PmuGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_extend_button")
            ) {
                Text("Prolonger de +$selectedDays jours")
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
