package no.nav.henvendelse.utils

import no.nav.common.log.LogFilter
import no.nav.common.log.LogFilter.isInternalRequest
import no.nav.common.log.LogUtils
import no.nav.common.log.MDCConstants
import no.nav.common.utils.IdUtils
import no.nav.common.utils.StringUtils
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import javax.servlet.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DebugLogFilter(val applicationName: String, val exposeErrorDetails: Boolean) : Filter {
    val delegatedFiler = LogFilter(applicationName, exposeErrorDetails)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest && response is HttpServletResponse) {
            if (request.getAttribute(LOG_FILTER_FILTERED) != null) {
                chain.doFilter(request, response)
            } else {
                request.setAttribute(LOG_FILTER_FILTERED, "true")
                try {
                    filter(request, response, chain)
                } finally {
                    request.removeAttribute(LOG_FILTER_FILTERED)
                }
            }
        } else {
            throw ServletException("DebugLogFilter support only HTTP requests")
        }
    }

    private fun filter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val userId = delegatedFiler.resolveUserId(request)
        if (userId.isNullOrEmpty()) {
            generateUserIdCookie(response)
        }
        val consumerId = request.getHeader(CONSUMER_ID_HEADER_NAME)
        val callId = NAV_CALL_ID_HEADER_NAMES
            .map { request.getHeader(it) }
            .firstOrNull(StringUtils::notNullOrEmpty)
            ?: IdUtils.generateId()

        MDC.put(MDCConstants.MDC_CALL_ID, callId)
        MDC.put(MDCConstants.MDC_USER_ID, userId)
        MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerId)
        MDC.put(MDCConstants.MDC_REQUEST_ID, IdUtils.generateId())

        response.setHeader(PREFERRED_NAV_CALL_ID_HEADER_NAME, callId)
        response.setHeader("Server", applicationName)
        try {
            filterWithErrorHandling(request, response, chain)
            if (!isInternalRequest(request)) {
                LogUtils.buildMarker()
                    .field("status", response.status)
                    .field("method", request.method)
                    .field("host", request.serverName)
                    .field("path", request.requestURI)
                    .apply {
                        if (exposeErrorDetails) {
                            field("headers", (request.headerNames?.toList() ?: emptyList()).joinToString { "$it=${request.getHeader(it)}" })
                            field("cookie", (request.cookies ?: emptyArray()).joinToString { "${it.name}=${it.value}" })
                        }
                    }
                    .log(tjenestekallLogg::info)
            }
        } finally {
            MDC.remove(MDCConstants.MDC_CALL_ID)
            MDC.remove(MDCConstants.MDC_USER_ID)
            MDC.remove(MDCConstants.MDC_CONSUMER_ID)
            MDC.remove(MDCConstants.MDC_REQUEST_ID)
        }
    }

    private fun filterWithErrorHandling(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        try {
            chain.doFilter(request, response)
        } catch (e: Exception) {
            tjenestekallLogg.error(e.message, e)
            if (response.isCommitted) {
                tjenestekallLogg.error("failed with status={}", response.status)
                throw e
            } else {
                response.status = 500
                if (exposeErrorDetails) {
                    e.printStackTrace(response.writer)
                }
            }
        }
    }

    private fun generateUserIdCookie(httpResponse: HttpServletResponse) {
        val userId = IdUtils.generateId()
        val cookie = Cookie(RANDOM_USER_UD_COOKIE_NAME, userId)
        cookie.path = "/"
        cookie.maxAge = ONE_MONTH_IN_SECONDS
        cookie.isHttpOnly = true
        cookie.secure = true
        httpResponse.addCookie(cookie)
    }

    companion object {
        private val tjenestekallLogg = LoggerFactory.getLogger("SecureLog")
        val LOG_FILTER_FILTERED = "LOG_FILTER_FILTERED"
        val RANDOM_USER_UD_COOKIE_NAME = "RUIDC"
        val ONE_MONTH_IN_SECONDS = 60 * 60 * 24 * 30
        val CONSUMER_ID_HEADER_NAME = "Nav-Consumer-Id"
        val PREFERRED_NAV_CALL_ID_HEADER_NAME = "Nav-Call-Id"
        val NAV_CALL_ID_HEADER_NAMES = listOf(
            PREFERRED_NAV_CALL_ID_HEADER_NAME,
            "Nav-CallId",
            "X-Correlation-Id"
        )
    }
}
