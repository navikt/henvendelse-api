package no.nav.henvendelse.rest.behandlehenvendelse

import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.behandlehenvendelse.BehandleHenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/behandlehenvendelse")
class BehandleHenvendelseController : BehandleHenvendelseApi {
    @Autowired
    lateinit var porttype: BehandleHenvendelsePortType

    @PostMapping("/ferdigstillutensvar")
    override fun ferdigstillUtenSvar(@RequestBody request: FerdigstillRequest) {
        porttype.ferdigstillUtenSvar(request.behandlingskjedeId, request.enhetId)
    }

    @PostMapping("/oppdaterkontorsperre")
    override fun oppdaterKontorsperre(@RequestBody request: KontorsperreRequest) {
        porttype.oppdaterKontorsperre(request.enhet, request.behandlingsIdListe)
    }

    @PostMapping("/oppdatertilkassering")
    override fun oppdaterTilKassering(@RequestBody request: OppdaterKasseringRequest) {
        porttype.oppdaterTilKassering(request.behandlingIdListe)
    }

    @PostMapping("/oppdatertemagruppe")
    override fun oppdaterTemagruppe(@RequestBody request: OppdaterTemagruppeRequest) {
        porttype.oppdaterTemagruppe(request.behandlingsId, request.temagruppe.name)
    }

    @PostMapping("/knyttbehandlingskjedetilsak")
    override fun knyttBehandlingskjedeTilSak(@RequestBody request: KnyttTilSakRequest) {
        porttype.knyttBehandlingskjedeTilSak(
            request.behandlingskjedeId,
            request.saksId,
            request.temakode, request.journalforendeEnhet
        )
    }

    @GetMapping("/ping")
    override fun ping() {
        porttype.ping()
    }
}
