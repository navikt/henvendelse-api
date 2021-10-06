package no.nav.henvendelse.consumer.saf

import no.nav.common.sts.SystemUserTokenProvider
import no.nav.common.utils.EnvironmentUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SafConfig {
    @Bean
    fun safService(stsService: SystemUserTokenProvider) = SafService(
        url = EnvironmentUtils.getRequiredProperty("SAF_API_URL"),
        stsService = stsService
    )
}
