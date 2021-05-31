package no.nav.henvendelse.rest.behandlehenvendelse

import assertk.assertThat
import assertk.assertions.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.behandlehenvendelse.BehandleHenvendelsePortType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@WebMvcTest(BehandleHenvendelseController::class)
internal class BehandleHenvendelseControllerMvcTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectmapper: ObjectMapper

    @MockkBean
    lateinit var porttype: BehandleHenvendelsePortType

    @Test
    fun `returnerer 400 om request ikke er i henhold til kontrakten (må ha en request body)`() {
        mockMvc
            .post(
                url = "/api/v1/behandlehenvendelse/ferdigstillutensvar",
                assertions = {
                    assertThat(status).isEqualTo(400)
                }
            )
    }

    @Test
    fun `returnerer 406 om dataene ikke passerer validering (enhet har feil format)`() {
        mockMvc
            .post(
                url = "/api/v1/behandlehenvendelse/ferdigstillutensvar",
                body = mapOf(
                    "behandlingskjedeId" to "10ACBDEF0",
                    "enhetId" to "219"
                ),
                assertions = {
                    assertThat(status).isEqualTo(406)
                    assertThat(errorMessage)
                        .isNotNull()
                        .contains("EnhetId må ha lengde 4. [219]")
                }
            )
    }

    @Test
    fun `returnerer 200 om kall til henvendelse er ok`() {
        every { porttype.ferdigstillUtenSvar(any(), any()) } returns Unit
        mockMvc
            .post(
                url = "/api/v1/behandlehenvendelse/ferdigstillutensvar",
                body = mapOf(
                    "behandlingskjedeId" to "10ACBDEF0",
                    "enhetId" to "0219"
                ),
                assertions = {
                    assertThat(status).isEqualTo(200)
                }
            )
    }

    @Test
    fun `feil fra henvendelse bobler opp til spring-web`() {
        every { porttype.ferdigstillUtenSvar(any(), any()) } throws IllegalStateException("Noe gikk feil")
        assertThat {
            mockMvc
                .post(
                    url = "/api/v1/behandlehenvendelse/ferdigstillutensvar",
                    body = mapOf(
                        "behandlingskjedeId" to "10ACBDEF0",
                        "enhetId" to "0219"
                    )
                )
        }
            .isFailure()
            .hasCause(IllegalStateException("Noe gikk feil"))
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
}
