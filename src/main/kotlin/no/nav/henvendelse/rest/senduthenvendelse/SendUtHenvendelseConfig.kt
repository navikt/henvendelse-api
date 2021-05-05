package no.nav.henvendelse.rest.senduthenvendelse

import no.nav.common.cxf.StsConfig
import no.nav.common.utils.EnvironmentUtils
import no.nav.henvendelse.utils.CXFClient
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMeldingFraBruker
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMeldingTilBruker
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.senduthenvendelse.SendUtHenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SendUtHenvendelseConfig {
    @Autowired
    lateinit var stsConfig: StsConfig

    @Bean
    fun sendUtHenvendelsePorttype(): SendUtHenvendelsePortType =
        CXFClient<SendUtHenvendelsePortType>()
            .wsdl("classpath:wsdl/SendUtHenvendelse.wsdl")
            .address(EnvironmentUtils.getRequiredProperty("DOMENE_BRUKERDIALOG_SENDUTHENVENDELSE_V1_ENDPOINTURL"))
            .withProperty(
                "jaxb.additionalContextClasses",
                arrayOf(
                    XMLHenvendelse::class.java,
                    XMLMetadataListe::class.java,
                    XMLMeldingFraBruker::class.java,
                    XMLMeldingTilBruker::class.java
                )
            )
            .configureStsForSubject(stsConfig)
            .build()
}
