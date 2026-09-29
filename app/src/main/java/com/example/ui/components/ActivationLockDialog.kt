package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FirestoreLicence
import com.example.ui.theme.PmuDarkGreen
import com.example.ui.theme.PmuGold
import com.example.ui.theme.PmuGoldDark
import com.example.ui.theme.PmuGoldLight
import com.example.ui.theme.PmuGreenPrimary

@Composable
fun ActivationLockDialog(
    activeLicense: FirestoreLicence?,
    activationError: String?,
    onDismiss: () -> Unit,
    onActivateCode: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onDeactivateCurrent: () -> Unit,
    modifier: Modifier = Modifier
) {
    var codeInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val isLicensed = activeLicense != null &&
        activeLicense.effectiveStatus == FirestoreLicence.STATUT_ACTIF

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isLicensed) listOf(PmuGold, PmuGoldLight) else listOf(PmuGreenPrimary, PmuDarkGreen)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLicensed) Icons.Default.Star else Icons.Default.Key,
                        contentDescription = null,
                        tint = if (isLicensed) PmuDarkGreen else PmuGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isLicensed) "Licence Active" else "Code d'Accès Requis",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = PmuGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "En ligne Firestore",
                            tint = PmuGoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (isLicensed) "Synchronisé en ligne avec Firestore" else "Validation en ligne requise",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isLicensed && activeLicense != null) {
                    // Active license status card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PmuGreenPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.5.dp, PmuGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Votre abonnement est actif sur Firestore",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF16A34A)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Code : ${activeLicense.code}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PmuDarkGreen
                            )
                            Text(
                                text = "Bénéficiaire : ${activeLicense.client_name.ifBlank { "Client VIP" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Expire dans : ${activeLicense.remainingDays} jours (${activeLicense.formattedExpirationDate})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = PmuGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Vous pouvez entrer un nouveau code pour prolonger ou changer de licence :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Cette application est protégée. Veuillez entrer votre code d'accès généré par l'ordinateur. Le code sera validé en direct sur Firebase Firestore.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input code field
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase() },
                    label = { Text("Code d'accès généré") },
                    placeholder = { Text("Ex: PMU-XXXX-XXXX-XXXX") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = PmuGoldDark)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                clipboardManager.getText()?.text?.let {
                                    codeInput = it.trim().uppercase()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Coller")
                        }
                    },
                    singleLine = true,
                    isError = activationError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activation_code_input")
                )

                if (activationError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activationError,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Admin Access button
                OutlinedButton(
                    onClick = onOpenAdmin,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PmuGoldDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = PmuDarkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tableau de Bord Admin (Générateur)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PmuDarkGreen
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (codeInput.isNotBlank()) {
                        onActivateCode(codeInput)
                    }
                },
                enabled = codeInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PmuGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("activate_code_submit_button")
            ) {
                Text("Valider en ligne")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isLicensed) "Fermer" else "Continuer", color = Color.Gray)
            }
        },
        modifier = modifier
    )
}
