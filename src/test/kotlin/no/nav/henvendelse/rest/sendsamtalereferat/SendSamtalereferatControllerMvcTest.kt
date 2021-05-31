package no.nav.henvendelse.rest.sendsamtalereferat

import assertk.assertThat
import assertk.assertions.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.common.auth.context.AuthContext
import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.common.auth.context.UserRole
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.SendUtHenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.meldinger.WSSendUtHenvendelseResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@WebMvcTest(SendSamtalereferatController::class)
internal class SendSamtalereferatControllerMvcTest {
    @TestConfiguration
    class TestConfig {
        @Bean
        fun authContext() = AuthContextHolderThreadLocal.instance()
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectmapper: ObjectMapper

    @MockkBean
    lateinit var porttype: SendUtHenvendelsePortType

    @Test
    fun `returnerer 400 om request ikke er i henhold til kontrakten (OKSOS er ikke godkjent temagruppe)`() {
        every { porttype.sendUtHenvendelse(any()) } returns WSSendUtHenvendelseResponse()
            .withBehandlingsId("1001ABBA")

        withAuthContext("Z999999") {
            mockMvc
                .post(
                    url = "/api/v1/sendsamtalereferat/send",
                    body = mapOf(
                        "fnr" to "12345678910",
                        "enhet" to "0219",
                        "fritekst" to "Innhold av melding her",
                        "temagruppe" to "OKSOS",
                        "kanal" to "TELEFON"
                    ),
                    assertions = {
                        assertThat(status).isEqualTo(400)
                    }
                )
        }
    }

    @Test
    fun `returnerer 406 om dataene ikke passerer validering (enhet har feil format)`() {
        withAuthContext("Z999999") {
            mockMvc
                .post(
                    url = "/api/v1/sendsamtalereferat/send",
                    body = mapOf(
                        "fnr" to "12345678910",
                        "enhet" to "219",
                        "fritekst" to "Innhold av melding her",
                        "temagruppe" to "ARBD",
                        "kanal" to "TELEFON"
                    ),
                    assertions = {
                        assertThat(status).isEqualTo(406)
                        assertThat(errorMessage)
                            .isNotNull()
                            .contains("EnhetId må ha lengde 4. [219]")
                    }
                )
        }
    }

    @Test
    fun `returnerer 200 om kall til henvendelse er ok`() {
        every { porttype.sendUtHenvendelse(any()) } returns WSSendUtHenvendelseResponse()
            .withBehandlingsId("1001ABBA")

        withAuthContext("Z999999") {
            mockMvc
                .post(
                    url = "/api/v1/sendsamtalereferat/send",
                    body = mapOf(
                        "fnr" to "12345678910",
                        "enhet" to "0219",
                        "fritekst" to "Innhold av melding her",
                        "temagruppe" to "ARBD",
                        "kanal" to "TELEFON"
                    ),
                    assertions = {
                        assertThat(status).isEqualTo(200)
                    }
                )
        }
    }

    @Test
    fun `feil fra henvendelse bobler opp til spring-web`() {
        every { porttype.sendUtHenvendelse(any()) } throws IllegalStateException("Noe gikk feil")

        withAuthContext("Z999999") {
            assertThat {
                mockMvc
                    .post(
                        url = "/api/v1/sendsamtalereferat/send",
                        body = mapOf(
                            "fnr" to "12345678910",
                            "enhet" to "0219",
                            "fritekst" to "Innhold av melding her",
                            "temagruppe" to "ARBD",
                            "kanal" to "TELEFON"
                        )
                    )
            }
                .isFailure()
                .hasCause(IllegalStateException("Noe gikk feil"))
        }
    }

    private fun MockMvc.post(url: String, body: Any? = null, assertions: (MockHttpServletResponse.() -> Unit)? = null) {
        this.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .apply {
                    body
                        ?.let { objectmapper.writeValueAsString(it) }
                        ?.also { content(it) }
                }
        )
            .andExpect {
                assertions?.invoke(it.response)
            }
    }

    private fun <T> withAuthContext(subject: String, fn: () -> T): T {
        val context = AuthContext(UserRole.INTERN, createJWT(subject))
        return AuthContextHolderThreadLocal.instance().withContext(context, fn)
    }

    private fun createJWT(subject: String): JWT {
        val claimset = JWTClaimsSet.Builder()
            .subject(subject)
            .claim("NAVident", subject)
            .claim("azp", "original-system")
            .build()
        val header = JWSHeader.Builder(JWSAlgorithm.RS256).build()
        return SignedJWT(header, claimset)
    }
}
