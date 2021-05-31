package no.nav.henvendelse.rest.common

import assertk.assertThat
import assertk.assertions.*
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class HenvendelseMappingTest {
    @Test
    fun `alle felter skal kunne inneholde null`() {
        val xml = XMLHenvendelse()
        assertThat { xml.fromWS() }.isSuccess()
    }

    @Test
    fun `journalfortInformasjon skal kunne innehold null verdier`() {
        val xml = XMLHenvendelse()
            .withJournalfortInformasjon(XMLJournalfortInformasjon())
        assertThat { xml.fromWS() }.isSuccess()
    }

    @Test
    fun `markeringer skal kunne innehold null verdier`() {
        val xml = XMLHenvendelse()
            .withMarkeringer(XMLMarkeringer())
        assertThat { xml.fromWS() }.isSuccess()
    }

    @Test
    fun `metadata skal kunne innehold null verdier`() {
        val xml = XMLHenvendelse()
            .withMetadataListe(XMLMetadataListe().withMetadata(null, null))
        assertThat { xml.fromWS() }
            .isSuccess()
            .prop("metadataListe") { it.metadataListe?.metadata }
            .isNotNull()
            .isEmpty()
    }

    @Test
    fun `metadata inneholder bare meldinger`() {
        val xml = XMLHenvendelse()
            .withMetadataListe(
                XMLMetadataListe().withMetadata(
                    XMLVedlegg(),
                    XMLMeldingTilBruker(),
                    XMLSoknadMetadata()
                )
            )

        assertThat { xml.fromWS() }
            .isSuccess()
            .prop("metadataListe") { it.metadataListe?.metadata }
            .isNotNull()
            .hasSize(1)
    }

    @Test
    fun `jodatime DateTime konverteres til java LocalDateTime`() {
        val time = "2021-05-05T12:12:12.123+02"
        val jodatime = DateTime.parse(time)
        val parsedJavaTime = ZonedDateTime.parse(time)

        assertThat(jodatime.toJavaTime())
            .isEqualTo(parsedJavaTime)
    }
}
