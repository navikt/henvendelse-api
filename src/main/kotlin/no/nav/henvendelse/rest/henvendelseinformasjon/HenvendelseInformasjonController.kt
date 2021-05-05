package no.nav.henvendelse.rest.henvendelseinformasjon

import no.nav.henvendelse.rest.common.HenvendelseType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/henvendelseinformasjon")
class HenvendelseInformasjonController : HenvendelseApi {
    @Autowired
    lateinit var porttype: HenvendelsePortType

    @GetMapping("/henthenvendelse")
    override fun hentHenvendelse(
        @RequestParam("behandlingsId") behandlingsId: String
    ): HentHenvendelseResponse {
        return porttype.hentHenvendelse(
            WSHentHenvendelseRequest()
                .withBehandlingsId(behandlingsId)
        ).fromWS()
    }

    @GetMapping("/hentbehandlingskjede")
    override fun hentBehandlingskjede(
        @RequestParam("behandlingskjedeId") behandlingskjedeId: String
    ): HentBehandlingskjedeResponse {
        return porttype.hentBehandlingskjede(
            WSHentBehandlingskjedeRequest()
                .withBehandlingskjedeId(behandlingskjedeId)
        ).fromWS()
    }

    @GetMapping("/henthenvendelseliste")
    override fun hentHenvendelseListe(
        @RequestParam("fodselsnummer") fodselsnummer: String,
        @RequestParam("typer") typer: List<HenvendelseType>
    ): HentHenvendelseListeResponse {
        return porttype.hentHenvendelseListe(
            WSHentHenvendelseListeRequest()
                .withFodselsnummer(fodselsnummer)
                .withTyper(typer.map { it.name })
        ).fromWS()
    }

    @GetMapping("/ping")
    override fun ping() {
        return porttype.ping()
    }
}
