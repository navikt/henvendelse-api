package no.nav.henvendelse.rest.behandlehenvendelse

import assertk.assertThat
import assertk.assertions.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.henvendelse.consumer.saf.SafService
import no.nav.henvendelse.consumer.saf.queries.HentBrukersSaker
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.behandlehenvendelse.BehandleHenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseRequest
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseResponse
import org.junit.jupiter.api.BeforeEach
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

    @MockkBean
    lateinit var safService: SafService

    @MockkBean
    lateinit var henvendelsePorttype: HenvendelsePortType

    @BeforeEach
    fun setup() {
        every { safService.hentSaker(any()) } returns emptyList()
    }

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

    @Test
    fun `journalføring på sak skal fungere om saf returnerer saksID for henvendelsen eier`() {
        every { safService.hentSaker(any()) } returns listOf(
            HentBrukersSaker.Sak("123465", HentBrukersSaker.Tema.OPP)
        )
        every { henvendelsePorttype.hentHenvendelse(any()) } returns WSHentHenvendelseResponse()
            .withAny(XMLHenvendelse().withAktorId("00012345678910").withFnr("12345678910"))
        every { porttype.knyttBehandlingskjedeTilSak(any(), any(), any(), any()) } returns Unit

        mockMvc
            .post(
                url = "/api/v1/behandlehenvendelse/knyttbehandlingskjedetilsak",
                body = mapOf(
                    "behandlingskjedeId" to "10ACBDEF0",
                    "saksId" to "123465",
                    "temakode" to "OPP",
                    "journalforendeEnhet" to "4100"
                ),
                assertions = {
                    assertThat(status).isEqualTo(200)
                    verify { henvendelsePorttype.hentHenvendelse(WSHentHenvendelseRequest().withBehandlingsId("10ACBDEF0")) }
                }
            )
    }

    @Test
    fun `journalføring på sak skal feile om saf ikke returnerer saksIden som man vil journalføre på`() {
        every { henvendelsePorttype.hentHenvendelse(any()) } returns WSHentHenvendelseResponse()
            .withAny(XMLHenvendelse().withAktorId("12345678910000").withFnr("12345678910"))

        mockMvc
            .post(
                url = "/api/v1/behandlehenvendelse/knyttbehandlingskjedetilsak",
                body = mapOf(
                    "behandlingskjedeId" to "10ACBDEF0",
                    "saksId" to "123465",
                    "temakode" to "OPP",
                    "journalforendeEnhet" to "4100"
                ),
                assertions = {
                    assertThat(status).isEqualTo(406)
                    assertThat(errorMessage)
                        .isNotNull()
                        .contains("SAF hadde ikke sak (123465) lagret for bruker 12345678910.")
                }
            )
    }

    @Test
    fun `journalføring på sak skal feile om henvendelse ikke har eierskap`() {
        every { henvendelsePorttype.hentHenvendelse(any()) } returns WSHentHenvendelseResponse()
            .withAny(XMLHenvendelse().withBehandlingsId("12345ABBA"))

        mockMvc
            .post(
                url = "/api/v1/behandlehenvendelse/knyttbehandlingskjedetilsak",
                body = mapOf(
                    "behandlingskjedeId" to "10ACBDEF0",
                    "saksId" to "123465",
                    "temakode" to "OPP",
                    "journalforendeEnhet" to "4100"
                ),
                assertions = {
                    assertThat(status).isEqualTo(406)
                    assertThat(errorMessage)
                        .isNotNull()
                        .contains("Henvendelse 10ACBDEF0 hadde ingen lagret fnr")
                }
            )
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
