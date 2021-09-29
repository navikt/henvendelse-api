package no.nav.henvendelse.consumer.pdl

import no.nav.common.rest.client.RestClient
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.henvendelse.consumer.pdl.queries.HentAktorIder

class PdlException(message: String, cause: Throwable) : RuntimeException(message, cause)

open class PdlService(
    private val url: String,
    private val stsService: SystemUserTokenProvider,
) {
    private val graphqlClient = GraphQLClient(
        RestClient.baseClient(),
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
                PdlException("Feil ved uthenting av GT", it)
            }
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
