package no.nav.henvendelse.rest.senduthenvendelse

import no.nav.henvendelse.rest.common.Henvendelse
import no.nav.henvendelse.rest.common.HenvendelseType
import no.nav.henvendelse.rest.common.RestOperationNotSupportedException
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.meldinger.WSFerdigstillHenvendelseRequest
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.meldinger.WSFerdigstillHenvendelseResponse
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.meldinger.WSSendUtHenvendelseRequest
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.meldinger.WSSendUtHenvendelseResponse

interface SendUtHenvendelseApi {
    fun avbrytHenvendelse(behandlingsId: String) {
        throw RestOperationNotSupportedException("Operasjonen kan bare brukes av dialogstyring")
    }
    fun slaSammenHenvendelser(behandlingsIder: List<String>): String {
        throw RestOperationNotSupportedException("Operasjonen kan bare brukes av modiapersonoversikt")
    }
    fun opprettHenvendelse(request: OpprettHenvendelseRequest): String
    fun ferdigstillHenvendelse(request: FerdigstillHenvendelseRequest): FerdigstillHenvendelseResponse
    fun sendUtHenvendelse(request: SendUtHenvendelseRequest): SendUtHenvendelseResponse
    fun ping()
}

class OpprettHenvendelseRequest(
    val fodselsnummer: String,
    val behandlingskjedeId: String,
    val type: HenvendelseType
)

class FerdigstillHenvendelseRequest(
    val behandlingsId: List<String>,
    val henvendelse: Henvendelse
)
class FerdigstillHenvendelseResponse
fun FerdigstillHenvendelseRequest.toWS() = WSFerdigstillHenvendelseRequest()
    .withBehandlingsId(this.behandlingsId)
    .withAny(this.henvendelse)

fun WSFerdigstillHenvendelseResponse.fromWS() = FerdigstillHenvendelseResponse()

class SendUtHenvendelseRequest(
    val fodselsnummer: String,
    val type: HenvendelseType,
    val henvendelse: Henvendelse
)
class SendUtHenvendelseResponse(
    val behandlingsId: String
)
fun SendUtHenvendelseRequest.toWS() = WSSendUtHenvendelseRequest()
    .withFodselsnummer(this.fodselsnummer)
    .withType(this.type.name)
    .withAny(this.henvendelse)

fun WSSendUtHenvendelseResponse.fromWS() = SendUtHenvendelseResponse(this.behandlingsId)
