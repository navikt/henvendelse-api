package no.nav.henvendelse.consumer.pdl

import no.nav.common.sts.SystemUserTokenProvider
import no.nav.common.utils.EnvironmentUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PdlConfig {
    @Bean
    fun pdlService(stsService: SystemUserTokenProvider) = PdlService(
        url = EnvironmentUtils.getRequiredProperty("PDL_API_URL"),
        stsService = stsService
    )
}
