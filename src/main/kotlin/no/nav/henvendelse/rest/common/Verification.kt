package no.nav.henvendelse.rest.common

import java.util.*

object Verification {
    fun verifyBehandlingsId(behandlingsId: String) {
        verify(behandlingsId.startsWith("10")) { "BehandlingsId må starte med 10. [$behandlingsId]" }
        verify(behandlingsId.isBase36()) { "BehandlingsId må være gyldig base36. [$behandlingsId]" }
    }
    fun verifyBehandlingsKjedeId(behandlingsKjedeId: String) {
        verify(behandlingsKjedeId.startsWith("10")) { "BehandlingsKjedeId må starte med 10. [$behandlingsKjedeId]" }
        verify(behandlingsKjedeId.isBase36()) { "BehandlingsKjedeId må være gyldig base36. [$behandlingsKjedeId]" }
    }

    fun verifySaksId(saksId: String) {
        verify(saksId.isNotEmpty()) { "SaksId kan ikke være tom streng. [$saksId]" }
        verify(saksId.isDigits()) { "SaksId skal bare inneholde tall. [$saksId]" }
    }

    fun verifyTemakode(temakode: String) {
        verify(temakode.isNotEmpty()) { "Temakode kan ikke være tom streng. [$temakode]" }
        verify(temakode.isUpperCaseLetters()) { "Temakode skal bare inneholde store bokstaver. [$temakode]" }
    }

    fun verifyFnr(fnr: String) {
        verify(fnr.length == 11) { "Fødselsnummer må ha lengde 11. [$fnr]" }
        verify(fnr.isDigits()) { "Fødselsnummer skal bare inneholde tall. [$fnr]" }
    }

    fun verifyEnhet(enhet: String) {
        verify(enhet.length == 4) { "EnhetId må ha lengde 4. [$enhet]" }
        verify(enhet.isDigits()) { "EnhetId skal bare inneholde tall. [$enhet]" }
    }

    fun verify(valid: Boolean, lazyMessage: () -> String) {
        if (!valid) {
            throw RestInvalidDataException(lazyMessage())
        }
    }

    fun <T> requireOptional(optional: Optional<T>): T {
        return optional.orElseThrow {
            RestInvalidDataException("Kunne ikke hente ut navIdent fra JWT")
        }
    }

    private fun String.isDigits(): Boolean {
        return this.all { it.isDigit() }
    }

    private fun String.isUpperCaseLetters(): Boolean {
        return this.all { it.isLetter() && it.isUpperCase() }
    }

    private fun String.isBase36(): Boolean {
        return try {
            this.toLong(36)
            true
        } catch (e: Throwable) {
            false
        }
    }
}
