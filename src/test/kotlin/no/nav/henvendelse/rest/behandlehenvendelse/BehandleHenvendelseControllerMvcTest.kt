package no.nav.henvendelse.rest.behandlehenvendelse

import assertk.assertThat
import assertk.assertions.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.henvendelse.consumer.pdl.PdlService
import no.nav.henvendelse.consumer.sak.SakApi
import no.nav.henvendelse.consumer.sak.SakDto
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.behandlehenvendelse.BehandleHenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseRequest
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseResponse
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
    lateinit var sakApi: SakApi

    @MockkBean
    lateinit var pdlService: PdlService

    @MockkBean
    lateinit var henvendelsePorttype: HenvendelsePortType

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
    fun `journalføring på sak skal fungere om det er samsvar mellom sak og henvendelse`() {
        every { sakApi.hentSak(any()) } returns SakDto(aktoerId = "00012345678910")
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
                    verify { sakApi.hentSak("123465") }
                    verify { henvendelsePorttype.hentHenvendelse(WSHentHenvendelseRequest().withBehandlingsId("10ACBDEF0")) }
                }
            )
    }

    @Test
    fun `journalføring på sak skal fungere om pdl mener aktorIden er endret`() {
        every { sakApi.hentSak(any()) } returns SakDto(aktoerId = "00012345678910")
        every { henvendelsePorttype.hentHenvendelse(any()) } returns WSHentHenvendelseResponse()
            .withAny(XMLHenvendelse().withAktorId("12345678910000").withFnr("12345678910"))
        every { porttype.knyttBehandlingskjedeTilSak(any(), any(), any(), any()) } returns Unit
        every { pdlService.hentAktorIder(any()) } returns listOf("00012345678910", "12345678910000")

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
                    verify { sakApi.hentSak("123465") }
                    verify { henvendelsePorttype.hentHenvendelse(WSHentHenvendelseRequest().withBehandlingsId("10ACBDEF0")) }
                    verify { pdlService.hentAktorIder("12345678910") }
                }
            )
    }

    @Test
    fun `journalføring på sak skal feile om sak ikke har eierskap`() {
        every { sakApi.hentSak(any()) } returns SakDto(id = "1234")
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
                        .contains("Saksid 1234 hadde ingen aktorId")
                }
            )
    }

    @Test
    fun `journalføring på sak skal feile om henvendelse ikke har eierskap`() {
        every { sakApi.hentSak(any()) } returns SakDto(aktoerId = "12345678910000")
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
                        .contains("Henvendelse 12345ABBA hadde ingen lagret aktorId")
                }
            )
    }

    @Test
    fun `journalføring på sak skal feile om pdl ikke kan mappe fnr til saks aktørid`() {
        every { sakApi.hentSak(any()) } returns SakDto(aktoerId = "00012345678910")
        every { henvendelsePorttype.hentHenvendelse(any()) } returns WSHentHenvendelseResponse()
            .withAny(XMLHenvendelse().withAktorId("12345678910000").withFnr("12345678910"))
        every { porttype.knyttBehandlingskjedeTilSak(any(), any(), any(), any()) } returns Unit
        every { pdlService.hentAktorIder(any()) } returns listOf("000987654321", "987654321000")

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
                        .contains("Henvendelse/Sak hadde forskjellige aktorId lagret, og oppslags vha PDL feilet.")
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
