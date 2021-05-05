package no.nav.henvendelse.rest.common

import io.swagger.annotations.ApiModel
import java.time.LocalDateTime

@ApiModel(description = "Temagrupper som verdikjeden har kjennskap til")
enum class Temagruppe {
    ARBD,
    HELSE,
    FMLI,
    FDAG,
    HJLPM,
    BIL,
    ORT_HJE,
    OVRG,
    PENS,
    PLEIEPENGERSY,
    UFRT,
    UTLAND,
    OKSOS,
    ANSOS;
}
@ApiModel(description = "Kanal for samtalereferat")
enum class Kanal {
    TEKST, TELEFON, OPPMOTE
}

enum class HenvendelseType {
    DOKUMENT_VARSEL,
    OPPGAVE_VARSEL,
    DOKUMENTINNSENDING,
    DOKUMENTINNSENDING_ETTERSENDING,
    SEND_SOKNAD,
    SEND_SOKNAD_ETTERSENDING,
    SEND_SOKNAD_KOMMUNAL,
    SPORSMAL_SKRIFTLIG,
    SPORSMAL_SKRIFTLIG_DIREKTE,
    SVAR_SKRIFTLIG,
    SVAR_OPPMOTE,
    SVAR_TELEFON,
    DELVIS_SVAR_SKRIFTLIG,
    REFERAT_OPPMOTE,
    REFERAT_TELEFON,
    SPORSMAL_MODIA_UTGAAENDE,
    INFOMELDING_MODIA_UTGAAENDE,
    SVAR_SBL_INNGAAENDE
}

class Henvendelse(
    val behandlingsId: String?,
    val behandlingskjedeId: String?,
    val applikasjonsId: String?,
    val fnr: String?,
    val aktorId: String?,
    val tema: String?,
    val behandlingstema: String?,
    val ferdigstiltUtenSvar: Boolean?,
    val henvendelseType: HenvendelseType?,
    val eksternAktor: String?,
    val tilknyttetEnhet: String?,
    val opprettetDato: LocalDateTime?,
    val avsluttetDato: LocalDateTime?,
    val lestDato: LocalDateTime?,
    val kontorsperreEnhet: String?,
    val brukersEnhet: String?,
    val markertSomFeilsendtAv: String?,
    val oppgaveIdGsak: String?,
    val henvendelseIdGsak: String?,
    val erTilknyttetAnsatt: Boolean?,
    val gjeldendeTemagruppe: Temagruppe?,
    val journalfortInformasjon: JournalfortInformasjon?,
    val markeringer: Markeringer?,
    val korrelasjonsId: String?,
    val metadataListe: MetadataListe?
)

class JournalfortInformasjon(
    val journalpostId: String?,
    val journalfortTema: String?,
    val journalfortDato: LocalDateTime?,
    val journalfortSaksId: String?,
    val journalforerNavIdent: String?
//  Vedlegg er sannsynligvis ikke nødvendig, og sparer oss for en hel del ekstra mapping
//  val vedleggJournalfort: XMLVedleggJournalfort?,
)

class Markeringer(
    val kontorsperre: Kontorsperre?,
    val feilsendt: Markering?,
    val ferdigstiltUtenSvar: Markering?
)
open class Markering(
    val dato: LocalDateTime?,
    val aktor: String?
)
class Kontorsperre(
    dato: LocalDateTime?,
    aktor: String?,
    val enhet: String?
) : Markering(dato, aktor)

class MetadataListe(
    val metadata: List<Melding>?
)

@ApiModel(subTypes = [MeldingFraBruker::class, MeldingTilBruker::class, HenvendelseVarsel::class])
open class Melding(
    val temagruppe: Temagruppe?,
    val fritekst: String?
)

class MeldingFraBruker(
    temagruppe: Temagruppe?,
    fritekst: String?,
) : Melding(temagruppe, fritekst)

class MeldingTilBruker(
    temagruppe: Temagruppe?,
    fritekst: String?,
    val sporsmalsId: String?,
    val kanal: Kanal?,
    val navident: String?
) : Melding(temagruppe, fritekst)

@ApiModel(
    subTypes = [OppgaveVarsel::class, DokumentVarsel::class]
)
open class HenvendelseVarsel(
    val stoppRepeterendeVarsel: Boolean?
) : Melding(null, null)

class OppgaveVarsel(
    stoppRepeterendeVarsel: Boolean?,
    val oppgaveType: String?,
    val oppgaveURL: String?
) : HenvendelseVarsel(stoppRepeterendeVarsel)

class DokumentVarsel(
    stoppRepeterendeVarsel: Boolean?,
    val journalpostId: String?,
    val dokumenttittel: String?,
    val dokumentIdListe: List<String>?,
    val ferdigstiltDato: LocalDateTime?,
    val temanavn: String?
) : HenvendelseVarsel(stoppRepeterendeVarsel)
