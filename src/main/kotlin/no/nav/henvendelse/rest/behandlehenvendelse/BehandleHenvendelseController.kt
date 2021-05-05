package no.nav.henvendelse.rest.behandlehenvendelse

import io.swagger.annotations.ApiImplicitParam
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.behandlehenvendelse.BehandleHenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/behandlehenvendelse")
class BehandleHenvendelseController : BehandleHenvendelseApi {
    @Autowired
    lateinit var porttype: BehandleHenvendelsePortType

    @PostMapping("/ferdigstillutensvar")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun ferdigstillUtenSvar(@RequestBody request: FerdigstillRequest) {
        porttype.ferdigstillUtenSvar(request.behandlingskjedeId, request.enhetId)
    }

    @PostMapping("/oppdaterkontorsperre")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun oppdaterKontorsperre(@RequestBody request: KontorsperreRequest) {
        porttype.oppdaterKontorsperre(request.enhet, request.behandlingsIdListe)
    }

    @PostMapping("/oppdatertilkassering")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun oppdaterTilKassering(@RequestBody request: OppdaterKasseringRequest) {
        porttype.oppdaterTilKassering(request.behandlingIdListe)
    }

    @PostMapping("/oppdatertemagruppe")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun oppdaterTemagruppe(@RequestBody request: OppdaterTemagruppeRequest) {
        porttype.oppdaterTemagruppe(request.behandlingsId, request.temagruppe.name)
    }

    @PostMapping("/knyttbehandlingskjedetilsak")
    @ApiImplicitParam(name = "X-Correlation-Id", paramType = "header", required = true)
    override fun knyttBehandlingskjedeTilSak(@RequestBody request: KnyttTilSakRequest) {
        porttype.knyttBehandlingskjedeTilSak(
            request.behandlingskjedeId,
            request.saksId,
            request.temakode, request.journalforendeEnhet
        )
    }
}
