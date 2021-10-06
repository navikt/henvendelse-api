package no.nav.henvendelse.rest.henvendelseinformasjon

import assertk.assertThat
import assertk.assertions.*
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@WebMvcTest(HenvendelseInformasjonController::class)
internal class HenvendelseInformasjonControllerMvcTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var porttype: HenvendelsePortType

    @Test
    fun `returnerer 400 om request ikke er i henhold til kontrakten (behandlingsId må være med)`() {
        mockMvc
            .get(
                url = "/api/v1/henvendelseinformasjon/henthenvendelse",
                assertions = {
                    assertThat(status).isEqualTo(400)
                }
            )
    }

    @Test
    fun `returnerer 406 om dataene ikke passerer validering (behandlingsId må ha riktig format)`() {
        mockMvc
            .get(
                url = "/api/v1/henvendelseinformasjon/henthenvendelse?behandlingsId=ABBA12345",
                assertions = {
                    assertThat(status).isEqualTo(406)
                    assertThat(errorMessage)
                        .isNotNull()
                        .contains("BehandlingsId må starte med 10")
                }
            )
    }

    @Test
    fun `returnerer 200 om kall til henvendelse er ok`() {
        every { porttype.hentHenvendelse(any()) } returns WSHentHenvendelseResponse()
            .withAny(XMLHenvendelse())

        mockMvc
            .get(
                url = "/api/v1/henvendelseinformasjon/henthenvendelse?behandlingsId=10001ABBA",
                assertions = {
                    assertThat(status).isEqualTo(200)
                }
            )
    }

    @Test
    fun `feil fra henvendelse bobler opp til spring-web`() {
        every { porttype.hentHenvendelse(any()) } throws IllegalStateException("Noe gikk feil")
        assertThat {
            mockMvc
                .get(
                    url = "/api/v1/henvendelseinformasjon/henthenvendelse?behandlingsId=10001ABBA",
                )
        }
            .isFailure()
            .hasCause(IllegalStateException("Noe gikk feil"))
    }

    private fun MockMvc.get(url: String, assertions: (MockHttpServletResponse.() -> Unit)? = null) {
        this.perform(
            MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect {
                assertions?.invoke(it.response)
            }
    }
}
