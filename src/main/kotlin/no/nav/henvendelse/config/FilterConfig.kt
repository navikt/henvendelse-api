package no.nav.henvendelse.config

import no.nav.common.auth.context.UserRole
import no.nav.common.auth.oidc.discovery.OidcDiscoveryConfigurationClient
import no.nav.common.auth.oidc.filter.OidcAuthenticationFilter
import no.nav.common.auth.oidc.filter.OidcAuthenticator
import no.nav.common.auth.oidc.filter.OidcAuthenticatorConfig
import no.nav.common.rest.filter.SetStandardHttpHeadersFilter
import no.nav.common.utils.EnvironmentUtils
import no.nav.henvendelse.utils.DebugLogFilter
import no.nav.henvendelse.utils.Pingable
import no.nav.henvendelse.utils.createPingable
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
    fun logFilter() = FilterRegistrationBean<DebugLogFilter>()
        .apply {
            filter = DebugLogFilter("henvendelse-api", EnvironmentUtils.isDevelopment().orElse(false))
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
    fun openAMPingable() = createOidcPingable(
        name = "OpemAM",
        clientId = modiaClientId,
        discoveryUrl = issoDiscoveryUrl
    )

    @Bean
    fun azurePingable() = createOidcPingable(
        name = "Azure",
        clientId = azureClientId,
        discoveryUrl = azureDiscoveryUrl
    )

    @Bean
    fun standardHeadersFilter() = FilterRegistrationBean<SetStandardHttpHeadersFilter>()
        .apply {
            filter = SetStandardHttpHeadersFilter()
            order = 4
            addUrlPatterns("/*")
        }

    fun createOidcPingable(name: String, clientId: String, discoveryUrl: String): Pingable {
        val client = OidcDiscoveryConfigurationClient()

        return createPingable(
            description = "$name - $clientId",
            critical = false,
            test = {
                client.fetchDiscoveryConfiguration(discoveryUrl)
            }
        )
    }
}
