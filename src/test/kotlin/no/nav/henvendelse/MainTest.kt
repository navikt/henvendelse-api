import no.nav.common.nais.NaisYamlUtils
import no.nav.common.nais.NaisYamlUtils.getTemplatedConfig
import no.nav.common.test.ssl.SSLTestUtils
import no.nav.common.utils.SslUtils.setupTruststore
import no.nav.henvendelse.Application
import org.springframework.boot.SpringApplication

fun main(args: Array<String>) {
    setupTruststore()
    NaisYamlUtils.loadFromYaml(getTemplatedConfig(".nais/preprod.yaml", mapOf("namespace" to "q0")))
    SSLTestUtils.disableCertificateChecks()

    val app = SpringApplication(Application::class.java)
    app.setAdditionalProfiles("local")
    app.run(*args)
}
