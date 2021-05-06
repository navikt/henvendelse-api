package no.nav.henvendelse.rest.behandlehenvendelse

import no.nav.common.cxf.StsConfig
import no.nav.common.utils.EnvironmentUtils
import no.nav.henvendelse.utils.CXFClient
import no.nav.henvendelse.utils.Pingable
import no.nav.henvendelse.utils.createPingable
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
        createBehandleHenvendelsePorttype()
            .configureStsForSubject(stsConfig)
            .build()

    @Bean
    fun behandleHenvendelsePorttypePing(): Pingable {
        val porttype = createBehandleHenvendelsePorttype()
            .configureStsForSystemUser(stsConfig)
            .build()

        return createPingable(
            description = "BehandleHenvendelsePortType",
            critical = true,
            test = { porttype.ping() }
        )
    }

    private fun createBehandleHenvendelsePorttype() =
        CXFClient<BehandleHenvendelsePortType>()
            .wsdl("classpath:wsdl/BehandleHenvendelse.wsdl")
            .address(EnvironmentUtils.getRequiredProperty("DOMENE_BRUKERDIALOG_BEHANDLEHENVENDELSE_V1_ENDPOINTURL"))
            .withProperty("jaxb.additionalContextClasses", arrayOf(XMLJournalfortInformasjon::class.java))
}
