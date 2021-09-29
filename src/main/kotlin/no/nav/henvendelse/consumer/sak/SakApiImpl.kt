package no.nav.henvendelse.consumer.sak

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.common.rest.client.RestClient
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.henvendelse.utils.AuthorizationInterceptor
import no.nav.henvendelse.utils.LoggingInterceptor
import no.nav.henvendelse.utils.OkHttpUtils
import no.nav.henvendelse.utils.XCorrelationIdInterceptor
import okhttp3.Request
import okhttp3.Response

class SakApiImpl(
    val baseUrl: String = "",
    val systemTokenProvider: SystemUserTokenProvider
) : SakApi {
    val client = RestClient.baseClient().newBuilder()
        .addInterceptor(XCorrelationIdInterceptor())
        .addInterceptor(
            AuthorizationInterceptor {
                systemTokenProvider.systemUserToken
            }
        )
        .addInterceptor(
            LoggingInterceptor("Sak") { request ->
                requireNotNull(request.header("X-Correlation-ID")) {
                    "Kall uten \"X-Correlation-ID\" er ikke lov"
                }
            }
        )
        .build()

    override fun hentSak(saksId: String): SakDto {
        val request = Request
            .Builder()
            .url("$baseUrl/api/v1/saker/$saksId")
            .header("accept", "application/json")
            .build()

        return fetch(request)
    }

    private inline fun <reified RESPONSE> fetch(request: Request): RESPONSE {
        val response: Response = client
            .newCall(request)
            .execute()

        val body = response.body()?.string()

        return if (response.code() in 200..299 && body != null) {
            OkHttpUtils.objectMapper.readValue(body)
        } else {
            throw IllegalStateException("Forventet 200-range svar og body fra sak-api, men fikk: ${response.code()} $body")
        }
    }
}
