package no.nav.henvendelse.consumer.pdl

import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.rest.client.RestClient
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.henvendelse.consumer.GraphQLClient
import no.nav.henvendelse.consumer.GraphQLClientConfig
import no.nav.henvendelse.consumer.getOrThrow
import no.nav.henvendelse.consumer.pdl.queries.HentAktorIder
import no.nav.henvendelse.utils.Pingable

class PdlException(message: String, cause: Throwable) : RuntimeException(message, cause)

open class PdlService(
    private val url: String,
    private val stsService: SystemUserTokenProvider,
) : Pingable {
    private val httpClient = RestClient.baseClient()
    private val graphqlClient = GraphQLClient(
        httpClient,
        GraphQLClientConfig(
            tjenesteNavn = "PDL",
            requestConfig = { callId ->
                val appToken: String = stsService.systemUserToken
                    ?: throw IllegalStateException("Kunne ikke hente ut systemusertoken")

                url(url)
                header("Nav-Call-Id", callId)
                header("Nav-Consumer-Id", "henvendelse-api")
                header("Nav-Consumer-Token", "Bearer $appToken")
                header("Authorization", "Bearer $appToken")
                header("Tema", "GEN")
            }
        )
    )

    fun hentAktorIder(fnr: String): List<String>? {
        return graphqlClient
            .runCatching {
                execute(
                    HentAktorIder(HentAktorIder.Variables(fnr))
                )
            }
            .map { response ->
                response
                    .data
                    ?.hentIdenter
                    ?.identer
                    ?.map { it.ident }
            }
            .getOrThrow {
                PdlException("Feil ved uthenting av aktorid", it)
            }
    }

    override fun ping() = SelfTestCheck("PDL via $url", false) {
        graphqlClient
            .runCatching {
                execute(
                    HentAktorIder(HentAktorIder.Variables("00000000000"))
                )
            }
            .fold(
                onSuccess = { HealthCheckResult.healthy() },
                onFailure = { HealthCheckResult.unhealthy(it) }
            )
    }

    companion object {
        fun lastQueryFraFil(name: String): String {
            return GraphQLClient::class.java
                .getResource("/pdl/$name.graphql")
                .readText()
                .replace("[\n\r]", "")
        }
    }
}
