package no.nav.henvendelse.consumer.sak

import no.nav.henvendelse.utils.Pingable
import java.time.OffsetDateTime

interface SakApi : Pingable {
    fun hentSak(saksId: String): SakDto
}

data class SakDto(
    val id: String? = null,
    val tema: String? = null, // example: AAP
    val applikasjon: String? = null, // example: IT01 Kode for applikasjon iht. felles kodeverk
    val aktoerId: String? = null, // example: 10038999999 Id til aktøren saken gjelder
    val orgnr: String? = null, // Orgnr til foretaket saken gjelder
    val fagsakNr: String? = null, // Fagsaknr for den aktuelle saken - hvis aktuelt
    val opprettetAv: String? = null, // Brukerident til den som opprettet saken
    val opprettetTidspunkt: OffsetDateTime? = null
)
