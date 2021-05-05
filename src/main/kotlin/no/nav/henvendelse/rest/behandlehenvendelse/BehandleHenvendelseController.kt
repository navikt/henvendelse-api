package no.nav.henvendelse.rest.behandlehenvendelse

import io.swagger.annotations.ApiImplicitParam
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
class BehandleHenvendelseController : BehandleHenvendelseApi {
    @Autowired
    lateinit var porttype: BehandleHenvendelsePortType

    @PostMapping("/ferdigstillutensvar")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun ferdigstillUtenSvar(@RequestBody request: FerdigstillRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSKJEDEID to request.behandlingskjedeId
        )
        withAudit(describe(UPDATE, FerdigstillUtenSvar, *identifiers)) {
            porttype.ferdigstillUtenSvar(request.behandlingskjedeId, request.enhetId)
        }
    }

    @PostMapping("/oppdaterkontorsperre")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun oppdaterKontorsperre(@RequestBody request: KontorsperreRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSIDLISTE to request.behandlingsIdListe.joinToString(", ")
        )
        withAudit(describe(UPDATE, OppdaterKontorsperre, *identifiers)) {
            porttype.oppdaterKontorsperre(request.enhet, request.behandlingsIdListe)
        }
    }

    @PostMapping("/oppdatertilkassering")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun oppdaterTilKassering(@RequestBody request: OppdaterKasseringRequest) {
        val identifiers = arrayOf(
            BEHANDLINGSIDLISTE to request.behandlingsIdListe.joinToString(", ")
        )
        withAudit(describe(UPDATE, OppdaterTilKassering, *identifiers)) {
            porttype.oppdaterTilKassering(request.behandlingsIdListe)
        }
    }

    @PostMapping("/oppdatertemagruppe")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
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
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
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
