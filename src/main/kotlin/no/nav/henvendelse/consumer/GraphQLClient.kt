package no.nav.henvendelse.consumer

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JavaType
import no.nav.common.log.MDCConstants
import no.nav.henvendelse.utils.OkHttpUtils
import no.nav.henvendelse.utils.TjenestekallLogger
import no.nav.henvendelse.utils.getCallId
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.MDC
import java.util.*

interface GraphQLVariables
interface GraphQLResult
data class GraphQLError(val message: String)
interface GraphQLRequest<VARIABLES : GraphQLVariables, RETURN_TYPE : GraphQLResult> {
    val query: String
    val variables: VARIABLES

    @get:JsonIgnore
    val expectedReturnType: Class<RETURN_TYPE>
}
data class GraphQLResponse<DATA>(
    val errors: List<GraphQLError>?,
    val data: DATA?
)

data class GraphQLClientConfig(
    val tjenesteNavn: String,
    val requestConfig: Request.Builder.(callId: String) -> Unit
)

class GraphQLClient(
    private val httpClient: OkHttpClient,
    private val config: GraphQLClientConfig
) {
    private val log = TjenestekallLogger.logger

    fun <VARS : GraphQLVariables, DATA : GraphQLResult, REQUEST : GraphQLRequest<VARS, DATA>> execute(
        request: REQUEST
    ): GraphQLResponse<DATA> {
        val callId = getCallId()
        try {
            log.info(
                """
                    ${config.tjenesteNavn}-request: $callId
                    ------------------------------------------------------------------------------------
                        callId: ${MDC.get(MDCConstants.MDC_CALL_ID)}
                    ------------------------------------------------------------------------------------
                """.trimIndent()
            )

            val requestBody: String = OkHttpUtils.objectMapper.writeValueAsString(request)
            val httpRequest = Request.Builder()
                .header("Content-Type", "application/json")
                .also {
                    config.requestConfig.invoke(it, callId)
                }
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                .build()

            val httpResponse = httpClient.newCall(httpRequest).execute()

            val body: String? = httpResponse.body()?.string()
            log.info(
                """
                    ${config.tjenesteNavn}-response: $callId
                    ------------------------------------------------------------------------------------
                        status: ${httpResponse.code()} 
                        body: $body
                    ------------------------------------------------------------------------------------
                """.trimIndent()
            )

            val typeReference: JavaType = OkHttpUtils.objectMapper.typeFactory
                .constructParametricType(GraphQLResponse::class.java, request.expectedReturnType)
            val response: GraphQLResponse<DATA> = OkHttpUtils.objectMapper.readValue(body, typeReference)

            if (response.errors?.isNotEmpty() == true) {
                val errorMessages = response.errors.joinToString(", ") { it.message }
                log.info(
                    """
                        ${config.tjenesteNavn}-response: $callId
                        ------------------------------------------------------------------------------------
                            status: ${httpResponse.code()} ${response.data}
                            errors: $errorMessages
                        ------------------------------------------------------------------------------------
                    """.trimIndent()
                )
                throw Exception(errorMessages)
            }

            return response
        } catch (exception: Exception) {
            log.error(
                """
                    ${config.tjenesteNavn}-response: $callId
                    ------------------------------------------------------------------------------------
                        exception:
                        $exception
                    ------------------------------------------------------------------------------------
                """.trimIndent()
            )

            throw exception
        }
    }
}

inline fun <T> Result<T>.getOrThrow(fn: (Throwable) -> Throwable): T {
    val exception = exceptionOrNull()
    if (exception != null) {
        throw fn(exception)
    }
    return getOrThrow()
}
