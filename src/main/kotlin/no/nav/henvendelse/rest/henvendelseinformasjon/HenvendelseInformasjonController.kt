package no.nav.henvendelse.rest.henvendelseinformasjon

import io.swagger.annotations.*
import no.nav.henvendelse.naudit.Audit
import no.nav.henvendelse.naudit.Audit.Companion.describe
import no.nav.henvendelse.naudit.Audit.Companion.withAudit
import no.nav.henvendelse.naudit.AuditIdentifier
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.Henvendelse
import no.nav.henvendelse.rest.common.HenvendelseType
import no.nav.henvendelse.rest.common.Verification.verify
import no.nav.henvendelse.rest.common.Verification.verifyBehandlingsId
import no.nav.henvendelse.rest.common.Verification.verifyBehandlingsKjedeId
import no.nav.henvendelse.rest.common.Verification.verifyFnr
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
@Api(description = "APIer for uthenting av henvendelser")
class HenvendelseInformasjonController(
    @Autowired val porttype: HenvendelsePortType
) : HenvendelseApi {

    @GetMapping("/henthenvendelse")
    @ApiOperation(
        value = "Hent henvendelse",
        notes = """
            Henter ut enkelt henvendelse gitt behandlingsId.
            Dette vil typisk kun være nødvendig om man akkurat har opprettet en henvendelse, 
            og vil hente denne spesifike henvendelsen ut igjen.
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Henvendelse"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
            ApiResponse(code = 500, message = "Ukjent feil, sannsynligvis fra henvendelse"),
        ]
    )
    override fun hentHenvendelse(
        @ApiParam(example = "1001ABBA")
        @RequestParam("behandlingsId") behandlingsId: String
    ): HentHenvendelseResponse {
        val identifiers = arrayOf(
            AuditIdentifier.BEHANDLINGSID to behandlingsId
        )
        return withAudit(describe(Audit.Action.READ, Henvendelse, *identifiers)) {
            verifyBehandlingsId(behandlingsId)
            porttype
                .hentHenvendelse(
                    WSHentHenvendelseRequest()
                        .withBehandlingsId(behandlingsId)
                )
                .fromWS()
        }
    }

    @GetMapping("/hentbehandlingskjede")
    @ApiOperation(
        value = "Hent behandlingskjede",
        notes = """
            Henter ut enkelt alle henvendelser med samme behandlingskjedeId (dialog).
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Henvendelsene"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
            ApiResponse(code = 500, message = "Ukjent feil, sannsynligvis fra henvendelse"),
        ]
    )
    override fun hentBehandlingskjede(
        @ApiParam(example = "1001ABBA")
        @RequestParam("behandlingskjedeId") behandlingskjedeId: String
    ): HentBehandlingskjedeResponse {
        val identifiers = arrayOf(
            AuditIdentifier.BEHANDLINGSKJEDEID to behandlingskjedeId
        )
        return withAudit(describe(Audit.Action.READ, Henvendelse, *identifiers)) {
            verifyBehandlingsKjedeId(behandlingskjedeId)
            porttype
                .hentBehandlingskjede(
                    WSHentBehandlingskjedeRequest()
                        .withBehandlingskjedeId(behandlingskjedeId)
                )
                .fromWS()
        }
    }

    @GetMapping("/henthenvendelseliste")
    @ApiOperation(
        value = "Hent brukers henvendelse",
        notes = """
            Henter ut alle henvendelser av en gitt henvendelseType tilknyttet bruker (fnr).
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Henvendelsene"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
            ApiResponse(code = 500, message = "Ukjent feil, sannsynligvis fra henvendelse"),
        ]
    )
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
            verifyFnr(fodselsnummer)
            verify(typer.isNotEmpty()) { "Typer-liste kan ikke være tom" }

            porttype.hentHenvendelseListe(
                WSHentHenvendelseListeRequest()
                    .withFodselsnummer(fodselsnummer)
                    .withTyper(typer.map { it.name })
            ).fromWS()
        }
    }
}
