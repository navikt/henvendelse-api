package no.nav.henvendelse.consumer.saf

import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.rest.client.RestClient
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.henvendelse.consumer.GraphQLClient
import no.nav.henvendelse.consumer.GraphQLClientConfig
import no.nav.henvendelse.consumer.getOrThrow
import no.nav.henvendelse.consumer.saf.queries.HentBrukersSaker
import no.nav.henvendelse.utils.Pingable

class SafException(message: String, cause: Throwable) : RuntimeException(message, cause)

class SafService(
    private val url: String,
    private val stsService: SystemUserTokenProvider,
) : Pingable {
    private val httpClient = RestClient.baseClient()
    private val graphqlClient = GraphQLClient(
        httpClient,
        GraphQLClientConfig(
            tjenesteNavn = "SAF",
            requestConfig = { callId ->
                val appToken: String = stsService.systemUserToken
                    ?: throw IllegalStateException("Kunne ikke hente ut systemusertoken")

                url(url)
                header("Nav-Call-Id", callId)
                header("X-Correlation-ID", callId)
                header("Authorization", "Bearer $appToken")
                header("Content-Type", "application/json")
            }
        )
    )

    fun hentSaker(fnr: String): List<HentBrukersSaker.Sak> {
        return graphqlClient
            .runCatching {
                execute(
                    HentBrukersSaker(
                        HentBrukersSaker.Variables(
                            HentBrukersSaker.BrukerIdInput(
                                id = fnr,
                                type = HentBrukersSaker.BrukerIdType.FNR
                            )
                        )
                    )
                )
            }
            .map { response ->
                response
                    .data
                    ?.saker
                    ?.filterNotNull()
                    ?: emptyList()
            }
            .getOrThrow {
                SafException("Feil ved uthenting av saker", it)
            }
    }

    override fun ping() = SelfTestCheck("SAF via $url", true) {
        graphqlClient
            .runCatching {
                execute(
                    HentBrukersSaker(
                        HentBrukersSaker.Variables(
                            HentBrukersSaker.BrukerIdInput(
                                id = "00000000000",
                                type = HentBrukersSaker.BrukerIdType.FNR
                            )
                        )
                    )
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
                .getResource("/saf/$name.graphql")
                .readText()
                .replace("[\n\r]", "")
        }
    }
}
