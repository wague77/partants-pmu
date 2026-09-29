package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UiCourse
import com.example.data.model.UiReunion
import com.example.ui.theme.PmuDarkGreen
import com.example.ui.theme.PmuGold
import com.example.ui.theme.PmuGoldContainer
import com.example.ui.theme.PmuGoldDark
import com.example.ui.theme.PmuGreenPrimary
import com.example.ui.theme.PmuOnGoldContainer

@Composable
fun ReunionCourseSelectors(
    reunions: List<UiReunion>,
    selectedReunion: UiReunion?,
    selectedCourse: UiCourse?,
    onSelectReunion: (UiReunion) -> Unit,
    onSelectCourse: (UiCourse) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp)
    ) {
        // Label Réunions
        Text(
            text = "RÉUNIONS DU JOUR",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = PmuGreenPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        // Réunions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reunions.forEach { reunion ->
                val isSelected = selectedReunion?.numOfficiel == reunion.numOfficiel
                Surface(
                    onClick = { onSelectReunion(reunion) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PmuGreenPrimary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PmuGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                    modifier = Modifier.testTag("reunion_chip_${reunion.numOfficiel}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge R1, R2...
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (isSelected) PmuGold else PmuGreenPrimary.copy(alpha = 0.12f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R${reunion.numOfficiel}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) PmuDarkGreen else PmuGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Nom hippodrome
                        Text(
                            text = reunion.hippodromeNom,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Nombre de courses
                        Text(
                            text = "(${reunion.courses.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) PmuGold else Color.Gray
                        )
                    }
                }
            }
        }

        // Courses Row (if a reunion is selected and has courses)
        if (selectedReunion != null && selectedReunion.courses.isNotEmpty()) {
            Spacer(modifier = Modifier.size(6.dp))

            Text(
                text = "COURSES · ${selectedReunion.hippodromeNom}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = PmuGoldDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedReunion.courses.forEach { course ->
                    val isSelected = selectedCourse?.numOrdre == course.numOrdre
                    Surface(
                        onClick = { onSelectCourse(course) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PmuGold else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) PmuGoldDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier.testTag("course_chip_${course.numOrdre}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "C${course.numOrdre}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = if (isSelected) PmuDarkGreen else PmuGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = course.heureDepartStr,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) PmuDarkGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
