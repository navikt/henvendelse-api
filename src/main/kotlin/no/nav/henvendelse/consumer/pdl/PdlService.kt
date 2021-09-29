package no.nav.henvendelse.consumer.pdl

import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.rest.client.RestClient
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.henvendelse.consumer.pdl.queries.HentAktorIder
import no.nav.henvendelse.utils.Pingable
import okhttp3.Request

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

    override fun ping() = SelfTestCheck("PDL via $url", true) {
        runCatching {
            val ping = Request.Builder()
                .url(url)
                .build()
            val response = httpClient.newCall(ping).execute()
            if (response.code() == 200) {
                HealthCheckResult.healthy()
            } else {
                HealthCheckResult.unhealthy("Feil status kode ${response.code()}")
            }
        }
            .fold(
                onSuccess = { it },
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

private inline fun <T> Result<T>.getOrThrow(fn: (Throwable) -> Throwable): T {
    val exception = exceptionOrNull()
    if (exception != null) {
        throw fn(exception)
    }
    return getOrThrow()
}
