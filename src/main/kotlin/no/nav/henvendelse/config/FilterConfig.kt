package no.nav.henvendelse.config

import no.nav.common.auth.context.UserRole
import no.nav.common.auth.oidc.filter.OidcAuthenticationFilter
import no.nav.common.auth.oidc.filter.OidcAuthenticator
import no.nav.common.auth.oidc.filter.OidcAuthenticatorConfig
import no.nav.common.log.LogFilter
import no.nav.common.rest.filter.SetStandardHttpHeadersFilter
import no.nav.common.utils.EnvironmentUtils
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!local")
class FilterConfig {
    /**
     * Disse verdiene kommer fra nais-yaml, og er bare her for å forenkle testing
     */
    private val issoDiscoveryUrl = EnvironmentUtils.getRequiredProperty("ISSO_DISCOVERY_URL")
    private val modiaClientId = EnvironmentUtils.getRequiredProperty("MODIA_CLIENT_ID")

    /**
     * Azure verdiene er automatisk injected til poden siden vi har lagt til azure-konfig i nais-yaml
     */
    private val azureDiscoveryUrl = EnvironmentUtils.getRequiredProperty("AZURE_APP_WELL_KNOWN_URL")
    private val azureClientId = EnvironmentUtils.getRequiredProperty("AZURE_APP_CLIENT_ID")

    @Bean
    fun logFilter() = FilterRegistrationBean<LogFilter>()
        .apply {
            filter = LogFilter("henvendelse-api", EnvironmentUtils.isDevelopment().orElse(false))
            order = 1
            addUrlPatterns("/*")
        }

    @Bean
    fun userTokenFilter() = FilterRegistrationBean<OidcAuthenticationFilter>()
        .apply {
            filter = OidcAuthenticationFilter(
                OidcAuthenticator.fromConfigs(
                    OidcAuthenticatorConfig()
                        .withClientIds(listOf(azureClientId))
                        .withDiscoveryUrl(azureDiscoveryUrl)
                        .withUserRole(UserRole.INTERN),
                    OidcAuthenticatorConfig()
                        .withClientIds(listOf(modiaClientId))
                        .withDiscoveryUrl(issoDiscoveryUrl)
                        .withUserRole(UserRole.INTERN)
                )
            )
            order = 3
            addUrlPatterns("/api/*")
        }

    @Bean
    fun standardHeadersFilter() = FilterRegistrationBean<SetStandardHttpHeadersFilter>()
        .apply {
            filter = SetStandardHttpHeadersFilter()
            order = 4
            addUrlPatterns("/*")
        }
}
