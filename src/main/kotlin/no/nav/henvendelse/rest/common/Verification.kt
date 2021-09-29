package no.nav.henvendelse.rest.common

import no.nav.henvendelse.consumer.pdl.PdlService
import no.nav.henvendelse.consumer.sak.SakDto
import no.nav.henvendelse.rest.henvendelseinformasjon.HentHenvendelseResponse
import java.util.*

object Verification {
    fun <T> verifyNotNull(value: T?, lazyMessage: () -> String): T {
        return runCatching {
            requireNotNull(value)
        }.fold(
            onSuccess = { it },
            onFailure = {
                throw RestInvalidDataException(lazyMessage())
            }
        )
    }
    fun verifyBehandlingsId(behandlingsId: String) {
        verify(behandlingsId.startsWith("10")) { "BehandlingsId må starte med 10. [$behandlingsId]" }
        verify(behandlingsId.length == 9) { "BehandlingsId må ha lengde 9. [$behandlingsId]" }
        verify(behandlingsId.isBase36()) { "BehandlingsId må være gyldig base36. [$behandlingsId]" }
    }
    fun verifyBehandlingsKjedeId(behandlingsKjedeId: String) {
        verify(behandlingsKjedeId.startsWith("10")) { "BehandlingsKjedeId må starte med 10. [$behandlingsKjedeId]" }
        verify(behandlingsKjedeId.length == 9) { "BehandlingsKjedeId må ha lengde 9. [$behandlingsKjedeId]" }
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

    fun verifySammeEierskapAvSakOgHenvendelse(pdlService: PdlService, sak: SakDto, henvendelse: HentHenvendelseResponse) {
        val sakAktorId = verifyNotNull(sak.aktoerId) { "Saksid ${sak.id} hadde ingen aktorId" }
        val henvendelseAktorId = verifyNotNull(henvendelse.henvendelse.aktorId) {
            "Henvendelse ${henvendelse.henvendelse.behandlingsId} hadde ingen lagret aktorId"
        }

        if (sakAktorId == henvendelseAktorId) {
            return
        } else {
            val fnr = henvendelse.henvendelse.fnr
            val aktorIder = fnr?.let { pdlService.hentAktorIder(it) } ?: emptyList()
            verify(aktorIder.contains(sakAktorId)) {
                """
                    Henvendelse/Sak hadde forskjellige aktorId lagret, og oppslags vha PDL feilet.
                    Henvendelse-fnr: $fnr
                    Henvendelse-aktorId: $henvendelseAktorId
                    SakAktorId: $sakAktorId
                    PdlAktorId: ${aktorIder.joinToString(", ")}
                """.trimIndent()
            }
        }
    }

    fun verify(valid: Boolean, lazyMessage: () -> String) {
        if (!valid) {
            throw RestInvalidDataException(lazyMessage())
        }
    }

    fun <T> requireOptional(optional: Optional<T>, lazyMessage: () -> String): T {
        return optional.orElseThrow {
            RestInvalidDataException(lazyMessage())
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
