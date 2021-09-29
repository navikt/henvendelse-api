package no.nav.henvendelse.utils

import org.slf4j.LoggerFactory
import java.lang.StringBuilder

object TjenestekallLogger {
    val logger = LoggerFactory.getLogger("SecureLog")

    fun info(header: String, fields: Map<String, Any?>) = logger.info(format(header, fields))
    fun warn(header: String, fields: Map<String, Any?>) = logger.warn(format(header, fields))
    fun error(header: String, fields: Map<String, Any?>) = logger.error(format(header, fields))
    fun error(header: String, fields: Map<String, Any?>, throwable: Throwable) = logger.error(format(header, fields), throwable)

    private fun format(header: String, fields: Map<String, Any?>): String {
        val sb = StringBuilder()
        sb.appendln(header)
        sb.appendln("------------------------------------------------------------------------------------")
        fields.forEach { (key, value) ->
            sb.appendln("$key: $value")
        }
        sb.appendln("------------------------------------------------------------------------------------")
        return sb.toString()
    }
}
