package no.nav.henvendelse.consumer.sak

import no.nav.common.sts.SystemUserTokenProvider
import no.nav.common.utils.EnvironmentUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SakConfig {
    @Bean
    fun sakApi(systemTokenProvider: SystemUserTokenProvider): SakApi {
        return SakApiImpl(
            EnvironmentUtils.getRequiredProperty("SAK_ENDPOINTURL"),
            systemTokenProvider
        )
    }
}
