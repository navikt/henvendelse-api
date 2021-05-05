package no.nav.henvendelse.rest.henvendelseinformasjon

import no.nav.henvendelse.rest.common.Henvendelse
import no.nav.henvendelse.rest.common.HenvendelseType
import no.nav.henvendelse.rest.common.fromWS
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.*

interface HenvendelseApi {
    fun hentHenvendelse(behandlingsId: String): HentHenvendelseResponse
    fun hentBehandlingskjede(behandlingskjedeId: String): HentBehandlingskjedeResponse
    fun hentHenvendelseListe(
        fodselsnummer: String,
        typer: List<HenvendelseType>
    ): HentHenvendelseListeResponse
    fun ping()
}

class HentHenvendelseResponse(
    val henvendelse: Henvendelse
)
fun WSHentHenvendelseResponse.fromWS() = HentHenvendelseResponse((this.any as XMLHenvendelse).fromWS())

class HentBehandlingskjedeResponse(
    val henvendelser: List<Henvendelse>
)
fun WSHentBehandlingskjedeResponse.fromWS() = HentBehandlingskjedeResponse(
    this.any.map { (it as XMLHenvendelse).fromWS() }
)

class HentHenvendelseListeResponse(
    val henvendelser: List<Henvendelse>
)
fun WSHentHenvendelseListeResponse.fromWS() = HentHenvendelseListeResponse(
    this.any.map { (it as XMLHenvendelse).fromWS() }
)
