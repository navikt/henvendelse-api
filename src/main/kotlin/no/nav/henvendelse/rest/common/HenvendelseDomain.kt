package no.nav.henvendelse.rest.common

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
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
@ApiModel(
    description = """
    Kanal bakgrunn for henvendelsen.
    Om HenvendelseType er 'REFERAT_OPPMOTE' eller 'REFERAT_TELEFON' må denne samsvare.
    For andre STO HenvendelseTyper så settes denne alltid til 'TEKST'  
"""
)
enum class Kanal {
    TEKST, TELEFON, OPPMOTE
}

@ApiModel(
    description = """
    Fullstendig liste over henvendelsetype som kan hentes ut og sendes inn til henvendelse.   
"""
)
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

@ApiModel(
    description = """
    All informasjon henvendelse holder styr på   
"""
)
class Henvendelse(
    @ApiModelProperty(example = "1001ABBA")
    val behandlingsId: String? = null,
    @ApiModelProperty(example = "1001ABBA")
    val behandlingskjedeId: String? = null,
    @ApiModelProperty(example = "BD10")
    val applikasjonsId: String? = null,
    @ApiModelProperty(example = "12345678910")
    val fnr: String? = null,
    @ApiModelProperty(example = "1234567891056")
    val aktorId: String? = null,
    @ApiModelProperty(example = "KNA")
    val tema: String? = null,
    val behandlingstema: String? = null,
    val ferdigstiltUtenSvar: Boolean? = null,
    val henvendelseType: HenvendelseType? = null,
    @ApiModelProperty(value = "NavIdent", example = "Z999999")
    val eksternAktor: String? = null,
    @ApiModelProperty(example = "2810")
    val tilknyttetEnhet: String? = null,
    val opprettetDato: LocalDateTime? = null,
    val avsluttetDato: LocalDateTime? = null,
    val lestDato: LocalDateTime? = null,
    @ApiModelProperty(example = "2810")
    val kontorsperreEnhet: String? = null,
    @ApiModelProperty(example = "500101")
    val brukersEnhet: String? = null,
    @ApiModelProperty(value = "Nav-Ident", example = "Z99999")
    val markertSomFeilsendtAv: String? = null,
    @ApiModelProperty(example = "123456789")
    val oppgaveIdGsak: String? = null,
    @ApiModelProperty(example = "123456789")
    val henvendelseIdGsak: String? = null,
    val erTilknyttetAnsatt: Boolean? = null,
    val gjeldendeTemagruppe: Temagruppe? = null,
    val journalfortInformasjon: JournalfortInformasjon? = null,
    val markeringer: Markeringer? = null,
    @ApiModelProperty(example = "abcd-defg-1234-abba")
    val korrelasjonsId: String? = null,
    val metadataListe: MetadataListe? = null
)

class JournalfortInformasjon(
    @ApiModelProperty(example = "123456789")
    val journalpostId: String?,
    @ApiModelProperty(example = "DAG")
    val journalfortTema: String?,
    val journalfortDato: LocalDateTime?,
    @ApiModelProperty(example = "123456789")
    val journalfortSaksId: String?,
    @ApiModelProperty(example = "Z999999")
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
    @ApiModelProperty(example = "Z999999")
    val aktor: String?
)
class Kontorsperre(
    dato: LocalDateTime?,
    aktor: String?,
    @ApiModelProperty(example = "2890")
    val enhet: String?
) : Markering(dato, aktor)

class MetadataListe(
    val metadata: List<Melding>?
)

@ApiModel(
    description = """
        Abstrakt klasse for alle henvendelser.
        Subtype: 'MeldingTilBruker', 'MeldingFraBruker', 'OppgaveVarsel' og 'DokumentVarsel'.
        
        Se 'MeldingTilBruker' for meldinger som skal sendes av saksbehandlere.
        Se 'MeldingFraBruker' for meldinger/spørsmål sendt inn av bruker. 
    """
)
abstract class Melding(
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

abstract class HenvendelseVarsel(
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
