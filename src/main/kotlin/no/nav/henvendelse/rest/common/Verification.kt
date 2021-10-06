package no.nav.henvendelse.rest.common

import no.nav.henvendelse.consumer.saf.SafService
import no.nav.henvendelse.rest.behandlehenvendelse.KnyttTilSakRequest
import no.nav.henvendelse.rest.henvendelseinformasjon.fromWS
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseRequest
import org.slf4j.LoggerFactory
import java.util.*

object Verification {
    var allowSoftVerification = true
    private val log = LoggerFactory.getLogger(Verification::class.java)

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

    fun verifySammeEierskapAvSakOgHenvendelse(
        request: KnyttTilSakRequest,
        saf: SafService,
        henvendelsePorttype: HenvendelsePortType
    ) {
        try {
            val henvendelse = henvendelsePorttype.hentHenvendelse(
                WSHentHenvendelseRequest().withBehandlingsId(request.behandlingskjedeId)
            ).fromWS()
            val fnr = verifyNotNull(henvendelse.henvendelse.fnr) {
                "Henvendelse ${request.behandlingskjedeId} hadde ingen lagret fnr"
            }
            val saker = saf.hentSaker(fnr)
            val saksReferanse = saker.find { it.arkivsaksnummer == request.saksId }
            verify(saksReferanse != null, softVerify = true) {
                """
                    SAF hadde ikke sak (${request.saksId}) lagret for bruker $fnr.
                    Saker: $saker
                """.trimIndent()
            }
            verify(saksReferanse?.tema?.name == request.temakode, softVerify = true) {
                """
                    Mismatch av temakode mellom SAF og request.
                    SAF: ${saksReferanse?.tema?.name}
                    Req: ${request.temakode}
                """.trimIndent()
            }
        } catch (e: RestInvalidDataException) {
            throw e
        } catch (e: Exception) {
            log.error("Soft-Verification failed verifySammeEierskapAvSakOgHenvendelse", e)
        }
    }

    fun verify(valid: Boolean, softVerify: Boolean = false, lazyMessage: () -> String) {
        if (!valid) {
            if (allowSoftVerification && softVerify) {
                log.warn("Soft-Verification failed: ${lazyMessage()}")
            } else {
                throw RestInvalidDataException(lazyMessage())
            }
        } else if (softVerify) {
            log.info("Soft-Verification success: ${lazyMessage()} ")
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
