package no.nav.henvendelse

import no.nav.common.utils.EnvironmentUtils.Type.PUBLIC
import no.nav.common.utils.EnvironmentUtils.setProperty
import no.nav.common.utils.NaisUtils
import no.nav.common.utils.SslUtils
import no.nav.henvendelse.config.ApplicationConfig.Companion.SRV_PASSWORD_PROPERTY
import no.nav.henvendelse.config.ApplicationConfig.Companion.SRV_USERNAME_PROPERTY
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    loadVaultSecrets()
    SslUtils.setupTruststore()
    runApplication<Application>(*args)
}

private fun loadVaultSecrets() {
    with(NaisUtils.getCredentials("service_user")) {
        setProperty(SRV_USERNAME_PROPERTY, this.username, PUBLIC)
        setProperty(SRV_PASSWORD_PROPERTY, this.password, PUBLIC)
    }
}
