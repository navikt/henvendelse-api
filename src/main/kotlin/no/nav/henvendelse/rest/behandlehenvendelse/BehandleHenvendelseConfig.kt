package no.nav.henvendelse.rest.behandlehenvendelse

import no.nav.common.cxf.StsConfig
import no.nav.common.utils.EnvironmentUtils
import no.nav.henvendelse.utils.CXFClient
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLJournalfortInformasjon
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.behandlehenvendelse.BehandleHenvendelsePortType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BehandleHenvendelseConfig {
    @Autowired
    lateinit var stsConfig: StsConfig

    @Bean
    fun behandleHenvendelsePorttype(): BehandleHenvendelsePortType =
        CXFClient<BehandleHenvendelsePortType>()
            .wsdl("classpath:wsdl/BehandleHenvendelse.wsdl")
            .address(EnvironmentUtils.getRequiredProperty("DOMENE_BRUKERDIALOG_BEHANDLEHENVENDELSE_V1_ENDPOINTURL"))
            .withProperty("jaxb.additionalContextClasses", arrayOf(XMLJournalfortInformasjon::class.java))
            .configureStsForSubject(stsConfig)
            .build()
}
