package no.nav.henvendelse.rest.henvendelseinformasjon

import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiParam
import no.nav.henvendelse.naudit.Audit
import no.nav.henvendelse.naudit.Audit.Companion.describe
import no.nav.henvendelse.naudit.Audit.Companion.withAudit
import no.nav.henvendelse.naudit.AuditIdentifier
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.Henvendelse
import no.nav.henvendelse.rest.common.HenvendelseType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentBehandlingskjedeRequest
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseListeRequest
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/henvendelseinformasjon")
class HenvendelseInformasjonController : HenvendelseApi {
    @Autowired
    lateinit var porttype: HenvendelsePortType

    @GetMapping("/henthenvendelse")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun hentHenvendelse(
        @ApiParam(example = "1001ABBA")
        @RequestParam("behandlingsId") behandlingsId: String
    ): HentHenvendelseResponse {
        val identifiers = arrayOf(
            AuditIdentifier.BEHANDLINGSID to behandlingsId
        )
        return withAudit(describe(Audit.Action.READ, Henvendelse, *identifiers)) {
            porttype
                .hentHenvendelse(
                    WSHentHenvendelseRequest()
                        .withBehandlingsId(behandlingsId)
                )
                .fromWS()
        }
    }

    @GetMapping("/hentbehandlingskjede")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun hentBehandlingskjede(
        @ApiParam(example = "1001ABBA")
        @RequestParam("behandlingskjedeId") behandlingskjedeId: String
    ): HentBehandlingskjedeResponse {
        val identifiers = arrayOf(
            AuditIdentifier.BEHANDLINGSKJEDEID to behandlingskjedeId
        )
        return withAudit(describe(Audit.Action.READ, Henvendelse, *identifiers)) {
            porttype
                .hentBehandlingskjede(
                    WSHentBehandlingskjedeRequest()
                        .withBehandlingskjedeId(behandlingskjedeId)
                )
                .fromWS()
        }
    }

    @GetMapping("/henthenvendelseliste")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun hentHenvendelseListe(
        @ApiParam(example = "12345678910")
        @RequestParam("fodselsnummer") fodselsnummer: String,
        @ApiParam(example = "[SVAR_OPPMOTE, REFERAT_OPPMOTE]")
        @RequestParam("typer") typer: List<HenvendelseType>
    ): HentHenvendelseListeResponse {
        val identifiers = arrayOf(
            AuditIdentifier.FNR to fodselsnummer,
            AuditIdentifier.HENVENDELSETYPER to typer.joinToString(", "),
        )
        return withAudit(describe(Audit.Action.READ, Henvendelse, *identifiers)) {
            porttype.hentHenvendelseListe(
                WSHentHenvendelseListeRequest()
                    .withFodselsnummer(fodselsnummer)
                    .withTyper(typer.map { it.name })
            ).fromWS()
        }
    }
}
