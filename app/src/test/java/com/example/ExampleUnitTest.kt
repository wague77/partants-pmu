package com.example

import com.example.data.model.FirestoreLicence
import com.example.data.repository.PmuRepository
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class ExampleUnitTest {

    @Test
    fun `test bet combinations generation according to PMU rules`() {
        val repo = PmuRepository()

        // Selection of ranked horses: 2, 9, 12, 1, 7, 10
        val selection = listOf(2, 9, 12, 1, 7, 10)

        val parisCodes = listOf(
            "E_SIMPLE_GAGNANT",
            "E_SIMPLE_PLACE",
            "E_COUPLE_GAGNANT",
            "E_2_SUR_4",
            "E_TRIO",
            "E_TIERCE",
            "E_SUPER_4",
            "E_QUARTE_PLUS",
            "E_MULTI",
            "E_QUINTE_PLUS",
            "E_PICK5"
        )

        val combos = repo.buildBetCombinations(parisCodes, selection)

        val simpleGagnant = combos.find { it.nomJeu == "Simple Gagnant" }
        assertEquals(1, simpleGagnant?.nbChevaux)
        assertEquals(listOf(2), simpleGagnant?.chevaux)

        val coupleGagnant = combos.find { it.nomJeu == "Couplé Gagnant" }
        assertEquals(2, coupleGagnant?.nbChevaux)
        assertEquals(listOf(2, 9), coupleGagnant?.chevaux)

        val deuxSurQuatre = combos.find { it.nomJeu == "2 sur 4" }
        assertEquals(2, deuxSurQuatre?.nbChevaux)
        assertEquals(listOf(2, 9), deuxSurQuatre?.chevaux)

        val trio = combos.find { it.nomJeu == "Trio" }
        assertEquals(3, trio?.nbChevaux)
        assertEquals(listOf(2, 9, 12), trio?.chevaux)

        val tierce = combos.find { it.nomJeu == "Tiercé" }
        assertEquals(3, tierce?.nbChevaux)
        assertEquals(listOf(2, 9, 12), tierce?.chevaux)

        val super4 = combos.find { it.nomJeu == "Super 4" }
        assertEquals(4, super4?.nbChevaux)
        assertEquals(listOf(2, 9, 12, 1), super4?.chevaux)

        val quarte = combos.find { it.nomJeu == "Quarté+" }
        assertEquals(4, quarte?.nbChevaux)
        assertEquals(listOf(2, 9, 12, 1), quarte?.chevaux)

        val multi = combos.find { it.nomJeu == "Multi" }
        assertEquals(4, multi?.nbChevaux)
        assertEquals(listOf(2, 9, 12, 1), multi?.chevaux)

        val quinte = combos.find { it.nomJeu == "Quinté+" }
        assertEquals(5, quinte?.nbChevaux)
        assertEquals(listOf(2, 9, 12, 1, 7), quinte?.chevaux)

        val pick5 = combos.find { it.nomJeu == "Pick 5" }
        assertEquals(5, pick5?.nbChevaux)
        assertEquals(listOf(2, 9, 12, 1, 7), pick5?.chevaux)
    }

    @Test
    fun `test firestore licence entity status and expiration logic`() {
        val now = System.currentTimeMillis()

        // Active licence with 10 days remaining
        val activeLicence = FirestoreLicence(
            code = "PMU-TEST-AAAA-BBBB",
            expire_le = Timestamp(Date(now + TimeUnit.DAYS.toMillis(10))),
            statut = FirestoreLicence.STATUT_ACTIF,
            client_name = "Jean Dupont",
            created_at = Timestamp(Date(now))
        )
        assertFalse(activeLicence.isExpired)
        assertEquals(FirestoreLicence.STATUT_ACTIF, activeLicence.effectiveStatus)
        assertTrue(activeLicence.remainingDays in 10..11)

        // Expired licence
        val expiredLicence = FirestoreLicence(
            code = "PMU-EXPI-CCCC-DDDD",
            expire_le = Timestamp(Date(now - TimeUnit.DAYS.toMillis(5))),
            statut = FirestoreLicence.STATUT_ACTIF,
            client_name = "Client Expiré",
            created_at = Timestamp(Date(now - TimeUnit.DAYS.toMillis(15)))
        )
        assertTrue(expiredLicence.isExpired)
        assertEquals(FirestoreLicence.STATUT_EXPIRE, expiredLicence.effectiveStatus)
        assertEquals(0L, expiredLicence.remainingDays)

        // Blocked licence
        val blockedLicence = activeLicence.copy(statut = FirestoreLicence.STATUT_BLOQUE)
        assertEquals(FirestoreLicence.STATUT_BLOQUE, blockedLicence.effectiveStatus)

        // Revoked licence
        val revokedLicence = activeLicence.copy(statut = FirestoreLicence.STATUT_REVOQUE)
        assertEquals(FirestoreLicence.STATUT_REVOQUE, revokedLicence.effectiveStatus)
    }
}
