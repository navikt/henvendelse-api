package no.nav.henvendelse.rest.sendsamtalereferat

import no.nav.common.types.identer.NavIdent
import no.nav.henvendelse.rest.common.HenvendelseType
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMeldingTilBruker
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.meldinger.WSSendUtHenvendelseRequest
import org.joda.time.DateTime

enum class GodkjentSamtalereferatTemagruppe {
    ARBD,
    HELSE,
    FMLI,
    HJLPM,
    PENS,
    OVRG
}
enum class SamtalereferatKanal {
    TELEFON, OPPMOTE;

    fun asHenvendelseType() = HenvendelseType.valueOf("REFERAT_${this.name}")
}
class SendSamtalereferatRequest(
    val fnr: String,
    val enhet: String,
    val fritekst: String,
    val temagruppe: GodkjentSamtalereferatTemagruppe,
    val kanal: SamtalereferatKanal
) {
    fun tilRequest(navIdent: NavIdent) = WSSendUtHenvendelseRequest()
        .withFodselsnummer(fnr)
        .withType(this.kanal.asHenvendelseType().name)
        .withAny(tilXMLHenvendelse(navIdent))

    private fun tilXMLHenvendelse(navIdent: NavIdent) = XMLHenvendelse()
        .withHenvendelseType(this.kanal.asHenvendelseType().name)
        .withFnr(fnr)
        .withOpprettetDato(DateTime.now())
        .withAvsluttetDato(DateTime.now())
        .withTema("KNA")
        .withEksternAktor(navIdent.get())
        .withTilknyttetEnhet(enhet)
        .withErTilknyttetAnsatt(true)
        .withMetadataListe(
            XMLMetadataListe().withMetadata(
                XMLMeldingTilBruker()
                    .withTemagruppe(temagruppe.name)
                    .withKanal(kanal.name)
                    .withFritekst(fritekst)
                    .withNavident(navIdent.get())
            )
        )
}
