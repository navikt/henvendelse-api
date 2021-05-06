package no.nav.henvendelse.rest.henvendelseinformasjon

import no.nav.common.cxf.StsConfig
import no.nav.common.utils.EnvironmentUtils
import no.nav.henvendelse.utils.CXFClient
import no.nav.henvendelse.utils.Pingable
import no.nav.henvendelse.utils.createPingable
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMeldingFraBruker
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMeldingTilBruker
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HenvendelseInformasjonConfig {

    @Autowired
    lateinit var stsConfig: StsConfig

    @Bean
    fun henvendelstPorttype(): HenvendelsePortType =
        createHenvendelsePorttype()
            .configureStsForSubject(stsConfig)
            .build()

    @Bean
    fun henvendelstPorttypePing(): Pingable {
        val porttype = createHenvendelsePorttype()
            .configureStsForSystemUser(stsConfig)
            .build()

        return createPingable(
            description = "HenvendelsePortType",
            critical = true,
            test = { porttype.ping() }
        )
    }

    private fun createHenvendelsePorttype() =
        CXFClient<HenvendelsePortType>()
            // TODO Trenger vi denne?
            // .wsdl("classpath:wsdl/Henvendelse.wsdl")
            .address(EnvironmentUtils.getRequiredProperty("DOMENE_BRUKERDIALOG_HENVENDELSE_V2_ENDPOINTURL"))
            .timeout(10000, 60000)
            .withProperty(
                "jaxb.additionalContextClasses",
                arrayOf(
                    XMLHenvendelse::class.java,
                    XMLMetadataListe::class.java,
                    XMLMeldingFraBruker::class.java,
                    XMLMeldingTilBruker::class.java
                )
            )
}
