package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UiParticipant
import com.example.ui.theme.FavoriBadge
import com.example.ui.theme.NonPartantBg
import com.example.ui.theme.NonPartantGray
import com.example.ui.theme.OddsBadgeBg
import com.example.ui.theme.OddsTextColor
import com.example.ui.theme.PmuDarkGreen
import com.example.ui.theme.PmuGold
import com.example.ui.theme.PmuGoldDark
import com.example.ui.theme.PmuGreenPrimary

@Composable
fun PartantsListSection(
    participants: List<UiParticipant>,
    sortByCote: Boolean,
    onToggleSort: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedList = if (sortByCote) {
        participants.sortedWith(
            compareBy<UiParticipant> { !it.isPartant }
                .thenBy { it.cote ?: Double.MAX_VALUE }
                .thenBy { it.numPmu }
        )
    } else {
        participants.sortedWith(
            compareBy<UiParticipant> { !it.isPartant }
                .thenBy { it.numPmu }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PARTANTS & COTES",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = PmuGreenPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PmuGreenPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${participants.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PmuGreenPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Sort Toggle FilterChip
            FilterChip(
                selected = sortByCote,
                onClick = onToggleSort,
                label = {
                    Text(
                        text = if (sortByCote) "Tri : Cotes" else "Tri : N° PMU",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Trier les partants",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PmuGold,
                    selectedLabelColor = PmuDarkGreen,
                    selectedLeadingIconColor = PmuDarkGreen
                ),
                modifier = Modifier.testTag("sort_filter_chip")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // List of runners
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            displayedList.forEach { participant ->
                PartantItemCard(participant = participant)
            }
        }
    }
}

@Composable
fun PartantItemCard(
    participant: UiParticipant,
    modifier: Modifier = Modifier
) {
    val isPartant = participant.isPartant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("partant_card_${participant.numPmu}")
            .then(if (!isPartant) Modifier.alpha(0.6f) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPartant) MaterialTheme.colorScheme.surface else NonPartantBg
        ),
        border = BorderStroke(
            1.dp,
            if (isPartant) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            else NonPartantGray.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPartant) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pastille dorée avec numéro du cheval (ou grisée si non-partant)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPartant) {
                            Brush.linearGradient(listOf(PmuGold, PmuGoldDark))
                        } else {
                            Brush.linearGradient(listOf(NonPartantGray, Color.Gray))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${participant.numPmu}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name and Non-partant / Favori indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = participant.nom,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (!isPartant) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isPartant) MaterialTheme.colorScheme.onSurface else NonPartantGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!isPartant) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Red.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, Color.Red)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "NON-PARTANT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = Color.Red
                                )
                            }
                        }
                    } else if (participant.isFavori) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FavoriBadge.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, FavoriBadge)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = FavoriBadge,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "FAVORI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = FavoriBadge
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Age & Sexe
                Text(
                    text = participant.ageSexeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Driver & Entraîneur
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PmuGreenPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = participant.driver,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (participant.entraineur.isNotBlank() && participant.entraineur != "-") {
                        Text(
                            text = " • E: ${participant.entraineur}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Musique (forme récente)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Musique : ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = participant.musique,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = PmuDarkGreen,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cote (Direct Odds)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isPartant && participant.cote != null) {
                    Text(
                        text = "COTE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OddsBadgeBg,
                        border = BorderStroke(1.dp, OddsTextColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "%.1f".format(participant.cote),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            color = OddsTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (isPartant) {
                    Text(
                        text = "Cote : --",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
