package no.nav.henvendelse.rest.behandlehenvendelse

import no.nav.henvendelse.rest.common.JournalfortInformasjon
import no.nav.henvendelse.rest.common.RestOperationNotSupportedException
import no.nav.henvendelse.rest.common.Temagruppe
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLOppgaveOpprettetInformasjon
import java.time.LocalDateTime

interface BehandleHenvendelseApi {
    fun knyttBehandlingskjedeTilTema(behandlingskjedeId: String, temakode: String) {
        throw RestOperationNotSupportedException("Skal ikke brukes av SF da denne bare brukes for BID-hacking")
    }
    fun markerTraadForHasteKassering(behandlingIdListe: List<String>) {
        throw RestOperationNotSupportedException("Kassering er kun en funksjon for brukerstøtte")
    }
    fun oppdaterOppgaveOpprettetInformasjon(request: OppgaveOpprettetInformasjon) {
        throw RestOperationNotSupportedException("Operasjonen kan bare brukes av dialogstyring")
    }
    fun oppdaterHenvendelsesarkivInformasjon(behandlingId: String, arkivpostId: String) {
        throw RestOperationNotSupportedException("Operasjonen kan bare brukes av dialogstyring")
    }
    fun oppdaterJournalfortInformasjon(behandlingId: String, journalfortInformasjon: JournalfortInformasjon) {
        throw RestOperationNotSupportedException("Operasjonen kan bare brukes av dialogstyring")
    }
    fun settOversendtDokmot(behandlingsId: String, oversendtDato: LocalDateTime) {
        throw RestOperationNotSupportedException("Operasjonen kan bare brukes av dialogstyring")
    }

    fun ferdigstillUtenSvar(request: FerdigstillRequest)
    fun oppdaterTilKassering(request: OppdaterKasseringRequest)
    fun oppdaterKontorsperre(request: KontorsperreRequest)
    fun oppdaterTemagruppe(request: OppdaterTemagruppeRequest)
    fun knyttBehandlingskjedeTilSak(request: KnyttTilSakRequest)
    fun ping()
}

class FerdigstillRequest(val behandlingskjedeId: String, val enhetId: String)
class KontorsperreRequest(val enhet: String, val behandlingsIdListe: List<String>)
class OppdaterKasseringRequest(val behandlingIdListe: List<String>)
class OppdaterTemagruppeRequest(val behandlingsId: String, val temagruppe: Temagruppe)
class KnyttTilSakRequest(
    val behandlingskjedeId: String,
    val saksId: String,
    val temakode: String,
    val journalforendeEnhet: String
)

class OppgaveOpprettetInformasjon(
    val behandlingsId: String,
    val oppgaveIdGsak: String,
    val henvendelseIdGsak: String
)
fun OppgaveOpprettetInformasjon.toWS() = XMLOppgaveOpprettetInformasjon()
    .withBehandlingsId(this.behandlingsId)
    .withOppgaveIdGsak(this.oppgaveIdGsak)
    .withHenvendelseIdGsak(this.henvendelseIdGsak)
