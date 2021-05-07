package no.nav.henvendelse.rest.common

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger("HenvendelseMapping")
fun XMLHenvendelse.fromWS(): Henvendelse {
    return Henvendelse(
        behandlingsId = this.behandlingsId,
        behandlingskjedeId = this.behandlingskjedeId,
        applikasjonsId = this.applikasjonsId,
        fnr = this.fnr,
        aktorId = this.aktorId,
        tema = this.tema,
        behandlingstema = this.behandlingstema,
        ferdigstiltUtenSvar = this.isFerdigstiltUtenSvar,
        henvendelseType = this.henvendelseType?.let(HenvendelseType::valueOf),
        eksternAktor = this.eksternAktor,
        tilknyttetEnhet = this.tilknyttetEnhet,
        opprettetDato = this.opprettetDato.toJavaTime(),
        avsluttetDato = this.avsluttetDato.toJavaTime(),
        lestDato = this.lestDato.toJavaTime(),
        kontorsperreEnhet = this.kontorsperreEnhet,
        brukersEnhet = this.brukersEnhet,
        markertSomFeilsendtAv = this.markertSomFeilsendtAv,
        oppgaveIdGsak = this.oppgaveIdGsak,
        henvendelseIdGsak = this.henvendelseIdGsak,
        erTilknyttetAnsatt = this.isErTilknyttetAnsatt,
        gjeldendeTemagruppe = this.gjeldendeTemagruppe?.let(Temagruppe::valueOf),
        journalfortInformasjon = this.journalfortInformasjon.fromWS(),
        markeringer = this.markeringer.fromWS(),
        korrelasjonsId = this.korrelasjonsId,
        metadataListe = this.metadataListe.fromWS()
    )
}

private fun XMLMetadataListe?.fromWS() = nullable {
    MetadataListe(
        this.metadata.mapNotNull { it.fromWS() }
    )
}

private fun XMLMetadata.fromWS(): Melding? {
    return when (this) {
        is XMLMelding -> this.fromWS()
        else -> {
            log.warn("Metadata type ikke støttet: {}", this::class.java.simpleName)
            null
        }
    }
}
private fun XMLMelding.fromWS(): Melding? {
    return when (this) {
        is XMLMeldingFraBruker -> MeldingFraBruker(
            temagruppe = this.temagruppe?.let(Temagruppe::valueOf),
            fritekst = this.fritekst
        )
        is XMLMeldingTilBruker -> MeldingTilBruker(
            temagruppe = this.temagruppe?.let(Temagruppe::valueOf),
            fritekst = this.fritekst,
            sporsmalsId = this.sporsmalsId,
            kanal = this.kanal?.let(Kanal::valueOf),
            navident = this.navident
        )
        is XMLOppgaveVarsel -> OppgaveVarsel(
            stoppRepeterendeVarsel = this.isStoppRepeterendeVarsel,
            oppgaveType = this.oppgaveType,
            oppgaveURL = this.oppgaveURL
        )
        is XMLDokumentVarsel -> DokumentVarsel(
            stoppRepeterendeVarsel = this.isStoppRepeterendeVarsel,
            journalpostId = this.journalpostId,
            dokumenttittel = this.dokumenttittel,
            dokumentIdListe = this.dokumentIdListe,
            ferdigstiltDato = this.ferdigstiltDato.toJavaTime(),
            temanavn = this.temanavn
        )
        else -> {
            log.warn("XMLMelding type ikke støttet: {}", this::class.java.simpleName)
            null
        }
    }
}

private fun XMLJournalfortInformasjon?.fromWS() = nullable {
    JournalfortInformasjon(
        journalpostId = this.journalpostId,
        journalfortTema = this.journalfortTema,
        journalfortDato = this.journalfortDato.toJavaTime(),
        journalfortSaksId = this.journalfortSaksId,
        journalforerNavIdent = this.journalforerNavIdent
    )
}

private fun XMLMarkeringer?.fromWS() = nullable {
    Markeringer(
        kontorsperre = this.kontorsperre.fromWS(),
        feilsendt = this.feilsendt.fromWS(),
        ferdigstiltUtenSvar = this.ferdigstiltUtenSvar.fromWS()
    )
}

private fun XMLKontorsperre?.fromWS() = nullable {
    Kontorsperre(
        dato = this.dato.toJavaTime(),
        aktor = this.aktor,
        enhet = this.enhet
    )
}

private fun XMLMarkering?.fromWS() = nullable {
    Markering(
        dato = this.dato.toJavaTime(),
        aktor = this.aktor
    )
}

private fun DateTime?.toJavaTime() = nullable {
    LocalDateTime.of(
        this.year,
        this.monthOfYear,
        this.dayOfMonth,
        this.hourOfDay,
        this.minuteOfHour,
        this.secondOfMinute
    )
}

private fun <S, T> S?.nullable(fn: S.() -> T): T? {
    return if (this == null) null else fn(this)
}
