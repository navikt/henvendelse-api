package no.nav.henvendelse.rest.behandlehenvendelse

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
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
    fun ping() {
        throw RestOperationNotSupportedException("Ping operasjonen er erstattet av isAlive mot henvendelse-api")
    }

    fun ferdigstillUtenSvar(request: FerdigstillRequest)
    fun oppdaterTilKassering(request: OppdaterKasseringRequest)
    fun oppdaterKontorsperre(request: KontorsperreRequest)
    fun oppdaterTemagruppe(request: OppdaterTemagruppeRequest)
    fun knyttBehandlingskjedeTilSak(request: KnyttTilSakRequest)
}

@ApiModel()
class FerdigstillRequest(
    @ApiModelProperty(example = "1001ABBA")
    val behandlingskjedeId: String,
    @ApiModelProperty(example = "4110")
    val enhetId: String
)
class KontorsperreRequest(
    @ApiModelProperty(example = "[\"1001ABBA\", \"1020ACDC\"]")
    val behandlingsIdListe: List<String>,
    @ApiModelProperty(example = "4110")
    val enhet: String
)
class OppdaterKasseringRequest(
    @ApiModelProperty(example = "[\"1001ABBA\", \"1020ACDC\"]")
    val behandlingsIdListe: List<String>
)
class OppdaterTemagruppeRequest(
    @ApiModelProperty(example = "1001ABBA")
    val behandlingsId: String,
    @ApiModelProperty(example = "ARBD")
    val temagruppe: Temagruppe
)
class KnyttTilSakRequest(
    @ApiModelProperty(example = "1001ABBA")
    val behandlingskjedeId: String,
    @ApiModelProperty(example = "123456")
    val saksId: String,
    @ApiModelProperty(example = "DAG")
    val temakode: String,
    @ApiModelProperty(example = "4110")
    val journalforendeEnhet: String
)

class OppgaveOpprettetInformasjon(
    @ApiModelProperty(example = "1001ABBA")
    val behandlingsId: String,
    @ApiModelProperty(example = "123456789")
    val oppgaveIdGsak: String,
    @ApiModelProperty(example = "123456789")
    val henvendelseIdGsak: String
)
fun OppgaveOpprettetInformasjon.toWS() = XMLOppgaveOpprettetInformasjon()
    .withBehandlingsId(this.behandlingsId)
    .withOppgaveIdGsak(this.oppgaveIdGsak)
    .withHenvendelseIdGsak(this.henvendelseIdGsak)
