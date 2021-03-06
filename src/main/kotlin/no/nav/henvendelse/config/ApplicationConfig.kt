package no.nav.henvendelse.config

import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.common.cxf.StsConfig
import no.nav.common.sts.NaisSystemUserTokenProvider
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.common.utils.EnvironmentUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {
    companion object {
        val SRV_USERNAME_PROPERTY = "SYSTEMUSER_USERNAME"
        val SRV_PASSWORD_PROPERTY = "SYSTEMUSER_PASSWORD"
    }

    @Bean
    fun stsConfig() = StsConfig.builder()
        .url(EnvironmentUtils.getRequiredProperty("SECURITYTOKENSERVICE_URL"))
        .username(EnvironmentUtils.getRequiredProperty(SRV_USERNAME_PROPERTY))
        .password(EnvironmentUtils.getRequiredProperty(SRV_PASSWORD_PROPERTY))
        .build()

    @Bean
    fun serviceUserProvider(): SystemUserTokenProvider = NaisSystemUserTokenProvider(
        EnvironmentUtils.getRequiredProperty("SECURITY_TOKEN_SERVICE_DISCOVERY_URL"),
        EnvironmentUtils.getRequiredProperty(SRV_USERNAME_PROPERTY),
        EnvironmentUtils.getRequiredProperty(SRV_PASSWORD_PROPERTY)
    )

    @Bean
    fun authContextHolder() = AuthContextHolderThreadLocal.instance()
}
