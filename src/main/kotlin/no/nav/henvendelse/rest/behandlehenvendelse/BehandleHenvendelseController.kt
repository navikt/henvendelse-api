package no.nav.henvendelse.rest.behandlehenvendelse

import io.swagger.annotations.*
import no.nav.henvendelse.naudit.Audit.Action.UPDATE
import no.nav.henvendelse.naudit.Audit.Companion.describe
import no.nav.henvendelse.naudit.Audit.Companion.withAudit
import no.nav.henvendelse.naudit.AuditIdentifier.*
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.FerdigstillUtenSvar
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.KnyttTilSak
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.OppdaterKontorsperre
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.OppdaterTemagruppe
import no.nav.henvendelse.naudit.AuditResources.Henvendelse.Companion.OppdaterTilKassering
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.behandlehenvendelse.BehandleHenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/behandlehenvendelse")
@Api(description = "APIer for behandling av eksisterende henvendelser")
class BehandleHenvendelseController : BehandleHenvendelseApi {
    @Autowired
    lateinit var porttype: BehandleHenvendelsePortType

    @PostMapping("/ferdigstillutensvar")
    @ApiOperation(
        value = "Marker som ferdigstilt uten svar",
        notes = """
            Markerer dialogen som ferdigstilt uten svar.
            Dialogstyring fanger deretter opp dette, og rapportere om dette til Sak&Behandling            
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Ok"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
        ]
    )
    override fun ferdigstillUtenSvar(@RequestBody request: FerdigstillRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSKJEDEID to request.behandlingskjedeId
        )
        withAudit(describe(UPDATE, FerdigstillUtenSvar, *identifiers)) {
            porttype.ferdigstillUtenSvar(request.behandlingskjedeId, request.enhetId)
        }
    }

    @PostMapping("/oppdaterkontorsperre")
    @ApiOperation(
        value = "Marker som kontorsperret",
        notes = """
            Markerer alle behandlingsIdene til å være kontorsperret, slik at kun oppgitt kontor får tilgang fremover.
            Man markerer alltid hele dialoger, og må derfor sende inn en liste over alle behandligskjedeIdene her.
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Ok"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
        ]
    )
    override fun oppdaterKontorsperre(@RequestBody request: KontorsperreRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSIDLISTE to request.behandlingsIdListe.joinToString(", ")
        )
        withAudit(describe(UPDATE, OppdaterKontorsperre, *identifiers)) {
            porttype.oppdaterKontorsperre(request.enhet, request.behandlingsIdListe)
        }
    }

    @PostMapping("/oppdatertilkassering")
    @ApiOperation(
        value = "Marker som feilsendt",
        notes = """
            Markerer alle behandlingsIdene som feilsendt slik at disse kan bli slettes fra henvendelsesarkiv raskere.
            Man markerer alltid hele dialoger, og må derfor sende inn en liste over alle behandligskjedeIdene her.
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Ok"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
        ]
    )
    override fun oppdaterTilKassering(@RequestBody request: OppdaterKasseringRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSIDLISTE to request.behandlingsIdListe.joinToString(", ")
        )
        withAudit(describe(UPDATE, OppdaterTilKassering, *identifiers)) {
            porttype.oppdaterTilKassering(request.behandlingsIdListe)
        }
    }

    @PostMapping("/oppdatertemagruppe")
    @ApiOperation(
        value = "Endre temagruppe",
        notes = """
            Endrer temagruppe for henvendelsen som blir vedlagt.
            Her må konsument samtidig passe på at eventuelle oppgaver tilknyttet til henvendelsen også blir oppdatert.                
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Ok"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
        ]
    )
    override fun oppdaterTemagruppe(@RequestBody request: OppdaterTemagruppeRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSID to request.behandlingsId,
            TEMAGRUPPE to request.temagruppe.name
        )
        withAudit(describe(UPDATE, OppdaterTemagruppe, *identifiers)) {
            porttype.oppdaterTemagruppe(request.behandlingsId, request.temagruppe.name)
        }
    }

    @PostMapping("/knyttbehandlingskjedetilsak")
    @ApiOperation(
        value = "Journalføring",
        notes = """
            Knytte henvendelse til sak (e.g journalføring)            
        """
    )
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Ok"),
            ApiResponse(code = 401, message = "Ugyldig token"),
            ApiResponse(code = 406, message = "Validering av dataene i requesten feilet, se feilmelding i responsen."),
        ]
    )
    override fun knyttBehandlingskjedeTilSak(@RequestBody request: KnyttTilSakRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSKJEDEID to request.behandlingskjedeId,
            SAK to request.saksId,
            TEMA to request.temakode
        )
        withAudit(describe(UPDATE, KnyttTilSak, *identifiers)) {
            porttype.knyttBehandlingskjedeTilSak(
                request.behandlingskjedeId,
                request.saksId,
                request.temakode, request.journalforendeEnhet
            )
        }
    }
}
