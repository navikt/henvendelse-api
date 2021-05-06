package no.nav.henvendelse.rest.common

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse

fun XMLHenvendelse.fromWS(): Henvendelse {
    return Henvendelse(
        behandlingsId = this.behandlingsId,
        behandlingskjedeId = this.behandlingskjedeId,
        fnr = this.fnr,
        aktorId = this.aktorId,
        tema = this.tema,
        henvendelseType = HenvendelseType.valueOf(this.henvendelseType)
    )
}
