package no.nav.henvendelse.rest.common

import assertk.assertThat
import assertk.assertions.*
import no.nav.henvendelse.rest.common.Verification.requireOptional
import no.nav.henvendelse.rest.common.Verification.verify
import no.nav.henvendelse.rest.common.Verification.verifyBehandlingsId
import no.nav.henvendelse.rest.common.Verification.verifyBehandlingsKjedeId
import no.nav.henvendelse.rest.common.Verification.verifyEnhet
import no.nav.henvendelse.rest.common.Verification.verifyFnr
import no.nav.henvendelse.rest.common.Verification.verifySaksId
import no.nav.henvendelse.rest.common.Verification.verifyTemakode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

internal class VerificationTest {
    @Nested
    inner class VerifyBehandlingsId {
        @Test
        fun `behandlingsId skal kunne valideres`() {
            valid { verifyBehandlingsId("10ABBAABA") }
            valid { verifyBehandlingsKjedeId("10ABBAABA") }
        }

        @Test
        fun `behandlingsId skal alltid starte med 10`() {
            invalid("BehandlingsId må starte med 10.") {
                verifyBehandlingsId("11ABBAABA")
            }
            invalid("BehandlingsKjedeId må starte med 10.") {
                verifyBehandlingsKjedeId("11ABBAABA")
            }
        }

        @Test
        fun `behandlingsId skal ha lenge 9`() {
            invalid("BehandlingsId må ha lengde 9.") {
                verifyBehandlingsId("10ABBAAB")
            }
            invalid("BehandlingsKjedeId må ha lengde 9.") {
                verifyBehandlingsKjedeId("10ABBAAB")
            }
        }

        @Test
        fun `behandlingsId skal base inneholde base36`() {
            invalid("BehandlingsId må være gyldig base36.") {
                verifyBehandlingsId("10ABBAABÆ")
            }
            invalid("BehandlingsKjedeId må være gyldig base36.") {
                verifyBehandlingsKjedeId("10ABBAABÆ")
            }
        }
    }

    @Nested
    inner class VerifySaksId {
        @Test
        fun `saksId skal kunne valideres`() {
            valid { verifySaksId("123456789") }
        }

        @Test
        fun `saksId skal ha innhold`() {
            invalid("SaksId kan ikke være tom streng.") {
                verifySaksId("")
            }
        }

        @Test
        fun `saksId kan ikke inneholde annet enn tall`() {
            invalid("SaksId skal bare inneholde tall.") {
                verifySaksId("12345678A")
            }
        }
    }

    @Nested
    inner class VerifyTemakode {
        @Test
        fun `temakode skal kunne valideres`() {
            valid { verifyTemakode("DAG") }
        }

        @Test
        fun `temakode skal ha innhold`() {
            invalid("Temakode kan ikke være tom streng.") {
                verifyTemakode("")
            }
        }

        @Test
        fun `temakode kan ikke inneholde annet enn store bokstaver`() {
            invalid("Temakode skal bare inneholde store bokstaver.") {
                verifyTemakode("DAG1")
            }
        }
    }

    @Nested
    inner class VerifyFnr {
        @Test
        fun `fnr skal kunne valideres`() {
            valid { verifyFnr("12345678910") }
        }

        @Test
        fun `fnr skal ha innhold`() {
            invalid("Fødselsnummer må ha lengde 11.") {
                verifyFnr("1234567891")
            }
        }

        @Test
        fun `fnr kan ikke inneholde annet enn tall`() {
            invalid("Fødselsnummer skal bare inneholde tall.") {
                verifyFnr("1234567891A")
            }
        }
    }

    @Nested
    inner class VerifyEnhet {
        @Test
        fun `enhet skal kunne valideres`() {
            valid { verifyEnhet("0219") }
        }

        @Test
        fun `enhet skal ha innhold`() {
            invalid("EnhetId må ha lengde 4.") {
                verifyEnhet("219")
            }
        }

        @Test
        fun `enhet kan ikke inneholde annet enn tall`() {
            invalid("EnhetId skal bare inneholde tall.") {
                verifyEnhet("219A")
            }
        }
    }

    @Nested
    inner class Verify {
        @Test
        fun `skal ikke kaste feil om valid`() {
            valid {
                verify(valid = true) {
                    "Denne skal ikke kastes"
                }
            }
        }

        @Test
        fun `skal kaste feil om invalid`() {
            invalid("Forventet feilmelding") {
                verify(valid = false) {
                    "Forventet feilmelding"
                }
            }
        }
    }

    @Nested
    inner class RequiredOptional {
        @Test
        fun `skal returnere verdi om tilgjengelig`() {
            valid {
                requireOptional(Optional.of("valid value")) {
                    "Feilmelding"
                }
            }.isEqualTo("valid value")
        }

        @Test
        fun `skal kaste feil om ikke tilgjengelig`() {
            invalid("Kunne ikke hente verdi fra optional") {
                requireOptional<String>(Optional.empty()) {
                    "Kunne ikke hente verdi fra optional"
                }
            }
        }
    }
}

private fun <T> invalid(message: String, fn: () -> T) =
    assertThat(fn)
        .isFailure()
        .isInstanceOf(RestInvalidDataException::class)
        .messageContains(message)

private fun <T> valid(fn: () -> T) =
    assertThat(fn).isSuccess()
